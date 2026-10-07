package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    /** Simple display name for the fan - no login system in this project */
    @Column(nullable = false)
    private String fanName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "predicted_pole_driver_id", nullable = false)
    private Driver predictedPoleDriver;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "predicted_winner_driver_id", nullable = false)
    private Driver predictedWinnerDriver;

    private LocalDateTime submittedAt;

    /** Null until Race.completed is true and the prediction has been scored */
    private Double accuracyPct;

    private boolean scored;
}
