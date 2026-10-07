package com.apexf1.backend.service;

import com.apexf1.backend.model.LapTime;
import com.apexf1.backend.model.Session;
import com.apexf1.backend.model.TelemetryPoint;
import com.apexf1.backend.model.WeatherSample;
import com.apexf1.backend.repository.LapTimeRepository;
import com.apexf1.backend.repository.SessionRepository;
import com.apexf1.backend.repository.TelemetryPointRepository;
import com.apexf1.backend.repository.WeatherSampleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Backs the Pit Wall page: everything that is looked up by session id
 * (lap times, telemetry, weather) lives here.
 */
@Service
public class SessionDataService {

    private final SessionRepository sessionRepository;
    private final LapTimeRepository lapTimeRepository;
    private final TelemetryPointRepository telemetryPointRepository;
    private final WeatherSampleRepository weatherSampleRepository;

    public SessionDataService(SessionRepository sessionRepository,
                               LapTimeRepository lapTimeRepository,
                               TelemetryPointRepository telemetryPointRepository,
                               WeatherSampleRepository weatherSampleRepository) {
        this.sessionRepository = sessionRepository;
        this.lapTimeRepository = lapTimeRepository;
        this.telemetryPointRepository = telemetryPointRepository;
        this.weatherSampleRepository = weatherSampleRepository;
    }

    public Session findById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session " + sessionId + " not found"));
    }

    public List<LapTime> lapTimes(Long sessionId, Long driverId) {
        if (driverId != null) {
            return lapTimeRepository.findBySessionIdAndDriverIdOrderByLapNumberAsc(sessionId, driverId);
        }
        return lapTimeRepository.findBySessionIdOrderByLapNumberAsc(sessionId);
    }

    public List<TelemetryPoint> telemetry(Long sessionId, Long driverId, Integer lapNumber) {
        if (lapNumber != null) {
            return telemetryPointRepository
                    .findBySessionIdAndDriverIdAndLapNumberOrderBySampleIndexAsc(sessionId, driverId, lapNumber);
        }
        return telemetryPointRepository
                .findBySessionIdAndDriverIdOrderByLapNumberAscSampleIndexAsc(sessionId, driverId);
    }

    public List<WeatherSample> weather(Long sessionId) {
        return weatherSampleRepository.findBySessionId(sessionId);
    }
}
