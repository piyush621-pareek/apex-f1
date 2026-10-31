package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A saved (persisted) result of one finished race simulation.
 * Created automatically by the SimulationEngine when a race completes,
 * so simulation history survives a restart ("persistent race statistics").
 */
@Entity
@Table(name = "simulation_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The in-memory simulation id this result came from. */
    private Long simulationId;

    private Long raceId;
    private String raceName;
    private Integer totalLaps;
    private Double timeScale;

    private String winnerCode;
    private String winnerName;

    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SimulationResultEntry> entries;
}
