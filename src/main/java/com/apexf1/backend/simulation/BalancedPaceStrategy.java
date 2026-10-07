package com.apexf1.backend.simulation;

import java.util.Random;

/**
 * A realistic-ish default pace model:
 *  - starts from the car's individual base pace,
 *  - loses a little speed as the tyres age (degradation),
 *  - adds small random lap-to-lap noise.
 */
public class BalancedPaceStrategy implements PaceStrategy {

    private static final double TYRE_DEG_PER_LAP = 0.35;  // km/h lost per lap of tyre age
    private static final double NOISE_KPH = 4.0;          // +/- random variation

    @Override
    public double speedKph(CarSimState state, Random rnd) {
        double tyrePenalty = state.getTyreAgeLaps() * TYRE_DEG_PER_LAP;
        double noise = (rnd.nextDouble() * 2 - 1) * NOISE_KPH;
        double speed = state.getBasePaceKph() - tyrePenalty + noise;
        return Math.max(60.0, speed); // never below a crawl
    }

    @Override
    public String name() {
        return "BALANCED";
    }
}
