package com.apexf1.backend.seed;

import com.apexf1.backend.model.*;
import com.apexf1.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Loads the 2026 dataset on first boot:
 *  - the correct 2026 grid: 11 teams (incl. Audi + Cadillac), 22 drivers
 *  - 8 circuits (each with an SVG track map in /static/img/tracks)
 *  - an 8-round calendar
 *  - for the 5 completed rounds: a Qualifying and a Race session with FULL
 *    simulated data (every lap of every driver, tyre stints, pit stops,
 *    positions, weather, and one telemetry lap per driver per session)
 *  - championship standings computed from those simulated results
 *
 * NOTE: the race data is SIMULATED sample data (real F1 live timing is
 * commercially licensed). Pace, tyre wear and pit-stop numbers are modelled,
 * so every chart on the Pit Wall has something real to draw.
 *
 * Old databases (seeded with the previous 2025/2026 mix) are detected and
 * rebuilt automatically on the next start.
 */
@Component
public class SeedDataRunner implements CommandLineRunner {

    private static final int SEASON = 2026;
    private static final int TELEMETRY_SAMPLES = 80;
    private static final int[] POINTS = {25, 18, 15, 12, 10, 8, 6, 4, 2, 1};
    private static final double[] GEAR_EDGES = {0, 62, 98, 135, 172, 212, 252, 292, 360};

    private final TeamRepository teamRepository;
    private final DriverRepository driverRepository;
    private final CircuitRepository circuitRepository;
    private final RaceRepository raceRepository;
    private final SessionRepository sessionRepository;
    private final LapTimeRepository lapTimeRepository;
    private final TelemetryPointRepository telemetryPointRepository;
    private final WeatherSampleRepository weatherSampleRepository;
    private final DriverStandingRepository driverStandingRepository;
    private final TeamStandingRepository teamStandingRepository;
    private final PredictionRepository predictionRepository;

    public SeedDataRunner(TeamRepository teamRepository,
                           DriverRepository driverRepository,
                           CircuitRepository circuitRepository,
                           RaceRepository raceRepository,
                           SessionRepository sessionRepository,
                           LapTimeRepository lapTimeRepository,
                           TelemetryPointRepository telemetryPointRepository,
                           WeatherSampleRepository weatherSampleRepository,
                           DriverStandingRepository driverStandingRepository,
                           TeamStandingRepository teamStandingRepository,
                           PredictionRepository predictionRepository) {
        this.teamRepository = teamRepository;
        this.driverRepository = driverRepository;
        this.circuitRepository = circuitRepository;
        this.raceRepository = raceRepository;
        this.sessionRepository = sessionRepository;
        this.lapTimeRepository = lapTimeRepository;
        this.telemetryPointRepository = telemetryPointRepository;
        this.weatherSampleRepository = weatherSampleRepository;
        this.driverStandingRepository = driverStandingRepository;
        this.teamStandingRepository = teamStandingRepository;
        this.predictionRepository = predictionRepository;
    }

    @Override
    public void run(String... args) {
        if (driverRepository.count() > 0) {
            if (!needsRefresh()) {
                return; // already seeded with the current dataset
            }
            System.out.println("APEX F1: old seed data detected - rebuilding with the corrected 2026 grid...");
            purgeAll();
        }
        seedAll();
    }

    /**
     * Rebuilds the seed dataset if:
     *  - old 2025 team ("Kick Sauber") is present or 2026 rookie ("LIN") is missing,
     *  - fewer than 16 sessions exist (all 8 circuits must have Qualifying + Race data for Pit Wall),
     *  - any circuit is missing its SVG track map URL.
     */
    private boolean needsRefresh() {
        boolean oldTeam = false;
        for (Team t : teamRepository.findAll()) {
            if ("Kick Sauber".equals(t.getName())) oldTeam = true;
        }
        boolean hasLindblad = false;
        for (Driver d : driverRepository.findAll()) {
            if ("LIN".equals(d.getCode())) hasLindblad = true;
        }
        boolean allSessionsSeeded = sessionRepository.count() >= 16;
        boolean allSvgMaps = true;
        for (Circuit c : circuitRepository.findAll()) {
            if (c.getMapImageUrl() == null || !c.getMapImageUrl().endsWith(".svg")) {
                allSvgMaps = false;
            }
        }
        return oldTeam || !hasLindblad || !allSessionsSeeded || !allSvgMaps;
    }

    /** Deletes everything in foreign-key-safe order. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.apexf1.backend.repository.SimulationResultRepository simulationResultRepository;

    private void purgeAll() {
        if (simulationResultRepository != null) simulationResultRepository.deleteAll();
        predictionRepository.deleteAllInBatch();
        telemetryPointRepository.deleteAllInBatch();
        lapTimeRepository.deleteAllInBatch();
        weatherSampleRepository.deleteAllInBatch();
        sessionRepository.deleteAllInBatch();
        driverStandingRepository.deleteAllInBatch();
        teamStandingRepository.deleteAllInBatch();
        raceRepository.deleteAllInBatch();
        driverRepository.deleteAllInBatch();
        circuitRepository.deleteAllInBatch();
        teamRepository.deleteAllInBatch();
    }

    private void seedAll() {
        // ---- Teams: the real 2026 entry list (logo files live in /static/img/teams) ----
        Team mercedes = team("Mercedes",        "MER", "German",   "#00D2A0", "mer");
        Team ferrari  = team("Ferrari",         "FER", "Italian",  "#E10600", "fer");
        Team redBull  = team("Red Bull Racing", "RBR", "Austrian", "#1E5BC6", "rbr");
        Team mclaren  = team("McLaren",         "MCL", "British",  "#FF8000", "mcl");
        Team aston    = team("Aston Martin",    "AST", "British",  "#229971", "ast");
        Team alpine   = team("Alpine",          "ALP", "French",   "#0093CC", "alp");
        Team williams = team("Williams",        "WIL", "British",  "#64C4FF", "wil");
        Team haas     = team("Haas",            "HAS", "American", "#E6E8EB", "haas");
        Team rb       = team("Racing Bulls",    "RB",  "Italian",  "#6692FF", "rb");
        Team audi     = team("Audi",            "AUD", "German",   "#8C96A8", "audi");
        Team cadillac = team("Cadillac",        "CAD", "American", "#B8860B", "cad");

        // ---- Drivers: the 2026 line-ups (headshot files in /static/img/drivers, keyed by code) ----
        List<Driver> grid = new ArrayList<>();
        Driver rus = add(grid, driver("RUS", "George Russell",       63, "British",       mercedes));
        Driver ant = add(grid, driver("ANT", "Andrea K. Antonelli",  12, "Italian",       mercedes));
        Driver ham = add(grid, driver("HAM", "Lewis Hamilton",       44, "British",       ferrari));
        Driver lec = add(grid, driver("LEC", "Charles Leclerc",      16, "Monegasque",    ferrari));
        Driver ver = add(grid, driver("VER", "Max Verstappen",        3, "Dutch",         redBull));
        Driver had = add(grid, driver("HAD", "Isack Hadjar",          6, "French",        redBull));
        Driver nor = add(grid, driver("NOR", "Lando Norris",          1, "British",       mclaren));
        Driver pia = add(grid, driver("PIA", "Oscar Piastri",        81, "Australian",    mclaren));
        Driver alo = add(grid, driver("ALO", "Fernando Alonso",      14, "Spanish",       aston));
        Driver str = add(grid, driver("STR", "Lance Stroll",         18, "Canadian",      aston));
        Driver gas = add(grid, driver("GAS", "Pierre Gasly",         10, "French",        alpine));
        Driver col = add(grid, driver("COL", "Franco Colapinto",     43, "Argentine",     alpine));
        Driver alb = add(grid, driver("ALB", "Alexander Albon",      23, "Thai",          williams));
        Driver sai = add(grid, driver("SAI", "Carlos Sainz",         55, "Spanish",       williams));
        Driver oco = add(grid, driver("OCO", "Esteban Ocon",         31, "French",        haas));
        Driver bea = add(grid, driver("BEA", "Oliver Bearman",       87, "British",       haas));
        Driver law = add(grid, driver("LAW", "Liam Lawson",          30, "New Zealander", rb));
        Driver lin = add(grid, driver("LIN", "Arvid Lindblad",       41, "British",       rb));
        Driver hul = add(grid, driver("HUL", "Nico Hulkenberg",      27, "German",        audi));
        Driver bor = add(grid, driver("BOR", "Gabriel Bortoleto",     5, "Brazilian",     audi));
        Driver per = add(grid, driver("PER", "Sergio Perez",         11, "Mexican",       cadillac));
        Driver bot = add(grid, driver("BOT", "Valtteri Bottas",      77, "Finnish",       cadillac));

        // ---- Circuits (SVG track maps live in /static/img/tracks) ----
        Circuit bahrain     = circuit("bahrain",     "Bahrain International Circuit",    "Bahrain",        "BH", "Sakhir",      5.412, 57);
        Circuit jeddah      = circuit("jeddah",      "Jeddah Corniche Circuit",          "Saudi Arabia",   "SA", "Jeddah",      6.174, 50);
        Circuit suzuka      = circuit("suzuka",      "Suzuka International Racing Course","Japan",         "JP", "Suzuka",      5.807, 53);
        Circuit monaco      = circuit("monaco",      "Circuit de Monaco",                "Monaco",         "MC", "Monte Carlo", 3.337, 78);
        Circuit barcelona   = circuit("barcelona",   "Circuit de Barcelona-Catalunya",   "Spain",          "ES", "Barcelona",   4.657, 66);
        Circuit silverstone = circuit("silverstone", "Silverstone Circuit",              "United Kingdom", "GB", "Silverstone", 5.891, 52);
        Circuit spa         = circuit("spa",         "Circuit de Spa-Francorchamps",     "Belgium",        "BE", "Spa",         7.004, 44);
        Circuit monza       = circuit("monza",       "Autodromo Nazionale Monza",        "Italy",          "IT", "Monza",       5.793, 53);

        // ---- 2026 Race calendar across all 8 circuits (all 8 seeded with full Qualifying + Race data) ----
        Race r1 = race(1, "Japanese GP",             suzuka,      LocalDate.of(2026, 3, 29), true);
        Race r2 = race(2, "Bahrain GP",              bahrain,     LocalDate.of(2026, 4, 12), true);
        Race r3 = race(3, "Saudi Arabian GP",        jeddah,      LocalDate.of(2026, 4, 19), true);
        Race r4 = race(4, "Monaco GP",               monaco,      LocalDate.of(2026, 6,  7), true);
        Race r5 = race(5, "Barcelona-Catalunya GP",  barcelona,   LocalDate.of(2026, 6, 14), true);
        Race r6 = race(6, "British GP",              silverstone, LocalDate.of(2026, 7,  5), true);
        Race r7 = race(7, "Belgian GP",              spa,         LocalDate.of(2026, 7, 19), true);
        Race r8 = race(8, "Italian GP",              monza,       LocalDate.of(2026, 9,  6), true);

        // ---- Simulated weekends (qualifying + race for all 8 circuits) -> also feeds the standings ----
        Map<Long, Integer> driverPoints = new HashMap<>();
        Map<Long, Integer> driverWins = new HashMap<>();
        simulateWeekend(r1, "suzuka",      grid, driverPoints, driverWins);
        simulateWeekend(r2, "bahrain",     grid, driverPoints, driverWins);
        simulateWeekend(r3, "jeddah",      grid, driverPoints, driverWins);
        simulateWeekend(r4, "monaco",      grid, driverPoints, driverWins);
        simulateWeekend(r5, "barcelona",   grid, driverPoints, driverWins);
        simulateWeekend(r6, "silverstone", grid, driverPoints, driverWins);
        simulateWeekend(r7, "spa",         grid, driverPoints, driverWins);
        simulateWeekend(r8, "monza",       grid, driverPoints, driverWins);

        saveStandings(grid, driverPoints, driverWins);

        System.out.println("APEX F1: 2026 dataset seeded (11 teams, 22 drivers, 8 circuits, 8 races, "
                + "8 complete race weekends with full lap/telemetry data).");
    }

    // =====================================================================
    //  Simulated race weekend
    // =====================================================================

    private void simulateWeekend(Race race, String slug, List<Driver> grid,
                                 Map<Long, Integer> driverPoints, Map<Long, Integer> driverWins) {
        Circuit circuit = race.getCircuit();
        CircuitProfile profile = profile(slug);
        double lengthKm = circuit.getLengthKm();
        int raceLaps = circuit.getTotalLaps();
        int nd = grid.size();
        Random rnd = new Random(7919L * race.getRound() + 31L);

        // per-weekend pace of every driver, in seconds off the reference lap
        double[] paceSec = new double[nd];
        for (int i = 0; i < nd; i++) {
            Driver d = grid.get(i);
            paceSec[i] = teamPace(d.getTeam().getShortName()) + driverOffset(d.getCode())
                    + (rnd.nextDouble() - 0.5) * 0.8;
        }

        // ---------------- Qualifying ----------------
        Session quali = sessionRepository.save(new Session(null, race, SessionType.QUALIFYING,
                race.getRaceDate().minusDays(1).atTime(15, 0), 3));

        long[][] qMs = new long[nd][4];
        long[] bestQ = new long[nd];
        int[] bestQLap = new int[nd];
        double[] warmUp = {0.0, 420.0, 140.0, 0.0};
        for (int i = 0; i < nd; i++) {
            bestQ[i] = Long.MAX_VALUE;
            for (int lap = 1; lap <= 3; lap++) {
                double t = profile.baseLapMs - 1300.0 + paceSec[i] * 1000.0 + warmUp[lap]
                        + (rnd.nextDouble() + rnd.nextDouble() - 1.0) * 230.0;
                qMs[i][lap] = Math.round(t);
                if (qMs[i][lap] < bestQ[i]) {
                    bestQ[i] = qMs[i][lap];
                    bestQLap[i] = lap;
                }
            }
        }
        int[] gridPos = rankAscending(bestQ);   // gridPos[i] = 1-based qualifying position
        List<LapTime> qualiLaps = new ArrayList<>();
        for (int i = 0; i < nd; i++) {
            for (int lap = 1; lap <= 3; lap++) {
                qualiLaps.add(new LapTime(null, quali, grid.get(i), lap, qMs[i][lap],
                        gridPos[i], Compound.SOFT, false, lap == 1));
            }
        }
        lapTimeRepository.saveAll(qualiLaps);

        List<TelemetryPoint> qualiTelemetry = new ArrayList<>();
        for (int i = 0; i < nd; i++) {
            qualiTelemetry.addAll(telemetryLap(quali, grid.get(i), bestQLap[i], profile,
                    lengthKm, paceSec[i], true, rnd));
        }
        telemetryPointRepository.saveAll(qualiTelemetry);
        weatherSampleRepository.save(weather(quali, profile, rnd, true));

        Driver poleDriver = grid.get(indexOf(gridPos, 1));

        // ---------------- Race ----------------
        Session raceSession = sessionRepository.save(new Session(null, race, SessionType.RACE,
                race.getRaceDate().atTime(15, 0), raceLaps));

        long[][] lapMs = new long[nd][raceLaps + 1];
        long[][] cumulative = new long[nd][raceLaps + 1];
        Compound[][] lapCompound = new Compound[nd][raceLaps + 1];
        boolean[][] lapIsPit = new boolean[nd][raceLaps + 1];
        boolean[][] lapIsOutlier = new boolean[nd][raceLaps + 1];

        for (int i = 0; i < nd; i++) {
            Plan plan = buildPlan(raceLaps, rnd, slug);
            int stint = 0;
            int ageInStint = 0;
            long running = 0L;
            for (int lap = 1; lap <= raceLaps; lap++) {
                Compound c = plan.compounds[stint];
                boolean inLap = stint < plan.stopLaps.length && lap == plan.stopLaps[stint];
                boolean outLap = ageInStint == 0 && stint > 0;

                double t = profile.baseLapMs + paceSec[i] * 1000.0
                        + compoundOffset(c) + degradationPerLap(c) * ageInStint
                        - 45.0 * (lap - 1)
                        + (rnd.nextDouble() + rnd.nextDouble() - 1.0) * 260.0;
                if (lap == 1) t += 3800.0 + (gridPos[i] - 1) * 85.0;   // standing start, grid order
                if (inLap) t += 21000.0;                                // pit-lane time loss
                if (outLap) t += 2500.0;                                // cold tyres

                long ms = Math.round(t);
                lapMs[i][lap] = ms;
                running += ms;
                cumulative[i][lap] = running;
                lapCompound[i][lap] = c;
                lapIsPit[i][lap] = inLap;
                lapIsOutlier[i][lap] = (lap == 1) || inLap || outLap;

                if (inLap) {
                    stint++;
                    ageInStint = 0;
                } else {
                    ageInStint++;
                }
            }
        }

        // running order after every lap
        int[][] posAt = new int[nd][raceLaps + 1];
        for (int lap = 1; lap <= raceLaps; lap++) {
            long[] column = new long[nd];
            for (int i = 0; i < nd; i++) column[i] = cumulative[i][lap];
            int[] ranks = rankAscending(column);
            for (int i = 0; i < nd; i++) posAt[i][lap] = ranks[i];
        }

        List<LapTime> raceLapRows = new ArrayList<>();
        int[] fastestLapNo = new int[nd];
        for (int i = 0; i < nd; i++) {
            long best = Long.MAX_VALUE;
            for (int lap = 1; lap <= raceLaps; lap++) {
                raceLapRows.add(new LapTime(null, raceSession, grid.get(i), lap, lapMs[i][lap],
                        posAt[i][lap], lapCompound[i][lap], lapIsPit[i][lap], lapIsOutlier[i][lap]));
                if (!lapIsOutlier[i][lap] && lapMs[i][lap] < best) {
                    best = lapMs[i][lap];
                    fastestLapNo[i] = lap;
                }
            }
            if (fastestLapNo[i] == 0) fastestLapNo[i] = 2;
        }
        lapTimeRepository.saveAll(raceLapRows);

        List<TelemetryPoint> raceTelemetry = new ArrayList<>();
        for (int i = 0; i < nd; i++) {
            raceTelemetry.addAll(telemetryLap(raceSession, grid.get(i), fastestLapNo[i], profile,
                    lengthKm, paceSec[i], false, rnd));
        }
        telemetryPointRepository.saveAll(raceTelemetry);
        weatherSampleRepository.save(weather(raceSession, profile, rnd, false));

        // ---------------- Results -> points, winner, pole ----------------
        int[] finalPos = posAt_last(posAt, raceLaps, nd);
        Driver winner = null;
        for (int i = 0; i < nd; i++) {
            int p = finalPos[i];
            if (p >= 1 && p <= POINTS.length) {
                Long id = grid.get(i).getId();
                driverPoints.put(id, getOrZero(driverPoints, id) + POINTS[p - 1]);
            }
            if (p == 1) {
                winner = grid.get(i);
                Long id = winner.getId();
                driverWins.put(id, getOrZero(driverWins, id) + 1);
            }
        }
        race.setActualPoleDriver(poleDriver);
        race.setActualWinnerDriver(winner);
        raceRepository.save(race);
    }

    private static int[] posAt_last(int[][] posAt, int laps, int nd) {
        int[] out = new int[nd];
        for (int i = 0; i < nd; i++) out[i] = posAt[i][laps];
        return out;
    }

    /** rank[i] = 1-based position of values[i] when sorted ascending (smallest = 1). */
    private static int[] rankAscending(long[] values) {
        int n = values.length;
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            int r = 1;
            for (int j = 0; j < n; j++) {
                if (values[j] < values[i] || (values[j] == values[i] && j < i)) r++;
            }
            rank[i] = r;
        }
        return rank;
    }

    private static int indexOf(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) return i;
        }
        return 0;
    }

    private static int getOrZero(Map<Long, Integer> map, Long key) {
        Integer v = map.get(key);
        return v == null ? 0 : v;
    }

    // =====================================================================
    //  Tyre / strategy model
    // =====================================================================

    private static final class Plan {
        int[] stopLaps;
        Compound[] compounds;
    }

    private Plan buildPlan(int laps, Random rnd, String slug) {
        Compound[] pool = {Compound.SOFT, Compound.MEDIUM, Compound.HARD};
        Plan p = new Plan();
        boolean twoStop = rnd.nextDouble() < 0.30 && !"monaco".equals(slug);
        Compound first = pool[rnd.nextInt(3)];
        Compound second = pool[(first.ordinal() + 1 + rnd.nextInt(2)) % 3];
        if (!twoStop) {
            int stop = (int) Math.round(laps * (0.34 + rnd.nextDouble() * 0.14));
            p.stopLaps = new int[]{stop};
            p.compounds = new Compound[]{first, second};
        } else {
            int s1 = (int) Math.round(laps * (0.24 + rnd.nextDouble() * 0.08));
            int s2 = (int) Math.round(laps * (0.58 + rnd.nextDouble() * 0.10));
            Compound third = pool[rnd.nextInt(3)];
            p.stopLaps = new int[]{s1, s2};
            p.compounds = new Compound[]{first, second, third};
        }
        return p;
    }

    private static double compoundOffset(Compound c) {
        if (c == Compound.SOFT) return -450.0;
        if (c == Compound.HARD) return 350.0;
        return 0.0;
    }

    private static double degradationPerLap(Compound c) {
        if (c == Compound.SOFT) return 110.0;
        if (c == Compound.HARD) return 55.0;
        return 78.0;
    }

    private static double teamPace(String shortName) {
        switch (shortName) {
            case "MER": return 0.00;
            case "MCL": return 0.08;
            case "FER": return 0.12;
            case "RBR": return 0.20;
            case "WIL": return 0.50;
            case "RB":  return 0.60;
            case "ALP": return 0.70;
            case "HAS": return 0.72;
            case "AST": return 0.85;
            case "AUD": return 0.92;
            case "CAD": return 1.20;
            default:    return 1.00;
        }
    }

    /** Stable +-0.25 s per-driver offset derived from the driver code. */
    private static double driverOffset(String code) {
        int h = 0;
        for (int i = 0; i < code.length(); i++) h = h * 31 + code.charAt(i);
        return ((h & 0xFF) / 255.0 - 0.5) * 0.5;
    }

    // =====================================================================
    //  Telemetry model (speed / throttle / brake / gear / rpm around one lap)
    // =====================================================================

    private List<TelemetryPoint> telemetryLap(Session session, Driver driver, int lapNumber,
                                              CircuitProfile p, double lengthKm, double paceSec,
                                              boolean quali, Random rnd) {
        int n = TELEMETRY_SAMPLES;
        double lapLenM = lengthKm * 1000.0;
        double vmax = p.vmaxKph * (1.0 - paceSec * 0.0030) * (quali ? 1.012 : 1.0);

        double[] v = new double[n];
        for (int i = 0; i < n; i++) {
            double x = (double) i / n;
            double speed = vmax;
            for (double[] corner : p.corners) {
                double minV = corner[1] * (1.0 - paceSec * 0.0035) * (quali ? 1.015 : 1.0);
                double depth = vmax - minV;
                if (depth <= 0.0) continue;
                double brakeZone = 0.020 + 0.050 * (depth / vmax);
                double accelZone = 0.030 + 0.080 * (depth / vmax);
                double dx = x - corner[0];
                if (dx > 0.5) dx -= 1.0;
                if (dx < -0.5) dx += 1.0;
                double vc;
                if (dx < 0.0) {
                    double u = -dx / brakeZone;
                    vc = (u >= 1.0) ? vmax : minV + depth * Math.pow(u, 1.7);
                } else {
                    double t = dx / accelZone;
                    vc = (t >= 1.0) ? vmax : minV + depth * (1.0 - Math.pow(1.0 - t, 1.8));
                }
                if (vc < speed) speed = vc;
            }
            speed += (rnd.nextDouble() - 0.5) * 2.4;
            v[i] = Math.max(55.0, Math.min(vmax + 2.0, speed));
        }

        List<TelemetryPoint> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double speed = v[i];
            double dv = v[(i + 1) % n] - speed;
            double brake = 0.0;
            double throttle;
            if (dv < -6.0) {
                brake = 1.0;
                throttle = 0.0;
            } else if (speed > vmax * 0.975) {
                throttle = 100.0;
            } else if (dv < -1.5) {
                throttle = 8.0;
            } else {
                double accel = Math.max(0.0, Math.min(1.0, dv / 14.0));
                throttle = 38.0 + 62.0 * accel;
            }
            if (throttle > 0.0 && throttle < 100.0) {
                throttle += (rnd.nextDouble() - 0.5) * 6.0;
            }
            throttle = Math.max(0.0, Math.min(100.0, Math.round(throttle)));

            int gear = gearFor(speed);
            double lo = GEAR_EDGES[gear - 1];
            double hi = GEAR_EDGES[gear];
            double frac = Math.max(0.0, Math.min(1.0, (speed - lo) / (hi - lo)));
            int rpm = (int) Math.round(8400.0 + frac * 3900.0 + (rnd.nextDouble() - 0.5) * 120.0);

            TelemetryPoint point = new TelemetryPoint();
            point.setSession(session);
            point.setDriver(driver);
            point.setLapNumber(lapNumber);
            point.setSampleIndex(i);
            point.setDistanceM(Math.round(i * lapLenM / n * 10.0) / 10.0);
            point.setSpeedKph(Math.round(speed * 10.0) / 10.0);
            point.setThrottlePct(throttle);
            point.setBrake(brake);
            point.setGear(gear);
            point.setRpm(rpm);
            out.add(point);
        }
        return out;
    }

    private static int gearFor(double kph) {
        if (kph < 62) return 1;
        if (kph < 98) return 2;
        if (kph < 135) return 3;
        if (kph < 172) return 4;
        if (kph < 212) return 5;
        if (kph < 252) return 6;
        if (kph < 292) return 7;
        return 8;
    }

    private WeatherSample weather(Session session, CircuitProfile p, Random rnd, boolean quali) {
        double air = p.airTempC + (quali ? -1.5 : 0.0) + (rnd.nextDouble() - 0.5) * 2.0;
        WeatherSample w = new WeatherSample();
        w.setSession(session);
        w.setAirTempC(Math.round(air * 10.0) / 10.0);
        w.setTrackTempC(Math.round((air + 11.0 + rnd.nextDouble() * 8.0) * 10.0) / 10.0);
        w.setHumidityPct(Math.round((p.humidityPct + (rnd.nextDouble() - 0.5) * 8.0) * 10.0) / 10.0);
        w.setWindSpeedMs(Math.round((1.0 + rnd.nextDouble() * 4.5) * 10.0) / 10.0);
        w.setPressureMb(Math.round((1000.0 + rnd.nextDouble() * 14.0) * 10.0) / 10.0);
        return w;
    }

    // =====================================================================
    //  Per-circuit modelling data (approximate - drives the simulated data only)
    //  corners = { position on lap (0..1), minimum speed km/h }
    // =====================================================================

    private static final class CircuitProfile {
        final long baseLapMs;
        final double vmaxKph;
        final double airTempC;
        final double humidityPct;
        final double[][] corners;

        CircuitProfile(long baseLapMs, double vmaxKph, double airTempC, double humidityPct, double[][] corners) {
            this.baseLapMs = baseLapMs;
            this.vmaxKph = vmaxKph;
            this.airTempC = airTempC;
            this.humidityPct = humidityPct;
            this.corners = corners;
        }
    }

    private static CircuitProfile profile(String slug) {
        switch (slug) {
            case "bahrain":
                return new CircuitProfile(92000L, 322.0, 29.0, 38.0, new double[][]{
                        {0.05, 78}, {0.08, 150}, {0.11, 112}, {0.17, 86}, {0.23, 195}, {0.27, 150},
                        {0.31, 205}, {0.37, 112}, {0.43, 142}, {0.62, 72}, {0.70, 182}, {0.78, 122},
                        {0.85, 150}, {0.93, 96}});
            case "jeddah":
                return new CircuitProfile(90500L, 335.0, 31.0, 55.0, new double[][]{
                        {0.04, 95}, {0.07, 160}, {0.12, 232}, {0.16, 190}, {0.20, 242}, {0.26, 262},
                        {0.31, 120}, {0.40, 248}, {0.46, 282}, {0.52, 236}, {0.58, 192}, {0.64, 105},
                        {0.72, 252}, {0.80, 232}, {0.88, 150}, {0.95, 100}});
            case "suzuka":
                return new CircuitProfile(90000L, 325.0, 18.0, 62.0, new double[][]{
                        {0.04, 150}, {0.08, 122}, {0.14, 215}, {0.17, 170}, {0.20, 232}, {0.24, 262},
                        {0.30, 136}, {0.34, 126}, {0.45, 72}, {0.52, 186}, {0.60, 106}, {0.80, 298},
                        {0.93, 86}});
            case "monaco":
                return new CircuitProfile(72500L, 290.0, 21.0, 66.0, new double[][]{
                        {0.04, 70}, {0.10, 95}, {0.16, 150}, {0.20, 110}, {0.27, 80}, {0.31, 45},
                        {0.38, 95}, {0.47, 265}, {0.55, 120}, {0.62, 130}, {0.68, 95}, {0.78, 115},
                        {0.87, 85}, {0.94, 55}});
            case "barcelona":
                return new CircuitProfile(76000L, 325.0, 24.0, 48.0, new double[][]{
                        {0.05, 95}, {0.10, 130}, {0.15, 96}, {0.24, 172}, {0.30, 240}, {0.38, 160},
                        {0.46, 210}, {0.54, 95}, {0.70, 250}, {0.78, 170}, {0.86, 140}, {0.93, 86}});
            case "silverstone":
                return new CircuitProfile(87000L, 325.0, 20.0, 60.0, new double[][]{
                        {0.05, 285}, {0.10, 96}, {0.16, 186}, {0.22, 110}, {0.30, 260}, {0.38, 116},
                        {0.44, 180}, {0.52, 256}, {0.58, 262}, {0.66, 96}, {0.74, 240}, {0.84, 196},
                        {0.90, 100}});
            case "spa":
                return new CircuitProfile(104000L, 335.0, 17.0, 70.0, new double[][]{
                        {0.03, 75}, {0.08, 250}, {0.18, 285}, {0.26, 130}, {0.31, 186}, {0.37, 150},
                        {0.46, 96}, {0.52, 260}, {0.58, 186}, {0.70, 232}, {0.78, 300}, {0.90, 290},
                        {0.95, 110}});
            case "monza":
                return new CircuitProfile(81000L, 345.0, 25.0, 52.0, new double[][]{
                        {0.06, 70}, {0.09, 110}, {0.24, 265}, {0.31, 95}, {0.40, 186}, {0.46, 176},
                        {0.60, 80}, {0.65, 140}, {0.92, 205}});
            default:
                return new CircuitProfile(90000L, 320.0, 24.0, 50.0, new double[][]{
                        {0.10, 100}, {0.30, 150}, {0.50, 120}, {0.70, 180}, {0.90, 110}});
        }
    }

    // =====================================================================
    //  Standings (computed from the simulated race results)
    // =====================================================================

    private void saveStandings(List<Driver> grid, Map<Long, Integer> pts, Map<Long, Integer> wins) {
        List<Driver> order = new ArrayList<>(grid);
        order.sort((a, b) -> {
            int pa = getOrZero(pts, a.getId());
            int pb = getOrZero(pts, b.getId());
            if (pa != pb) return pb - pa;
            int wa = getOrZero(wins, a.getId());
            int wb = getOrZero(wins, b.getId());
            if (wa != wb) return wb - wa;
            return a.getFullName().compareTo(b.getFullName());
        });
        int position = 1;
        for (Driver d : order) {
            driverStanding(d, (double) getOrZero(pts, d.getId()), position, getOrZero(wins, d.getId()));
            position++;
        }

        Map<Long, Integer> teamPts = new HashMap<>();
        Map<Long, Integer> teamWins = new HashMap<>();
        Map<Long, Team> teams = new HashMap<>();
        for (Driver d : grid) {
            Long tid = d.getTeam().getId();
            teams.put(tid, d.getTeam());
            teamPts.put(tid, getOrZero(teamPts, tid) + getOrZero(pts, d.getId()));
            teamWins.put(tid, getOrZero(teamWins, tid) + getOrZero(wins, d.getId()));
        }
        List<Team> teamOrder = new ArrayList<>(teams.values());
        teamOrder.sort((a, b) -> {
            int pa = getOrZero(teamPts, a.getId());
            int pb = getOrZero(teamPts, b.getId());
            if (pa != pb) return pb - pa;
            int wa = getOrZero(teamWins, a.getId());
            int wb = getOrZero(teamWins, b.getId());
            if (wa != wb) return wb - wa;
            return a.getName().compareTo(b.getName());
        });
        int tp = 1;
        for (Team t : teamOrder) {
            teamStanding(t, (double) getOrZero(teamPts, t.getId()), tp, getOrZero(teamWins, t.getId()));
            tp++;
        }
    }

    // ---- helpers ----
    private Driver add(List<Driver> list, Driver d) {
        list.add(d);
        return d;
    }

    private Team team(String name, String shortName, String nat, String color, String logoSlug) {
        Team t = new Team();
        t.setName(name);
        t.setShortName(shortName);
        t.setNationality(nat);
        t.setColorHex(color);
        t.setLogoUrl("/img/teams/" + logoSlug + ".png");
        return teamRepository.save(t);
    }

    private Driver driver(String code, String fullName, int number, String nat, Team team) {
        String headshot = "/img/drivers/" + code.toLowerCase() + ".png";
        return driverRepository.save(new Driver(null, code, fullName, number, nat, headshot, team));
    }

    private Circuit circuit(String slug, String name, String country, String cc,
                            String city, double km, int laps) {
        Circuit c = new Circuit();
        c.setName(name);
        c.setCountry(country);
        c.setCountryCode(cc);
        c.setCity(city);
        c.setLengthKm(km);
        c.setTotalLaps(laps);
        c.setMapImageUrl("/img/tracks/" + slug + ".svg");
        return circuitRepository.save(c);
    }

    private Race race(int round, String name, Circuit circuit, LocalDate date, boolean completed) {
        Race r = new Race();
        r.setSeason(SEASON);
        r.setRound(round);
        r.setName(name);
        r.setCircuit(circuit);
        r.setRaceDate(date);
        r.setCompleted(completed);
        return raceRepository.save(r);
    }

    private void driverStanding(Driver d, double points, int position, int wins) {
        driverStandingRepository.save(new DriverStanding(null, SEASON, d, points, position, wins));
    }

    private void teamStanding(Team t, double points, int position, int wins) {
        teamStandingRepository.save(new TeamStanding(null, SEASON, t, points, position, wins));
    }
}
