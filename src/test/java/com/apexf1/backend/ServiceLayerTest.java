package com.apexf1.backend;

import com.apexf1.backend.model.Driver;
import com.apexf1.backend.model.Team;
import com.apexf1.backend.repository.DriverRepository;
import com.apexf1.backend.repository.TeamRepository;
import com.apexf1.backend.service.DriverService;
import com.apexf1.backend.service.TeamService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Automated JUnit 5 tests for DriverService and TeamService CRUD behaviour.
 */
class ServiceLayerTest {

    @Test
    void driverUpdatePreservesExistingHeadshotWhenOmittedByManageForm() {
        DriverRepository driverRepo = mock(DriverRepository.class);
        TeamRepository teamRepo = mock(TeamRepository.class);

        Team ferrari = new Team(2L, "Ferrari", "FER", "Italian", "#E10600", "/img/teams/fer.png");
        Driver existing = new Driver(7L, "HAM", "Lewis Hamilton", 44, "British", "/img/drivers/ham.png", ferrari);

        when(driverRepo.findById(7L)).thenReturn(Optional.of(existing));
        when(teamRepo.findById(2L)).thenReturn(Optional.of(ferrari));
        when(driverRepo.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        DriverService driverService = new DriverService(driverRepo, teamRepo);

        // Simulate Manage form PUT payload which omits headshotUrl (null)
        Driver incoming = new Driver(null, "HAM", "Sir Lewis Hamilton", 44, "British", null, ferrari);
        Driver updated = driverService.update(7L, incoming);

        assertEquals("Sir Lewis Hamilton", updated.getFullName());
        assertEquals("/img/drivers/ham.png", updated.getHeadshotUrl(),
                "Existing headshot URL must not be overwritten with null");
    }

    @Test
    void teamUpdatePreservesExistingLogoWhenOmittedByManageForm() {
        TeamRepository teamRepo = mock(TeamRepository.class);
        Team existing = new Team(3L, "Audi", "AUD", "German", "#8C96A8", "/img/teams/audi.png");

        when(teamRepo.findById(3L)).thenReturn(Optional.of(existing));
        when(teamRepo.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamService teamService = new TeamService(teamRepo);
        Team incoming = new Team(null, "Audi F1 Team", "AUD", "German", "#FF0000", null);
        Team updated = teamService.update(3L, incoming);

        assertEquals("Audi F1 Team", updated.getName());
        assertEquals("#FF0000", updated.getColorHex());
        assertEquals("/img/teams/audi.png", updated.getLogoUrl(),
                "Existing team logo URL must not be overwritten with null");
    }
}
