package com.apexf1.backend.repository;

import com.apexf1.backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByRaceId(Long raceId);
}
