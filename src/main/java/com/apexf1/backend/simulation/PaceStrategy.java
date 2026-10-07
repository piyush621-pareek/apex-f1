package com.apexf1.backend.simulation;

import java.util.Random;

/**
 * STRATEGY design pattern.
 * Encapsulates how a car's speed is computed each tick, so different pace
 * models (balanced, aggressive, wet-weather…) can be swapped without
 * touching the simulation loop.
 */
public interface PaceStrategy {

    /**
     * @param state the car's current state (lap, tyre age, base pace…)
     * @param rnd   per-car random source for small lap-to-lap variation
     * @return the car's speed for this tick, in km/h
     */
    double speedKph(CarSimState state, Random rnd);

    /** Human-readable name shown in the API. */
    String name();
}
