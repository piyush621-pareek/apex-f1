package com.apexf1.backend.service;

import com.apexf1.backend.model.Circuit;
import com.apexf1.backend.model.Driver;
import com.apexf1.backend.model.Race;
import com.apexf1.backend.model.Session;
import com.apexf1.backend.repository.CircuitRepository;
import com.apexf1.backend.repository.DriverRepository;
import com.apexf1.backend.repository.RaceRepository;
import com.apexf1.backend.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RaceService {

    private final RaceRepository raceRepository;
    private final SessionRepository sessionRepository;
    private final CircuitRepository circuitRepository;
    private final DriverRepository driverRepository;

    public RaceService(RaceRepository raceRepository,
                       SessionRepository sessionRepository,
                       CircuitRepository circuitRepository,
                       DriverRepository driverRepository) {
        this.raceRepository = raceRepository;
        this.sessionRepository = sessionRepository;
        this.circuitRepository = circuitRepository;
        this.driverRepository = driverRepository;
    }

    public List<Race> findBySeason(Integer season) {
        return raceRepository.findBySeasonOrderByRoundAsc(season);
    }

    public Race findById(Long id) {
        return raceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Race " + id + " not found"));
    }

    public List<Session> findSessionsForRace(Long raceId) {
        return sessionRepository.findByRaceId(raceId);
    }

    /** CREATE */
    public Race create(Race race) {
        race.setId(null);
        applyRefs(race, race);
        return raceRepository.save(race);
    }

    /** UPDATE */
    public Race update(Long id, Race incoming) {
        Race existing = findById(id);
        existing.setSeason(incoming.getSeason());
        existing.setRound(incoming.getRound());
        existing.setName(incoming.getName());
        existing.setRaceDate(incoming.getRaceDate());
        existing.setCompleted(incoming.isCompleted());
        applyRefs(existing, incoming);
        return raceRepository.save(existing);
    }

    /** DELETE */
    public void delete(Long id) {
        if (!raceRepository.existsById(id)) {
            throw new NoSuchElementException("Race " + id + " not found");
        }
        raceRepository.deleteById(id);
    }

    /** Resolves circuit / pole / winner id references onto the target race. */
    private void applyRefs(Race target, Race source) {
        target.setCircuit(resolveCircuit(source.getCircuit()));
        target.setActualPoleDriver(resolveDriver(source.getActualPoleDriver()));
        target.setActualWinnerDriver(resolveDriver(source.getActualWinnerDriver()));
    }

    private Circuit resolveCircuit(Circuit ref) {
        if (ref == null || ref.getId() == null) return null;
        return circuitRepository.findById(ref.getId())
                .orElseThrow(() -> new NoSuchElementException("Circuit " + ref.getId() + " not found"));
    }

    private Driver resolveDriver(Driver ref) {
        if (ref == null || ref.getId() == null) return null;
        return driverRepository.findById(ref.getId())
                .orElseThrow(() -> new NoSuchElementException("Driver " + ref.getId() + " not found"));
    }
}
