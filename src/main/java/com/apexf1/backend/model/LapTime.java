package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lap_times")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LapTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(nullable = false)
    private Integer lapNumber;

    /** Lap time in milliseconds, e.g. 80540 for 1:20.540 */
    private Long lapTimeMs;

    private Integer position;

    @Enumerated(EnumType.STRING)
    private Compound compound;

    private boolean pitLap;

    private boolean outlier;
}
