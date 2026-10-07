package com.apexf1.backend.simulation;

/**
 * A concrete Observer that logs lap completions and the race finish to the
 * console. Demonstrates the Observer pattern; a WebSocket broadcaster can be
 * added later the same way without changing the simulation core.
 */
public class LoggingTelemetryObserver implements TelemetryObserver {

    @Override
    public void onTick(long simulationId, CarSimState car) {
        // Intentionally quiet on ticks (would be very chatty).
    }

    @Override
    public void onLapCompleted(long simulationId, CarSimState car) {
        System.out.printf("[SIM %d] %s completed lap %d (%.3fs) on %s%n",
                simulationId, car.getDriverCode(), car.getCurrentLap() - 1,
                car.getLastLapTimeMs() / 1000.0, car.getCompound());
    }

    @Override
    public void onRaceFinished(long simulationId) {
        System.out.printf("[SIM %d] race finished%n", simulationId);
    }
}
