package com.apexf1.backend.repository;

import com.apexf1.backend.model.WeatherSample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeatherSampleRepository extends JpaRepository<WeatherSample, Long> {

    List<WeatherSample> findBySessionId(Long sessionId);
}
