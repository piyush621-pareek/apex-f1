package com.apexf1.backend.simulation;

import java.util.Random;

/**
 * The unit of concurrency: ONE thread runs ONE car for the whole race.
 * Every {@code TICK_MS} it asks its {@link PaceStrategy} for a speed, moves
 * the car forward, handles lap completion, and notifies observers — until the
 * car finishes the race or the simulation is stopped.
 */
public class CarRunner implements Runnable {

    /** Real milliseconds between ticks. */
    public static final long TICK_MS = 100;

    private final RaceSimulation sim;
    private final CarSimState state;
    private final PaceStrategy strategy;
    private final Random rnd;

    private double lapDistanceM = 0.0;

    public CarRunner(RaceSimulation sim, CarSimState state, PaceStrategy strategy, long seed) {
        this.sim = sim;
        this.state = state;
        this.strategy = strategy;
        this.rnd = new Random(seed);
    }

    @Override
    public void run() {
        try {
            while (sim.isRunning() && !state.isFinished()) {
                Thread.sleep(TICK_MS);

                // model time elapsed this tick = real tick * time compression
                double dtModelSec = (TICK_MS / 1000.0) * sim.getTimeScale();

                double speedKph = strategy.speedKph(state, rnd);
                state.setSpeedKph(speedKph);

                double metres = (speedKph / 3.6) * dtModelSec;
                lapDistanceM += metres;
                state.addDistance(metres);
                state.addLapModelMs((long) (dtModelSec * 1000));

                if (lapDistanceM >= sim.getLapLengthM()) {
                    lapDistanceM -= sim.getLapLengthM();
                    state.completeLap();
                    sim.notifyLap(state);

                    // Simple strategy hook: one stop near half distance.
                    if (state.getCurrentLap() == sim.getTotalLaps() / 2) {
                        state.fitTyres("HARD");
                    }
                    if (state.getCurrentLap() > sim.getTotalLaps()) {
                        state.setFinished(true);
                        state.setSpeedKph(0.0);
                    }
                }

                sim.notifyTick(state);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // cooperative shutdown
        } finally {
            sim.carFinished();
        }
    }
}
