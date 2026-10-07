package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weather_samples")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private Double airTempC;
    private Double trackTempC;
    private Double humidityPct;
    private Double windSpeedMs;
    private Double pressureMb;
}
