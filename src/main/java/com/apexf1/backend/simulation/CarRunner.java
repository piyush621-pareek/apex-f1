package com.apexf1.backend.simulation;

import java.util.Random;

/**
 * The unit of concurrency: ONE thread runs ONE car for the whole race.
 * Every {@code TICK_MS} it asks its {@link PaceStrategy} for a speed, moves
 * the car forward, interpolates exact finish-line crossing timestamps to the
 * millisecond, handles staggered pit stops, and notifies observers.
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
                double dtModelMs = dtModelSec * 1000.0;

                double speedKph = strategy.speedKph(state, rnd);
                state.setSpeedKph(speedKph);

                double metres = (speedKph / 3.6) * dtModelSec;
                lapDistanceM += metres;
                state.addDistance(metres);

                double lapLen = sim.getLapLengthM();
                if (lapDistanceM >= lapLen && metres > 0.0) {
                    double overshootM = lapDistanceM - lapLen;
                    double fractionBeforeLine = Math.max(0.0, Math.min(1.0, (metres - overshootM) / metres));
                    double timeBeforeLineMs = dtModelMs * fractionBeforeLine;
                    double timeAfterLineMs = dtModelMs - timeBeforeLineMs;

                    // Small per-driver, per-lap millisecond variance so lap times have realistic .xxx precision
                    double jitterMs = (rnd.nextDouble() - 0.5) * 130.0
                            + ((state.getDriverId() * 37L + state.getCurrentLap() * 13L) % 29L - 14L) * 4.3;

                    lapDistanceM -= lapLen;
                    if (lapDistanceM >= lapLen) {
                        lapDistanceM = lapDistanceM % lapLen;
                    }

                    state.completeLap(timeBeforeLineMs, timeAfterLineMs, jitterMs);

                    // Complete pit stop if the car pitted on the lap just finished
                    if (state.isPittingThisLap()) {
                        state.fitTyres(state.getNextCompound());
                        state.setPittingThisLap(false);
                    }

                    // Enter pit window on the car's scheduled pit lap
                    if (state.getCurrentLap() == state.getPlannedPitLap()
                            && state.getCurrentLap() < sim.getTotalLaps()) {
                        state.setPittingThisLap(true);
                    }

                    sim.notifyLap(state);

                    if (state.getCurrentLap() > sim.getTotalLaps()) {
                        state.setFinished(true);
                        state.setSpeedKph(0.0);
                    }
                } else {
                    state.addLapModelMs(dtModelMs);
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
