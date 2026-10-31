# APEX F1 — 2026 Update & Bug-Fix Notes

This archive contains the complete full-stack project: **Java 17 + Spring Boot 3.3.4** backend + static frontend (`src/main/resources/static/`).
Set your MySQL credentials in `src/main/resources/application.properties`, run `mvn spring-boot:run`, and open `http://localhost:8080`.

## Fixes Applied in This Build

1. **Millisecond-Accurate Distinct Lap Times in Race Sim (`CarRunner.java`, `CarSimState.java`, `BalancedPaceStrategy.java`, `SimulationEngine.java`)**:
   - Fixed the tick quantization bug where every 100 ms tick added a flat `1000 ms / 2000 ms / 4000 ms` block to `lapModelMs` without sub-tick finish-line interpolation, which previously caused drivers crossing on the same tick to show identical `.000` lap times.
   - Added exact finish-line crossing interpolation (`fractionBeforeLine`), overshoot time carry-over into the next lap, circuit-specific reference speeds (Monaco ~73.2s vs Spa ~104.5s vs Monza ~81.2s), 2026 team/driver pace hierarchy, compound speed deltas (`SOFT`, `MEDIUM`, `HARD`), staggered pit-stop windows (34%–62% of race distance), and realistic pit-lane time loss (~20–22s on pit laps).
2. **Full Circuit Lap Counts & Realistic Simulation Speeds (`race.html`, `SimulationEngine.java`)**:
   - Added **Full Race Distance** (`Full Race (53 laps)`, `Full Race (57 laps)`, `Full Race (78 laps)`, etc., dynamically matched to the selected circuit) as the default in Race Sim alongside `5, 10, 20, 35, 50` lap options.
   - Added **Real-Time (x1)**, **Broadcast (x2)**, **Normal (x5 — default)**, **Fast (x10)**, and **Ultra Fast (x20)** simulation speed controls.
3. **Pit Wall Analysis Working Across All 8 Circuits (`SeedDataRunner.java`, `pitwall.html`)**:
   - Seeded complete `QUALIFYING` and `RACE` sessions, lap times, tyre stints, weather samples, and 80-point telemetry laps across **all 8 circuits** (`Suzuka`, `Bahrain`, `Jeddah`, `Monaco`, `Barcelona-Catalunya`, `Silverstone`, `Spa-Francorchamps`, and `Monza`) so Pit Wall works on every track.
4. **Automatic Database Refresh & Resilient Track Loader (`SeedDataRunner.java`, `js/track.js`, `DriverService.java`, `TeamService.java`)**:
   - Upgraded `SeedDataRunner.needsRefresh()` to automatically detect databases with fewer than 16 sessions (`< 8` completed race weekends) or legacy `.png` map URLs and rebuild cleanly on startup.
   - Added `.png -> .svg` auto-recovery and an inline SVG fallback circuit in `js/track.js` so custom races always animate in Race Sim.
   - Fixed `DriverService.update()` and `TeamService.update()` so editing drivers or teams in `manage.html` preserves existing `headshotUrl` and `logoUrl` values.

5. **WebSocket Live Telemetry (`WebSocketConfig.java`, `SimulationWebSocketHandler.java`, `WebSocketTelemetryObserver.java`, `race.html`)**:
   - Added `spring-boot-starter-websocket` and a `WebSocketTelemetryObserver` implementing `TelemetryObserver` to push live `SimulationSnapshot` updates over `ws://localhost:8080/ws/simulations?simId={id}` with automatic HTTP polling fallback.
6. **JUnit 5 Automated Test Suite (`src/test/java/com/apexf1/backend/`)**:
   - Added `SimulationEngineTest.java`, `PredictionServiceTest.java`, and `ServiceLayerTest.java` (run with `mvn test`).
7. **Deployment Packaging (`application-prod.properties`, `Dockerfile`, `docker-compose.yml`, `build-prod.bat`, `run.bat`)**:
   - Added a `prod` Spring profile, multi-stage `Dockerfile`, `docker-compose.yml` (MySQL 8 + Spring Boot), and auto-downloading Maven support in `run.bat` and `build-prod.bat`.
