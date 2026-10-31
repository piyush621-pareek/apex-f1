package com.apexf1.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One car's finishing line in a saved SimulationResult (final classification).
 */
@Entity
@Table(name = "simulation_result_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResultEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Back-reference to the parent result (ignored in JSON to avoid recursion). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    @JsonIgnore
    private SimulationResult result;

    private Integer position;
    private Long driverId;
    private String driverCode;
    private String driverName;
    private String teamName;
    private Integer finalLap;
    private Double totalDistanceM;
    private Long lastLapTimeMs;
}
