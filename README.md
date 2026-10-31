# APEX F1 — Full Stack (Backend + Frontend combined)

Spring Boot REST API **and** the web frontend in a single runnable project.
The frontend lives in `src/main/resources/static/`, so Spring Boot serves
it on the same port as the API. No CORS setup, no second server, no
`file://` problems — everything runs from **http://localhost:8080**.

## Stack
- Java 17, Spring Boot 3.3
- Spring Web + Spring Data JPA + MySQL, Lombok
- Vanilla HTML / CSS / JS frontend (served as static resources)

## 1. Prerequisites
- JDK 17+
- Maven (or an IDE like IntelliJ that bundles it)
- MySQL running locally on port 3306

## 2. Configure the database
Copy `src/main/resources/application-local.properties.example` to
`application-local.properties` (same folder) and set your MySQL password:

```
DB_PASSWORD=your_mysql_password
```

That file is git-ignored, so the password is never pushed. You can also set
a `DB_PASSWORD` environment variable instead. Username defaults to `root`.

The `apexf1` database is created automatically on first run, tables are
generated from the entities, and `SeedDataRunner` loads the 2026 dataset
(11 teams, 22 drivers, 8 circuits, 8 races, full simulated lap/telemetry/weather
data for rounds 1-5, and standings computed from it). An older database seeded
with the previous data is rebuilt automatically on the next start.
See `UPDATE_NOTES.md` for everything that changed.

## 3. Run it
```
mvn spring-boot:run
```
Then open **http://localhost:8080** in your browser.

- `/` → redirects to the Dashboard
- `dashboard.html` → live driver & constructor standings
- `pitwall.html` → classification, stint analysis, lap-time/position charts, telemetry charts and weather per session
- `race.html` → starts the multithreaded race simulation and animates the cars on the track
- `fsp.html` → submit / edit / delete fan season predictions
- `manage.html` → full CRUD admin for Drivers, Teams and Races

## 4. How the frontend connects to the backend
Because the pages are served by Spring Boot itself, the JavaScript calls
the API with **relative URLs** (`/api/...` — see `static/js/api.js`).
Same origin → no CORS issues. The API endpoints are unchanged from the
original backend (see the endpoint table below).

**Full CRUD** (Create / Read / Update / Delete) exists for Drivers, Teams,
Races and Predictions — driven from the **Manage** page (`manage.html`).

Drivers
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/drivers` | all drivers |
| GET | `/api/drivers/{id}` | one driver |
| POST | `/api/drivers` | create a driver |
| PUT | `/api/drivers/{id}` | update a driver |
| DELETE | `/api/drivers/{id}` | delete a driver |

Teams
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/teams` · `/api/teams/{id}` | read |
| POST | `/api/teams` | create |
| PUT | `/api/teams/{id}` | update |
| DELETE | `/api/teams/{id}` | delete |

Races
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/races?season=2026` · `/api/races/{id}` | read |
| GET | `/api/races/{id}/sessions` | sessions in a race weekend |
| POST | `/api/races` | create |
| PUT | `/api/races/{id}` | update |
| DELETE | `/api/races/{id}` | delete |

Predictions (FSP)
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/predictions/race/{raceId}` | picks for a race |
| POST | `/api/predictions/race/{raceId}` | submit a pick |
| PUT | `/api/predictions/{id}` | edit a pick |
| DELETE | `/api/predictions/{id}` | delete a pick |
| POST | `/api/predictions/race/{raceId}/score` | score picks |

Read-only data
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/circuits` | all circuits |
| GET | `/api/sessions/{id}/laptimes?driverId=` | lap times |
| GET | `/api/sessions/{id}/telemetry?driverId=&lap=` | telemetry |
| GET | `/api/sessions/{id}/weather` | weather samples |
| GET | `/api/standings/drivers?season=2026` | driver standings |
| GET | `/api/standings/teams?season=2026` | constructor standings |

**Relationship references:** when creating/updating, pass related entities by
id, e.g. a driver's team as `"team": { "id": 2 }`, or a race's circuit as
`"circuit": { "id": 1 }`. The backend resolves them to managed rows.

## 6. Multithreaded Race Simulation Engine

The `com.apexf1.backend.simulation` package is the concurrency + OOP core of
the project (the "Simulation Engine" pipeline stage).

**How it works:** starting a simulation launches **one thread per car**. Each
thread advances its car every 100 ms — asking a pace model for a speed,
moving the car forward, completing laps, and pitting once near half distance —
until it finishes the race or the sim is stopped. A manager thread waits on a
`CountDownLatch` and flips the status to `FINISHED` when every car is done.

**Concurrency building blocks:** `ExecutorService` (thread pool),
`CountDownLatch` (finish coordination), `ConcurrentHashMap` (live sim
registry), `CopyOnWriteArrayList` (observer list), `AtomicLong` (id sequence),
`volatile` fields + `synchronized` lap updates (safe publication to the
snapshot reader).

**Design patterns used:**
- **Strategy** — `PaceStrategy` / `BalancedPaceStrategy`: swap the speed model
  without touching the simulation loop.
- **Observer** — `TelemetryObserver` / `LoggingTelemetryObserver`: the sim
  pushes lap/finish events to listeners; `WebSocketTelemetryObserver` streams
  live snapshots to the browser over WebSocket.
- **Singleton** — `SimulationEngine` (one Spring-managed instance) owns the
  thread pool and all running simulations.

**Endpoints**
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/simulations/race/{raceId}?laps=&timeScale=` | start a sim (all drivers race). `laps` and `timeScale` optional (defaults 10 laps, 20×) |
| GET | `/api/simulations/{simId}` | live snapshot (the Race Sim page gets these pushed over WebSocket) |
| POST | `/api/simulations/{simId}/stop` | stop a running sim |
| GET | `/api/simulations` | list all simulations |
| GET | `/api/simulations/results` | saved results of finished simulations (MySQL) |
| GET | `/api/simulations/results/{id}` | one saved result with full classification |

**Try it (with the app running and DB seeded):**
```
# start a 5-lap simulation of race 1, sped up 30x
curl -X POST "http://localhost:8080/api/simulations/race/1?laps=5&timeScale=30"

# then poll the snapshot (use the simulationId returned above)
curl "http://localhost:8080/api/simulations/1001"
```
The snapshot returns cars sorted by race position, each with live `speedKph`,
`currentLap`, `compound`, `lastLapTimeMs` and `gapToLeaderM`. Watch the
console for `[SIM …] completed lap …` lines from the Observer.

## 7. Real-Time WebSocket Telemetry Streaming

Live simulations now push real-time `SimulationSnapshot` updates directly from the server over WebSocket:
- **Endpoint**: `ws://localhost:8080/ws/simulations?simId={simulationId}` (configured in `config/WebSocketConfig.java` and `simulation/SimulationWebSocketHandler.java`).
- **Observer Pattern Integration**: `WebSocketTelemetryObserver` implements `TelemetryObserver` and registers alongside `LoggingTelemetryObserver` in `SimulationEngine`. It streams live car positions, speeds, tyre compounds, and lap completions to `race.html` (with automatic HTTP polling fallback).

## 8. Automated JUnit 5 Test Suite

Unit and service tests live in `src/test/java/com/apexf1/backend/`:
- `SimulationEngineTest.java` — verifies sub-tick finish-line interpolation (distinct millisecond lap times), `BalancedPaceStrategy` compound pace/degradation/pit-lane loss, and multithreaded `SimulationEngine` start/snapshot/stop lifecycle.
- `PredictionServiceTest.java` — verifies Fan Season Predictions (FSP) submission and 100% / 50% / 0% accuracy scoring.
- `ServiceLayerTest.java` — verifies `DriverService` and `TeamService` update preservation rules.

Run all tests with:
```bash
mvn test
```

## 9. Production Deployment Packaging (Runnable JAR & Docker)

- **Production Profile (`src/main/resources/application-prod.properties`)**: Supports `PORT`, `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables, HikariCP connection pooling, and gzip asset/JSON compression.
- **One-Click Runnable JAR (`build-prod.bat`)**:
  ```bash
  mvn clean package
  java -jar target/apex-f1-backend.jar --spring.profiles.active=prod
  ```
- **Docker & Docker Compose (`Dockerfile`, `docker-compose.yml`)**:
  ```bash
  docker compose up --build
  ```

## 5. Project layout
```
apex-f1/
├── pom.xml
└── src/main/
    ├── java/com/apexf1/backend/   ← REST API (controllers, services, models…)
    └── resources/
        ├── application.properties
        └── static/                ← FRONTEND (served at localhost:8080)
            ├── index.html
            ├── dashboard.html
            ├── pitwall.html
            ├── fsp.html
            ├── manage.html        ← CRUD admin
            ├── css/style.css
            └── js/  (main.js, api.js)
```
