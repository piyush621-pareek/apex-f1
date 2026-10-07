package com.apexf1.backend.simulation;

import com.apexf1.backend.model.Circuit;
import com.apexf1.backend.model.Driver;
import com.apexf1.backend.model.Race;
import com.apexf1.backend.repository.DriverRepository;
import com.apexf1.backend.repository.RaceRepository;
import com.apexf1.backend.simulation.dto.CarSnapshot;
import com.apexf1.backend.simulation.dto.SimulationSnapshot;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

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

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<Long, RaceSimulation> simulations = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1000);

    public SimulationEngine(RaceRepository raceRepository, DriverRepository driverRepository) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * Start a new simulation for a race. All drivers in the DB take part.
     *
     * @param raceId    race to simulate (for circuit length / name)
     * @param lapsOrNull override lap count (defaults to the circuit's, capped)
     * @param timeScaleOrNull model-seconds per real-second (defaults to 20x)
     */
    public SimulationSnapshot start(Long raceId, Integer lapsOrNull, Double timeScaleOrNull) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new NoSuchElementException("Race " + raceId + " not found"));

        List<Driver> drivers = driverRepository.findAll();
        if (drivers.isEmpty()) {
            throw new IllegalStateException("No drivers in the database to simulate");
        }

        Circuit circuit = race.getCircuit();
        double lapLengthM = (circuit != null && circuit.getLengthKm() != null)
                ? circuit.getLengthKm() * 1000.0 : 5000.0;
        int circuitLaps = (circuit != null && circuit.getTotalLaps() != null)
                ? circuit.getTotalLaps() : 10;
        int totalLaps = (lapsOrNull != null && lapsOrNull > 0) ? lapsOrNull : Math.min(circuitLaps, 10);
        double timeScale = (timeScaleOrNull != null && timeScaleOrNull > 0) ? timeScaleOrNull : 20.0;

        // Build each car's starting state with a small, deterministic pace spread.
        List<CarSimState> cars = new ArrayList<>();
        for (Driver d : drivers) {
            Random seedRnd = new Random(d.getId() == null ? 0 : d.getId());
            double basePace = 205.0 + seedRnd.nextDouble() * 12.0;   // ~205-217 km/h
            String team = d.getTeam() != null ? d.getTeam().getName() : "—";
            String color = d.getTeam() != null ? d.getTeam().getColorHex() : "#888888";
            cars.add(new CarSimState(
                    d.getId(), d.getCode(), d.getFullName(), team, color, basePace));
        }

        long simId = idSeq.incrementAndGet();
        RaceSimulation sim = new RaceSimulation(
                simId, raceId, race.getName(), totalLaps, lapLengthM, timeScale, cars);

        // Observer pattern in action: log each completed lap to the console.
        sim.addObserver(new LoggingTelemetryObserver());

        simulations.put(simId, sim);

        // One thread per car (the concurrency core), plus a manager thread that
        // flips the status to FINISHED once every car has completed.
        PaceStrategy strategy = new BalancedPaceStrategy();
        for (CarSimState car : cars) {
            long seed = car.getDriverId() * 31 + simId;
            pool.submit(new CarRunner(sim, car, strategy, seed));
        }
        pool.submit(() -> {
            try {
                sim.awaitAllCars();
                sim.markFinished();
                sim.notifyFinished();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return snapshot(simId);
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
