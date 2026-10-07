package com.apexf1.backend.repository;

import com.apexf1.backend.model.DriverStanding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverStandingRepository extends JpaRepository<DriverStanding, Long> {

    List<DriverStanding> findBySeasonOrderByPositionAsc(Integer season);
}
