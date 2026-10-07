package com.apexf1.backend.controller;

import com.apexf1.backend.model.Race;
import com.apexf1.backend.model.Session;
import com.apexf1.backend.service.RaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/races")
public class RaceController {

    private final RaceService raceService;

    public RaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @GetMapping
    public List<Race> getBySeason(@RequestParam(defaultValue = "2026") Integer season) {
        return raceService.findBySeason(season);
    }

    @GetMapping("/{id}")
    public Race getById(@PathVariable Long id) {
        return raceService.findById(id);
    }

    @GetMapping("/{id}/sessions")
    public List<Session> getSessions(@PathVariable Long id) {
        return raceService.findSessionsForRace(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Race create(@RequestBody Race race) {
        return raceService.create(race);
    }

    @PutMapping("/{id}")
    public Race update(@PathVariable Long id, @RequestBody Race race) {
        return raceService.update(id, race);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        raceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
