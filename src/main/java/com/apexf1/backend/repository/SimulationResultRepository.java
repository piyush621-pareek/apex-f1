package com.apexf1.backend.repository;

import com.apexf1.backend.model.SimulationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationResultRepository extends JpaRepository<SimulationResult, Long> {

    /** Most recent simulations first. */
    List<SimulationResult> findAllByOrderByFinishedAtDesc();

    /** Saved results for one race, most recent first. */
    List<SimulationResult> findByRaceIdOrderByFinishedAtDesc(Long raceId);
}
