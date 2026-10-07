package com.apexf1.backend.controller;

import com.apexf1.backend.dto.PredictionRequest;
import com.apexf1.backend.model.Prediction;
import com.apexf1.backend.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /** Fan submits a pole pick + winner pick before the race. */
    @PostMapping("/race/{raceId}")
    public Prediction submit(@PathVariable Long raceId, @Valid @RequestBody PredictionRequest request) {
        return predictionService.submit(
                raceId,
                request.getFanName(),
                request.getPredictedPoleDriverId(),
                request.getPredictedWinnerDriverId()
        );
    }

    @GetMapping("/race/{raceId}")
    public List<Prediction> getForRace(@PathVariable Long raceId) {
        return predictionService.findByRace(raceId);
    }

    /** Fan edits an existing pick (identified by prediction id). */
    @PutMapping("/{predictionId}")
    public Prediction update(@PathVariable Long predictionId, @Valid @RequestBody PredictionRequest request) {
        return predictionService.update(
                predictionId,
                request.getFanName(),
                request.getPredictedPoleDriverId(),
                request.getPredictedWinnerDriverId()
        );
    }

    @DeleteMapping("/{predictionId}")
    public ResponseEntity<Void> delete(@PathVariable Long predictionId) {
        predictionService.delete(predictionId);
        return ResponseEntity.noContent().build();
    }

    /** Call after the race's actual pole/winner are recorded on the Race entity. */
    @PostMapping("/race/{raceId}/score")
    public List<Prediction> score(@PathVariable Long raceId) {
        return predictionService.scorePredictionsForRace(raceId);
    }
}
