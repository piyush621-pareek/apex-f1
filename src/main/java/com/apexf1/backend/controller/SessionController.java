package com.apexf1.backend.controller;

import com.apexf1.backend.model.LapTime;
import com.apexf1.backend.model.Session;
import com.apexf1.backend.model.TelemetryPoint;
import com.apexf1.backend.model.WeatherSample;
import com.apexf1.backend.service.SessionDataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionDataService sessionDataService;

    public SessionController(SessionDataService sessionDataService) {
        this.sessionDataService = sessionDataService;
    }

    @GetMapping("/{id}")
    public Session getById(@PathVariable Long id) {
        return sessionDataService.findById(id);
    }

    @GetMapping("/{id}/laptimes")
    public List<LapTime> getLapTimes(@PathVariable Long id,
                                      @RequestParam(required = false) Long driverId) {
        return sessionDataService.lapTimes(id, driverId);
    }

    @GetMapping("/{id}/telemetry")
    public List<TelemetryPoint> getTelemetry(@PathVariable Long id,
                                              @RequestParam Long driverId,
                                              @RequestParam(required = false) Integer lap) {
        return sessionDataService.telemetry(id, driverId, lap);
    }

    @GetMapping("/{id}/weather")
    public List<WeatherSample> getWeather(@PathVariable Long id) {
        return sessionDataService.weather(id);
    }
}
