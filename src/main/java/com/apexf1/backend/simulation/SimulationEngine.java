package com.apexf1.backend.simulation;

import com.apexf1.backend.model.Circuit;
import com.apexf1.backend.model.Driver;
import com.apexf1.backend.model.Race;
import com.apexf1.backend.model.SimulationResult;
import com.apexf1.backend.model.SimulationResultEntry;
import com.apexf1.backend.repository.DriverRepository;
import com.apexf1.backend.repository.SimulationResultRepository;
import com.apexf1.backend.repository.RaceRepository;
import com.apexf1.backend.simulation.dto.CarSnapshot;
import com.apexf1.backend.simulation.dto.SimulationSnapshot;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central, thread-safe manager for all running race simulations
 * (effectively a Spring-managed singleton). Starts a dedicated thread per
 * car via a shared thread pool, tracks live simulations in a concurrent map,
 * and builds JSON-friendly snapshots on demand.
 */
@Service
public class SimulationEngine {

    private final RaceRepository raceRepository;
    private final DriverRepository driverRepository;
    private final SimulationWebSocketHandler wsHandler;

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<Long, RaceSimulation> simulations = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1000);

    @org.springframework.beans.factory.annotation.Autowired
    public SimulationEngine(RaceRepository raceRepository,
                            DriverRepository driverRepository,
                            SimulationWebSocketHandler wsHandler) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.wsHandler = wsHandler;
    }

    private SimulationResultRepository resultRepository;

    /** Optional: when present, finished races are saved to MySQL. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setResultRepository(SimulationResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public SimulationEngine(RaceRepository raceRepository, DriverRepository driverRepository) {
        this(raceRepository, driverRepository, null);
    }

    /**
     * Start a new simulation for a race. All drivers in the DB take part.
     *
     * @param raceId          race to simulate (for circuit length / name / laps)
     * @param lapsOrNull      override lap count (0 or null = full circuit lap count)
     * @param timeScaleOrNull model-seconds per real-second (defaults to 5x)
     */
    public SimulationSnapshot start(Long raceId, Integer lapsOrNull, Double timeScaleOrNull) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new NoSuchElementException("Race " + raceId + " not found"));

        List<Driver> drivers = driverRepository.findAll();
        if (drivers.isEmpty()) {
            throw new IllegalStateException("No drivers in the database to simulate");
        }

        Circuit circuit = race.getCircuit();
        double lapLengthM = (circuit != null && circuit.getLengthKm() != null && circuit.getLengthKm() > 0)
                ? circuit.getLengthKm() * 1000.0 : 5000.0;
        int circuitLaps = (circuit != null && circuit.getTotalLaps() != null && circuit.getTotalLaps() > 0)
                ? circuit.getTotalLaps() : 50;
        int totalLaps = (lapsOrNull != null && lapsOrNull > 0) ? lapsOrNull : circuitLaps;
        double timeScale = (timeScaleOrNull != null && timeScaleOrNull > 0) ? timeScaleOrNull : 5.0;

        long simId = idSeq.incrementAndGet();
        double baseLapSec = referenceLapSec(circuit, lapLengthM);

        // Build each car's starting state with realistic 2026 team/driver pace and staggered strategy.
        List<CarSimState> cars = new ArrayList<>();
        for (Driver d : drivers) {
            long dId = d.getId() == null ? 0L : d.getId();
            Random seedRnd = new Random(dId * 997L + raceId * 131L + simId);
            String teamShort = (d.getTeam() != null && d.getTeam().getShortName() != null)
                    ? d.getTeam().getShortName() : "";
            double gapSec = teamPaceSec(teamShort) + driverOffsetSec(d.getCode())
                    + (seedRnd.nextDouble() - 0.5) * 0.55;
            double driverLapSec = Math.max(55.0, baseLapSec + gapSec);
            double basePaceKph = (lapLengthM / 1000.0) / (driverLapSec / 3600.0);

            String startCompound = seedRnd.nextDouble() < 0.45 ? "SOFT" : "MEDIUM";
            String nextCompound = "SOFT".equals(startCompound)
                    ? (seedRnd.nextDouble() < 0.65 ? "HARD" : "MEDIUM")
                    : "HARD";
            int minPit = Math.max(2, (int) Math.round(totalLaps * 0.34));
            int maxPit = Math.max(minPit + 1, (int) Math.round(totalLaps * 0.62));
            int plannedPitLap = Math.min(totalLaps - 1, minPit + seedRnd.nextInt(Math.max(1, maxPit - minPit + 1)));
            if (plannedPitLap < 2) plannedPitLap = Math.max(2, totalLaps / 2);

            String team = d.getTeam() != null ? d.getTeam().getName() : "—";
            String color = d.getTeam() != null ? d.getTeam().getColorHex() : "#888888";
            cars.add(new CarSimState(
                    dId, d.getCode(), d.getFullName(), team, color,
                    basePaceKph, startCompound, plannedPitLap, nextCompound));
        }

        RaceSimulation sim = new RaceSimulation(
                simId, raceId, race.getName(), totalLaps, lapLengthM, timeScale, cars);

        // Observer pattern in action: log each completed lap to console + stream live over WebSocket.
        sim.addObserver(new LoggingTelemetryObserver());
        if (wsHandler != null) {
            sim.addObserver(new WebSocketTelemetryObserver(this, wsHandler));
        }

        simulations.put(simId, sim);

        // One thread per car (the concurrency core), plus a manager thread that
        // flips the status to FINISHED once every car has completed.
        PaceStrategy strategy = new BalancedPaceStrategy();
        for (CarSimState car : cars) {
            long seed = car.getDriverId() * 31 + simId * 17;
            pool.submit(new CarRunner(sim, car, strategy, seed));
        }
        pool.submit(() -> {
            try {
                sim.awaitAllCars();
                sim.markFinished();
                persistResult(simId);
                sim.notifyFinished();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return snapshot(simId);
    }

    private static double referenceLapSec(Circuit circuit, double lapLengthM) {
        if (circuit != null && circuit.getName() != null) {
            String n = circuit.getName().toLowerCase();
            if (n.contains("bahrain")) return 92.4;
            if (n.contains("jeddah")) return 90.8;
            if (n.contains("suzuka")) return 90.5;
            if (n.contains("monaco")) return 73.2;
            if (n.contains("barcelona")) return 76.5;
            if (n.contains("silverstone")) return 87.5;
            if (n.contains("spa")) return 104.5;
            if (n.contains("monza")) return 81.2;
        }
        // Fallback for custom circuits (~215 km/h average F1 race speed)
        return (lapLengthM / 1000.0) / (215.0 / 3600.0);
    }

    private static double teamPaceSec(String shortName) {
        switch (shortName) {
            case "MER": return 0.00;
            case "MCL": return 0.08;
            case "FER": return 0.12;
            case "RBR": return 0.18;
            case "WIL": return 0.48;
            case "RB":  return 0.58;
            case "ALP": return 0.68;
            case "HAS": return 0.72;
            case "AST": return 0.82;
            case "AUD": return 0.90;
            case "CAD": return 1.15;
            default:    return 0.75;
        }
    }

    private static double driverOffsetSec(String code) {
        if (code == null) return 0.0;
        switch (code) {
            case "VER": case "ANT": case "LEC": case "NOR": return -0.14;
            case "RUS": case "HAM": case "PIA": case "ALO": return -0.08;
            case "SAI": case "GAS": case "ALB": case "LAW": case "HUL": return 0.00;
            default: return 0.10;
        }
    }

    /** Save the final classification of a finished race (one row per car). */
    private void persistResult(long simId) {
        if (resultRepository == null) return;
        try {
            SimulationSnapshot snap = snapshot(simId);
            SimulationResult result = new SimulationResult();
            result.setSimulationId(snap.simulationId);
            result.setRaceId(snap.raceId);
            result.setRaceName(snap.raceName);
            result.setTotalLaps(snap.totalLaps);
            result.setTimeScale(snap.timeScale);
            result.setFinishedAt(LocalDateTime.now());
            List<SimulationResultEntry> entries = new ArrayList<>();
            for (CarSnapshot c : snap.cars) {
                SimulationResultEntry e = new SimulationResultEntry();
                e.setResult(result);
                e.setPosition(c.position);
                e.setDriverId(c.driverId);
                e.setDriverCode(c.driverCode);
                e.setDriverName(c.driverName);
                e.setTeamName(c.teamName);
                e.setFinalLap(c.currentLap);
                e.setTotalDistanceM(c.totalDistanceM);
                e.setLastLapTimeMs(c.lastLapTimeMs);
                entries.add(e);
            }
            if (!entries.isEmpty()) {
                result.setWinnerCode(entries.get(0).getDriverCode());
                result.setWinnerName(entries.get(0).getDriverName());
            }
            result.setEntries(entries);
            resultRepository.save(result);
        } catch (RuntimeException ex) {
            System.err.println("Could not save simulation " + simId + ": " + ex.getMessage());
        }
    }

    public List<SimulationResult> listResults() {
        return resultRepository == null ? List.of() : resultRepository.findAllByOrderByFinishedAtDesc();
    }

    public SimulationResult getResult(Long id) {
        if (resultRepository == null) throw new NoSuchElementException("Results not available");
        return resultRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Simulation result " + id + " not found"));
    }

    /** Stop a running simulation (cooperative: car threads exit on next tick). */
    public SimulationSnapshot stop(long simId) {
        RaceSimulation sim = require(simId);
        sim.stop();
        return snapshot(simId);
    }

    public List<SimulationSnapshot> list() {
        List<SimulationSnapshot> out = new ArrayList<>();
        for (Long id : simulations.keySet()) out.add(snapshot(id));
        return out;
    }

    /** Build a consistent, position-sorted snapshot of one simulation. */
    public SimulationSnapshot snapshot(long simId) {
        RaceSimulation sim = require(simId);

        List<CarSimState> ordered = new ArrayList<>(sim.getCars());
        ordered.sort(Comparator.comparingDouble(CarSimState::getTotalDistanceM).reversed());

        double leaderDistance = ordered.isEmpty() ? 0 : ordered.get(0).getTotalDistanceM();
        int leaderLap = ordered.isEmpty() ? 0 : Math.min(ordered.get(0).getCurrentLap(), sim.getTotalLaps());

        List<CarSnapshot> carDtos = new ArrayList<>();
        int pos = 1;
        for (CarSimState c : ordered) {
            CarSnapshot dto = new CarSnapshot();
            dto.position = pos++;
            dto.driverId = c.getDriverId();
            dto.driverCode = c.getDriverCode();
            dto.driverName = c.getDriverName();
            dto.teamName = c.getTeamName();
            dto.teamColor = c.getTeamColor();
            dto.speedKph = round1(c.getSpeedKph());
            dto.currentLap = Math.min(c.getCurrentLap(), sim.getTotalLaps());
            dto.compound = c.getCompound();
            dto.tyreAgeLaps = c.getTyreAgeLaps();
            dto.lastLapTimeMs = c.getLastLapTimeMs();
            dto.totalDistanceM = round1(c.getTotalDistanceM());
            dto.gapToLeaderM = round1(leaderDistance - c.getTotalDistanceM());
            dto.finished = c.isFinished();
            carDtos.add(dto);
        }

        SimulationSnapshot snap = new SimulationSnapshot();
        snap.simulationId = sim.getId();
        snap.raceId = sim.getRaceId();
        snap.raceName = sim.getRaceName();
        snap.status = sim.getStatus().name();
        snap.totalLaps = sim.getTotalLaps();
        snap.lapLengthM = sim.getLapLengthM();
        snap.timeScale = sim.getTimeScale();
        snap.leaderLap = leaderLap;
        snap.cars = carDtos;
        return snap;
    }

    private RaceSimulation require(long simId) {
        RaceSimulation sim = simulations.get(simId);
        if (sim == null) throw new NoSuchElementException("Simulation " + simId + " not found");
        return sim;
    }

    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    @PreDestroy
    public void shutdown() {
        for (RaceSimulation sim : simulations.values()) sim.stop();
        pool.shutdownNow();
    }
}
