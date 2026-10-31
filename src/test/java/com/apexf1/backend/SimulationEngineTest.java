package com.apexf1.backend;

import com.apexf1.backend.model.Circuit;
import com.apexf1.backend.model.Driver;
import com.apexf1.backend.model.Race;
import com.apexf1.backend.model.Team;
import com.apexf1.backend.repository.DriverRepository;
import com.apexf1.backend.repository.RaceRepository;
import com.apexf1.backend.simulation.BalancedPaceStrategy;
import com.apexf1.backend.simulation.CarSimState;
import com.apexf1.backend.simulation.SimulationEngine;
import com.apexf1.backend.simulation.dto.SimulationSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Automated JUnit 5 test suite for the multithreaded Race Simulation Engine,
 * sub-tick finish-line interpolation, and tyre pace strategy.
 */
class SimulationEngineTest {

    @Test
    void subTickInterpolationProducesDistinctMillisecondLapTimes() {
        CarSimState carA = new CarSimState(1L, "RUS", "George Russell", "Mercedes", "#00D2A0",
                220.0, "SOFT", 10, "HARD");
        CarSimState carB = new CarSimState(2L, "LEC", "Charles Leclerc", "Ferrari", "#E10600",
                218.5, "MEDIUM", 12, "HARD");

        carA.addLapModelMs(89000.0);
        carB.addLapModelMs(89000.0);

        // Two cars cross the line in the same tick at different sub-tick fractions
        carA.completeLap(312.4, 687.6, -18.2);
        carB.completeLap(745.9, 254.1, 24.7);

        assertNotEquals(carA.getLastLapTimeMs(), carB.getLastLapTimeMs(),
                "Cars crossing at different sub-tick fractions must not share identical lap times");
        assertEquals(2, carA.getCurrentLap());
        assertEquals(1, carA.getTyreAgeLaps());
    }

    @Test
    void balancedPaceStrategyRespectsCompoundsDegradationAndPitLoss() {
        BalancedPaceStrategy strategy = new BalancedPaceStrategy();
        CarSimState softCar = new CarSimState(1L, "NOR", "Lando Norris", "McLaren", "#FF8000",
                215.0, "SOFT", 8, "HARD");
        CarSimState hardCar = new CarSimState(2L, "PIA", "Oscar Piastri", "McLaren", "#FF8000",
                215.0, "HARD", 14, "MEDIUM");

        // Average over 200 samples to smooth out random tick noise
        Random rndA = new Random(42L);
        Random rndB = new Random(42L);
        double softSum = 0.0, hardSum = 0.0;
        for (int i = 0; i < 200; i++) {
            softSum += strategy.speedKph(softCar, rndA);
            hardSum += strategy.speedKph(hardCar, rndB);
        }
        assertTrue(softSum > hardSum, "Fresh SOFT tyre should have higher average speed than fresh HARD tyre");

        // Pit lap reduces speed by ~19%
        softCar.setPittingThisLap(true);
        double pitSpeed = strategy.speedKph(softCar, new Random(1L));
        assertTrue(pitSpeed < 190.0, "Car in pit lane should experience significant speed reduction");
    }

    @Test
    void simulationEngineStartsSnapshotsAndStopsCleanly() throws InterruptedException {
        RaceRepository raceRepo = mock(RaceRepository.class);
        DriverRepository driverRepo = mock(DriverRepository.class);

        Circuit suzuka = new Circuit(1L, "Suzuka International Racing Course", "Japan", "JP",
                "Suzuka", 5.807, 53, "/img/tracks/suzuka.svg");
        Race race = new Race(1L, 2026, 1, "Japanese GP", suzuka, LocalDate.of(2026, 3, 29), true, null, null);
        Team mer = new Team(1L, "Mercedes", "MER", "German", "#00D2A0", "/img/teams/mer.png");
        Team fer = new Team(2L, "Ferrari", "FER", "Italian", "#E10600", "/img/teams/fer.png");

        List<Driver> grid = List.of(
                new Driver(1L, "ANT", "Andrea K. Antonelli", 12, "Italian", "/img/drivers/ant.png", mer),
                new Driver(2L, "LEC", "Charles Leclerc", 16, "Monegasque", "/img/drivers/lec.png", fer)
        );

        when(raceRepo.findById(1L)).thenReturn(Optional.of(race));
        when(driverRepo.findAll()).thenReturn(grid);

        SimulationEngine engine = new SimulationEngine(raceRepo, driverRepo);
        try {
            // Passing null laps should default to full circuit laps (53 for Suzuka)
            SimulationSnapshot startSnap = engine.start(1L, null, 5.0);
            assertEquals("RUNNING", startSnap.status);
            assertEquals(53, startSnap.totalLaps);
            assertEquals(2, startSnap.cars.size());

            Thread.sleep(220);
            SimulationSnapshot liveSnap = engine.snapshot(startSnap.simulationId);
            assertTrue(liveSnap.cars.get(0).totalDistanceM > 0.0, "Cars should advance distance on ticks");

            SimulationSnapshot stoppedSnap = engine.stop(startSnap.simulationId);
            assertEquals("STOPPED", stoppedSnap.status);
        } finally {
            engine.shutdown();
        }
    }
}
