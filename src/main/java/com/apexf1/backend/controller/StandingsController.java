package com.apexf1.backend.controller;

import com.apexf1.backend.model.DriverStanding;
import com.apexf1.backend.model.TeamStanding;
import com.apexf1.backend.service.StandingsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/standings")
public class StandingsController {

    private final StandingsService standingsService;

    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    @GetMapping("/drivers")
    public List<DriverStanding> getDriverStandings(@RequestParam(defaultValue = "2026") Integer season) {
        return standingsService.driverStandings(season);
    }

    @GetMapping("/teams")
    public List<TeamStanding> getTeamStandings(@RequestParam(defaultValue = "2026") Integer season) {
        return standingsService.teamStandings(season);
    }
}
