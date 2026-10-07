package com.apexf1.backend.repository;

import com.apexf1.backend.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    List<Prediction> findByRaceId(Long raceId);

    List<Prediction> findByRaceIdAndScoredFalse(Long raceId);
}
