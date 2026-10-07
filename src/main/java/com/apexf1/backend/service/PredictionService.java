package com.apexf1.backend.service;

import com.apexf1.backend.model.Driver;
import com.apexf1.backend.model.Prediction;
import com.apexf1.backend.model.Race;
import com.apexf1.backend.repository.DriverRepository;
import com.apexf1.backend.repository.PredictionRepository;
import com.apexf1.backend.repository.RaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Fan Strategy Predictor.
 * - Before a race: fans submit a pole pick + a winner pick.
 * - After the race is marked completed (with actualPoleDriver /
 *   actualWinnerDriver set on the Race), scorePredictionsForRace()
 *   computes each fan's accuracy: pole correct = 50%, winner correct = 50%.
 */
@Service
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final RaceRepository raceRepository;
    private final DriverRepository driverRepository;

    public PredictionService(PredictionRepository predictionRepository,
                              RaceRepository raceRepository,
                              DriverRepository driverRepository) {
        this.predictionRepository = predictionRepository;
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
    }

    public Prediction submit(Long raceId, String fanName, Long predictedPoleDriverId, Long predictedWinnerDriverId) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new NoSuchElementException("Race " + raceId + " not found"));

        Driver pole = driverRepository.findById(predictedPoleDriverId)
                .orElseThrow(() -> new NoSuchElementException("Driver " + predictedPoleDriverId + " not found"));

        Driver winner = driverRepository.findById(predictedWinnerDriverId)
                .orElseThrow(() -> new NoSuchElementException("Driver " + predictedWinnerDriverId + " not found"));

        Prediction prediction = new Prediction();
        prediction.setRace(race);
        prediction.setFanName(fanName);
        prediction.setPredictedPoleDriver(pole);
        prediction.setPredictedWinnerDriver(winner);
        prediction.setSubmittedAt(LocalDateTime.now());
        prediction.setScored(false);

        return predictionRepository.save(prediction);
    }

    public List<Prediction> findByRace(Long raceId) {
        return predictionRepository.findByRaceId(raceId);
    }

    /** UPDATE — let a fan change their pole/winner picks (resets scoring). */
    public Prediction update(Long predictionId, String fanName,
                             Long predictedPoleDriverId, Long predictedWinnerDriverId) {
        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new NoSuchElementException("Prediction " + predictionId + " not found"));

        Driver pole = driverRepository.findById(predictedPoleDriverId)
                .orElseThrow(() -> new NoSuchElementException("Driver " + predictedPoleDriverId + " not found"));
        Driver winner = driverRepository.findById(predictedWinnerDriverId)
                .orElseThrow(() -> new NoSuchElementException("Driver " + predictedWinnerDriverId + " not found"));

        prediction.setFanName(fanName);
        prediction.setPredictedPoleDriver(pole);
        prediction.setPredictedWinnerDriver(winner);
        prediction.setScored(false);
        prediction.setAccuracyPct(null);
        return predictionRepository.save(prediction);
    }

    /** DELETE */
    public void delete(Long predictionId) {
        if (!predictionRepository.existsById(predictionId)) {
            throw new NoSuchElementException("Prediction " + predictionId + " not found");
        }
        predictionRepository.deleteById(predictionId);
    }

    /**
     * Call once a race's actual pole/winner are set on the Race entity.
     * Scores every not-yet-scored prediction for that race.
     */
    public List<Prediction> scorePredictionsForRace(Long raceId) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new NoSuchElementException("Race " + raceId + " not found"));

        if (race.getActualPoleDriver() == null || race.getActualWinnerDriver() == null) {
            throw new IllegalStateException("Race " + raceId + " has no actual pole/winner set yet");
        }

        List<Prediction> unscored = predictionRepository.findByRaceIdAndScoredFalse(raceId);

        for (Prediction prediction : unscored) {
            double accuracy = 0.0;

            if (prediction.getPredictedPoleDriver().getId().equals(race.getActualPoleDriver().getId())) {
                accuracy += 50.0;
            }
            if (prediction.getPredictedWinnerDriver().getId().equals(race.getActualWinnerDriver().getId())) {
                accuracy += 50.0;
            }

            prediction.setAccuracyPct(accuracy);
            prediction.setScored(true);
        }

        return predictionRepository.saveAll(unscored);
    }
}
