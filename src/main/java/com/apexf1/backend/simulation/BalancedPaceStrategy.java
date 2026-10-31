package com.apexf1.backend.simulation;

import java.util.Random;

/**
 * A realistic 2026 pace model:
 *  - starts from the car's circuit-specific and team/driver-specific base pace,
 *  - applies compound speed delta (SOFT fastest, MEDIUM baseline, HARD durable),
 *  - loses speed as tyres age (compound-specific degradation),
 *  - gains slight pace as fuel burns off over the race,
 *  - applies pit-lane speed loss on the driver's scheduled pit lap,
 *  - adds realistic lap-to-lap and corner-to-corner speed variation.
 */
public class BalancedPaceStrategy implements PaceStrategy {

    private static final double NOISE_KPH = 3.2;

    @Override
    public double speedKph(CarSimState state, Random rnd) {
        String cmp = state.getCompound();
        double compoundBonusKph;
        double degPerLapKph;
        if ("SOFT".equals(cmp)) {
            compoundBonusKph = 2.2;
            degPerLapKph = 0.42;
        } else if ("HARD".equals(cmp)) {
            compoundBonusKph = -1.4;
            degPerLapKph = 0.16;
        } else {
            compoundBonusKph = 0.0;
            degPerLapKph = 0.28;
        }

        double tyrePenalty = state.getTyreAgeLaps() * degPerLapKph;
        double fuelBurnBonus = Math.min(3.5, (state.getCurrentLap() - 1) * 0.06);
        double noise = (rnd.nextDouble() * 2.0 - 1.0) * NOISE_KPH;

        double speed = state.getBasePaceKph() + compoundBonusKph - tyrePenalty + fuelBurnBonus + noise;

        // Pit-lane time loss (~20-22s across the pit lap)
        if (state.isPittingThisLap()) {
            speed *= 0.81;
        }

        return Math.max(60.0, speed);
    }

    @Override
    public String name() {
        return "BALANCED";
    }
}
