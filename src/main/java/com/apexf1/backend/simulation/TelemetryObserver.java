package com.apexf1.backend.simulation;

/**
 * OBSERVER design pattern.
 * Anything that wants live updates from the simulation (a logger now, a
 * WebSocket broadcaster later) implements this and registers with the
 * RaceSimulation. The simulation pushes events without knowing who listens.
 */
public interface TelemetryObserver {

    /** Called after a car advances on each tick. */
    void onTick(long simulationId, CarSimState car);

    /** Called when a car crosses the line to start a new lap. */
    default void onLapCompleted(long simulationId, CarSimState car) { }

    /** Called once when the whole race simulation finishes. */
    default void onRaceFinished(long simulationId) { }
}
