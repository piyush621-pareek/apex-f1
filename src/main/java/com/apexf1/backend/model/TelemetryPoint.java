package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "telemetry_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryPoint {

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

    /** Order of this sample within the lap - used to draw the line charts in order */
    @Column(nullable = false)
    private Integer sampleIndex;

    private Double distanceM;
    private Double speedKph;
    private Double throttlePct;

    /** 0 or 1 - matches the dot-meter brake readout in the UI */
    private Double brake;

    private Integer gear;
    private Integer rpm;
}
