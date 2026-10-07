package com.apexf1.backend.seed;

import com.apexf1.backend.model.*;
import com.apexf1.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Loads a full 2026-style dataset on first boot: 11 teams, 22 drivers (each
 * with a headshot image), 8 circuits (each with a track-map image), an
 * 8-round calendar, championship standings, and detailed session data
 * (lap times / telemetry / weather) for the Spanish GP so the Pit Wall works.
 *
 * Skips itself if drivers already exist (e.g. on app restart).
 */
@Component
public class SeedDataRunner implements CommandLineRunner {

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

    public SeedDataRunner(TeamRepository teamRepository,
                           DriverRepository driverRepository,
                           CircuitRepository circuitRepository,
                           RaceRepository raceRepository,
                           SessionRepository sessionRepository,
                           LapTimeRepository lapTimeRepository,
                           TelemetryPointRepository telemetryPointRepository,
                           WeatherSampleRepository weatherSampleRepository,
                           DriverStandingRepository driverStandingRepository,
                           TeamStandingRepository teamStandingRepository) {
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
    }

    @Override
    public void run(String... args) {
        if (driverRepository.count() > 0) {
            return; // already seeded
        }

        // ---- Teams (logo files live in /static/img/teams) ----
        Team mercedes = team("Mercedes",        "MER", "German",   "#00D2A0", "mer");
        Team ferrari  = team("Ferrari",         "FER", "Italian",  "#E10600", "fer");
        Team redBull  = team("Red Bull Racing", "RBR", "Austrian", "#1E5BC6", "rbr");
        Team mclaren  = team("McLaren",         "MCL", "British",  "#FF8000", "mcl");
        Team aston    = team("Aston Martin",    "AST", "British",  "#229971", "ast");
        Team alpine   = team("Alpine",          "ALP", "French",   "#0093CC", "alp");
        Team williams = team("Williams",        "WIL", "British",  "#64C4FF", "wil");
        Team haas     = team("Haas",            "HAS", "American", "#B6BABD", "haas");
        Team rb       = team("Racing Bulls",    "RB",  "Italian",  "#6692FF", "rb");
        Team sauber   = team("Kick Sauber",     "SAU", "Swiss",    "#52E252", "sau");
        Team cadillac = team("Cadillac",        "CAD", "American", "#B8860B", "cad");

        // ---- Drivers (headshot files live in /static/img/drivers, keyed by code) ----
        Driver rus = driver("RUS", "George Russell",     63, "British",   mercedes);
        Driver ant = driver("ANT", "Andrea K. Antonelli",12, "Italian",   mercedes);
        Driver ham = driver("HAM", "Lewis Hamilton",     44, "British",   ferrari);
        Driver lec = driver("LEC", "Charles Leclerc",    16, "Monegasque",ferrari);
        Driver ver = driver("VER", "Max Verstappen",      1, "Dutch",     redBull);
        Driver tsu = driver("TSU", "Yuki Tsunoda",       22, "Japanese",  redBull);
        Driver nor = driver("NOR", "Lando Norris",        4, "British",   mclaren);
        Driver pia = driver("PIA", "Oscar Piastri",      81, "Australian",mclaren);
        Driver str = driver("STR", "Lance Stroll",       18, "Canadian",  aston);
        Driver per = driver("PER", "Sergio Perez",       11, "Mexican",   aston);
        Driver gas = driver("GAS", "Pierre Gasly",       10, "French",    alpine);
        Driver col = driver("COL", "Franco Colapinto",   43, "Argentine", alpine);
        Driver alb = driver("ALB", "Alexander Albon",    23, "Thai",      williams);
        Driver sai = driver("SAI", "Carlos Sainz",       55, "Spanish",   williams);
        Driver oco = driver("OCO", "Esteban Ocon",       31, "French",    haas);
        Driver bea = driver("BEA", "Oliver Bearman",     87, "British",   haas);
        Driver had = driver("HAD", "Isack Hadjar",        6, "French",    rb);
        Driver law = driver("LAW", "Liam Lawson",        30, "New Zealander", rb);
        Driver hul = driver("HUL", "Nico Hulkenberg",    27, "German",    sauber);
        Driver bor = driver("BOR", "Gabriel Bortoleto",   5, "Brazilian", sauber);
        Driver bot = driver("BOT", "Valtteri Bottas",    77, "Finnish",   cadillac);
        Driver zho = driver("ZHO", "Zhou Guanyu",        24, "Chinese",   cadillac);

        // ---- Circuits (track-map files live in /static/img/tracks) ----
        Circuit bahrain     = circuit("bahrain",     "Bahrain International Circuit",   "Bahrain",       "BH", "Sakhir",      5.412, 57);
        Circuit jeddah      = circuit("jeddah",      "Jeddah Corniche Circuit",         "Saudi Arabia",  "SA", "Jeddah",      6.174, 50);
        Circuit suzuka      = circuit("suzuka",      "Suzuka International Racing Course","Japan",        "JP", "Suzuka",      5.807, 53);
        Circuit monaco      = circuit("monaco",      "Circuit de Monaco",               "Monaco",        "MC", "Monte Carlo", 3.337, 78);
        Circuit barcelona   = circuit("barcelona",   "Circuit de Barcelona-Catalunya",  "Spain",         "ES", "Barcelona",   4.657, 66);
        Circuit silverstone = circuit("silverstone", "Silverstone Circuit",             "United Kingdom","GB", "Silverstone", 5.891, 52);
        Circuit spa         = circuit("spa",         "Circuit de Spa-Francorchamps",    "Belgium",       "BE", "Spa",         7.004, 44);
        Circuit monza       = circuit("monza",       "Autodromo Nazionale Monza",       "Italy",         "IT", "Monza",       5.793, 53);

        // ---- Race calendar (rounds 1-8) ----
        race(1, "Bahrain GP",        bahrain,     LocalDate.of(2026, 3,  8), true,  ver, ver);
        race(2, "Saudi Arabian GP",  jeddah,      LocalDate.of(2026, 3, 22), true,  nor, pia);
        race(3, "Japanese GP",       suzuka,      LocalDate.of(2026, 4,  5), true,  nor, nor);
        race(4, "Monaco GP",         monaco,      LocalDate.of(2026, 5, 24), true,  lec, nor);
        Race spanishGp =
        race(5, "Spanish GP",        barcelona,   LocalDate.of(2026, 6,  7), true,  rus, rus);
        race(6, "British GP",        silverstone, LocalDate.of(2026, 7,  5), false, null, null);
        race(7, "Belgian GP",        spa,         LocalDate.of(2026, 7, 26), false, null, null);
        race(8, "Italian GP",        monza,       LocalDate.of(2026, 9,  6), false, null, null);

        // ---- Sessions for the Spanish GP ----
        Session qualifying = sessionRepository.save(
                new Session(null, spanishGp, SessionType.QUALIFYING, LocalDateTime.of(2026, 6, 6, 15, 0), 20));
        Session raceSession = sessionRepository.save(
                new Session(null, spanishGp, SessionType.RACE, LocalDateTime.of(2026, 6, 7, 15, 0), 66));

        // ---- Lap times (race session), a few laps for the front-runners ----
        lapTimeRepository.save(new LapTime(null, raceSession, rus, 63, 81200L, 1, Compound.HARD, false, false));
        lapTimeRepository.save(new LapTime(null, raceSession, rus, 64, 80980L, 1, Compound.HARD, false, false));
        lapTimeRepository.save(new LapTime(null, raceSession, rus, 65, 87127L, 1, Compound.HARD, false, false));
        lapTimeRepository.save(new LapTime(null, raceSession, nor, 63, 81410L, 2, Compound.MEDIUM, false, false));
        lapTimeRepository.save(new LapTime(null, raceSession, nor, 64, 81250L, 2, Compound.MEDIUM, false, false));
        lapTimeRepository.save(new LapTime(null, raceSession, nor, 65, 81190L, 2, Compound.MEDIUM, false, false));
        lapTimeRepository.save(new LapTime(null, raceSession, ver, 63, 81650L, 3, Compound.HARD, false, false));
        lapTimeRepository.save(new LapTime(null, raceSession, ver, 64, 81420L, 3, Compound.HARD, false, false));
        lapTimeRepository.save(new LapTime(null, raceSession, ver, 65, 81390L, 3, Compound.HARD, false, false));

        // ---- Telemetry: RUS, lap 43 fastest lap, ~10 samples across the lap ----
        double[] speed    = {331, 300, 260, 210, 163, 175, 220, 270, 310, 331};
        double[] throttle = {100, 90, 60, 20, 0, 15, 55, 85, 100, 100};
        double[] brake    = {0, 0, 0, 1, 1, 0, 0, 0, 0, 0};
        int[] gear        = {8, 7, 6, 4, 3, 4, 5, 6, 7, 8};
        int[] rpm         = {11400, 11000, 10200, 9000, 8200, 9200, 10400, 11000, 11300, 11400};
        for (int i = 0; i < speed.length; i++) {
            TelemetryPoint point = new TelemetryPoint();
            point.setSession(raceSession);
            point.setDriver(rus);
            point.setLapNumber(43);
            point.setSampleIndex(i);
            point.setDistanceM(i * 460.0);
            point.setSpeedKph(speed[i]);
            point.setThrottlePct(throttle[i]);
            point.setBrake(brake[i]);
            point.setGear(gear[i]);
            point.setRpm(rpm[i]);
            telemetryPointRepository.save(point);
        }

        // ---- Weather (race session) ----
        WeatherSample weather = new WeatherSample();
        weather.setSession(raceSession);
        weather.setAirTempC(30.5);
        weather.setTrackTempC(50.3);
        weather.setHumidityPct(29.0);
        weather.setWindSpeedMs(2.6);
        weather.setPressureMb(1001.0);
        weatherSampleRepository.save(weather);

        // ---- Driver standings (points, position, wins) ----
        driverStanding(nor, 310.0, 1, 8);
        driverStanding(pia, 285.0, 2, 6);
        driverStanding(ver, 276.0, 3, 5);
        driverStanding(rus, 240.0, 4, 2);
        driverStanding(lec, 210.0, 5, 1);
        driverStanding(ham, 190.0, 6, 0);
        driverStanding(ant, 150.0, 7, 0);
        driverStanding(sai, 120.0, 8, 0);
        driverStanding(alb,  95.0, 9, 0);
        driverStanding(gas,  80.0, 10, 0);
        driverStanding(had,  55.0, 11, 0);
        driverStanding(oco,  45.0, 12, 0);
        driverStanding(tsu,  40.0, 13, 0);
        driverStanding(bea,  35.0, 14, 0);
        driverStanding(hul,  30.0, 15, 0);
        driverStanding(str,  26.0, 16, 0);
        driverStanding(law,  20.0, 17, 0);
        driverStanding(col,  14.0, 18, 0);
        driverStanding(bor,  10.0, 19, 0);
        driverStanding(per,   8.0, 20, 0);
        driverStanding(bot,   4.0, 21, 0);
        driverStanding(zho,   2.0, 22, 0);

        // ---- Constructor standings (sum of each team's two drivers) ----
        teamStanding(mclaren,  595.0, 1, 14);
        teamStanding(ferrari,  400.0, 2, 1);
        teamStanding(mercedes, 390.0, 3, 2);
        teamStanding(redBull,  316.0, 4, 5);
        teamStanding(williams, 215.0, 5, 0);
        teamStanding(alpine,    94.0, 6, 0);
        teamStanding(haas,      80.0, 7, 0);
        teamStanding(rb,        75.0, 8, 0);
        teamStanding(sauber,    40.0, 9, 0);
        teamStanding(aston,     34.0, 10, 0);
        teamStanding(cadillac,   6.0, 11, 0);

        System.out.println("APEX F1: full dataset seeded (11 teams, 22 drivers, 8 circuits, 8 races).");
    }

    // ---- helpers ----
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
        c.setMapImageUrl("/img/tracks/" + slug + ".png");
        return circuitRepository.save(c);
    }

    private Race race(int round, String name, Circuit circuit, LocalDate date,
                      boolean completed, Driver pole, Driver winner) {
        Race r = new Race();
        r.setSeason(2026);
        r.setRound(round);
        r.setName(name);
        r.setCircuit(circuit);
        r.setRaceDate(date);
        r.setCompleted(completed);
        r.setActualPoleDriver(pole);
        r.setActualWinnerDriver(winner);
        return raceRepository.save(r);
    }

    private void driverStanding(Driver d, double points, int position, int wins) {
        driverStandingRepository.save(new DriverStanding(null, 2026, d, points, position, wins));
    }

    private void teamStanding(Team t, double points, int position, int wins) {
        teamStandingRepository.save(new TeamStanding(null, 2026, t, points, position, wins));
    }
}
