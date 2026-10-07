package com.apexf1.backend.repository;

import com.apexf1.backend.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceRepository extends JpaRepository<Race, Long> {

    List<Race> findBySeasonOrderByRoundAsc(Integer season);
}
