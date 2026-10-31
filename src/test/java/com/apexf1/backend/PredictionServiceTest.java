package com.apexf1.backend;

import com.apexf1.backend.model.Driver;
import com.apexf1.backend.model.Prediction;
import com.apexf1.backend.model.Race;
import com.apexf1.backend.model.Team;
import com.apexf1.backend.repository.DriverRepository;
import com.apexf1.backend.repository.PredictionRepository;
import com.apexf1.backend.repository.RaceRepository;
import com.apexf1.backend.service.PredictionService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Automated JUnit 5 test suite for Fan Strategy Predictions (FSP) submission and scoring.
 */
class PredictionServiceTest {

    @Test
    void scoresHundredFiftyAndZeroPercentPredictionsAccurately() {
        PredictionRepository predRepo = mock(PredictionRepository.class);
        RaceRepository raceRepo = mock(RaceRepository.class);
        DriverRepository driverRepo = mock(DriverRepository.class);

        Team mer = new Team(1L, "Mercedes", "MER", "German", "#00D2A0", "/img/teams/mer.png");
        Driver rus = new Driver(1L, "RUS", "George Russell", 63, "British", "/img/drivers/rus.png", mer);
        Driver ant = new Driver(2L, "ANT", "Andrea K. Antonelli", 12, "Italian", "/img/drivers/ant.png", mer);
        Driver ver = new Driver(3L, "VER", "Max Verstappen", 3, "Dutch", "/img/drivers/ver.png", null);

        Race race = new Race(10L, 2026, 2, "Bahrain GP", null, LocalDate.of(2026, 4, 12), true, rus, ant);

        Prediction exactPick = new Prediction(101L, race, "Sanjay", rus, ant, null, null, false);
        Prediction halfPick = new Prediction(102L, race, "Alex", rus, ver, null, null, false);
        Prediction missPick = new Prediction(103L, race, "Sam", ver, ver, null, null, false);

        when(raceRepo.findById(10L)).thenReturn(Optional.of(race));
        when(predRepo.findByRaceIdAndScoredFalse(10L)).thenReturn(List.of(exactPick, halfPick, missPick));
        when(predRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        PredictionService service = new PredictionService(predRepo, raceRepo, driverRepo);
        List<Prediction> scored = service.scorePredictionsForRace(10L);

        assertEquals(3, scored.size());
        assertEquals(100.0, exactPick.getAccuracyPct());
        assertTrue(exactPick.isScored());

        assertEquals(50.0, halfPick.getAccuracyPct());
        assertTrue(halfPick.isScored());

        assertEquals(0.0, missPick.getAccuracyPct());
        assertTrue(missPick.isScored());
    }
}
