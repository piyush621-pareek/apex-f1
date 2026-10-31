package com.apexf1.backend.service;

import com.apexf1.backend.model.Team;
import com.apexf1.backend.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Team findById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team " + id + " not found"));
    }

    /** CREATE */
    public Team create(Team team) {
        team.setId(null);
        return teamRepository.save(team);
    }

    /** UPDATE */
    public Team update(Long id, Team incoming) {
        Team existing = findById(id);
        existing.setName(incoming.getName());
        existing.setShortName(incoming.getShortName());
        existing.setNationality(incoming.getNationality());
        existing.setColorHex(incoming.getColorHex());
        if (incoming.getLogoUrl() != null && !incoming.getLogoUrl().isBlank()) {
            existing.setLogoUrl(incoming.getLogoUrl());
        }
        return teamRepository.save(existing);
    }

    /** DELETE */
    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new NoSuchElementException("Team " + id + " not found");
        }
        teamRepository.deleteById(id);
    }
}
