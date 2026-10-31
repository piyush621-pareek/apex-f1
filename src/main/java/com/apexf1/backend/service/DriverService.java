package com.apexf1.backend.service;

import com.apexf1.backend.model.Driver;
import com.apexf1.backend.model.Team;
import com.apexf1.backend.repository.DriverRepository;
import com.apexf1.backend.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;

    public DriverService(DriverRepository driverRepository, TeamRepository teamRepository) {
        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
    }

    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    public Driver findById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Driver " + id + " not found"));
    }

    /** CREATE */
    public Driver create(Driver driver) {
        driver.setId(null);                 // ignore any client-supplied id
        if (driver.getHeadshotUrl() == null || driver.getHeadshotUrl().isBlank()) {
            if (driver.getCode() != null && !driver.getCode().isBlank()) {
                driver.setHeadshotUrl("/img/drivers/" + driver.getCode().trim().toLowerCase() + ".png");
            }
        }
        driver.setTeam(resolveTeam(driver.getTeam()));
        return driverRepository.save(driver);
    }

    /** UPDATE */
    public Driver update(Long id, Driver incoming) {
        Driver existing = findById(id);
        existing.setCode(incoming.getCode());
        existing.setFullName(incoming.getFullName());
        existing.setCarNumber(incoming.getCarNumber());
        existing.setNationality(incoming.getNationality());
        if (incoming.getHeadshotUrl() != null && !incoming.getHeadshotUrl().isBlank()) {
            existing.setHeadshotUrl(incoming.getHeadshotUrl());
        }
        existing.setTeam(resolveTeam(incoming.getTeam()));
        return driverRepository.save(existing);
    }

    /** DELETE */
    public void delete(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new NoSuchElementException("Driver " + id + " not found");
        }
        driverRepository.deleteById(id);
    }

    /** Turns a {"team":{"id":X}} reference from the request into a managed Team. */
    private Team resolveTeam(Team ref) {
        if (ref == null || ref.getId() == null) return null;
        return teamRepository.findById(ref.getId())
                .orElseThrow(() -> new NoSuchElementException("Team " + ref.getId() + " not found"));
    }
}
