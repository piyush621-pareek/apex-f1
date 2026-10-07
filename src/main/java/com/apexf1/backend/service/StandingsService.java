package com.apexf1.backend.service;

import com.apexf1.backend.model.DriverStanding;
import com.apexf1.backend.model.TeamStanding;
import com.apexf1.backend.repository.DriverStandingRepository;
import com.apexf1.backend.repository.TeamStandingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StandingsService {

    private final DriverStandingRepository driverStandingRepository;
    private final TeamStandingRepository teamStandingRepository;

    public StandingsService(DriverStandingRepository driverStandingRepository,
                             TeamStandingRepository teamStandingRepository) {
        this.driverStandingRepository = driverStandingRepository;
        this.teamStandingRepository = teamStandingRepository;
    }

    public List<DriverStanding> driverStandings(Integer season) {
        return driverStandingRepository.findBySeasonOrderByPositionAsc(season);
    }

    public List<TeamStanding> teamStandings(Integer season) {
        return teamStandingRepository.findBySeasonOrderByPositionAsc(season);
    }
}
