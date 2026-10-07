package com.apexf1.backend.repository;

import com.apexf1.backend.model.TelemetryPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelemetryPointRepository extends JpaRepository<TelemetryPoint, Long> {

    List<TelemetryPoint> findBySessionIdAndDriverIdAndLapNumberOrderBySampleIndexAsc(
            Long sessionId, Long driverId, Integer lapNumber);

    List<TelemetryPoint> findBySessionIdAndDriverIdOrderByLapNumberAscSampleIndexAsc(
            Long sessionId, Long driverId);
}
