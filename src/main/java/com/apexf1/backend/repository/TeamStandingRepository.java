package com.apexf1.backend.repository;

import com.apexf1.backend.model.TeamStanding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamStandingRepository extends JpaRepository<TeamStanding, Long> {

    List<TeamStanding> findBySeasonOrderByPositionAsc(Integer season);
}
