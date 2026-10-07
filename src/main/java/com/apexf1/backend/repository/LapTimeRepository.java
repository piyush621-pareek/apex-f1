package com.apexf1.backend.repository;

import com.apexf1.backend.model.LapTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LapTimeRepository extends JpaRepository<LapTime, Long> {

    List<LapTime> findBySessionIdOrderByLapNumberAsc(Long sessionId);

    List<LapTime> findBySessionIdAndDriverIdOrderByLapNumberAsc(Long sessionId, Long driverId);
}
