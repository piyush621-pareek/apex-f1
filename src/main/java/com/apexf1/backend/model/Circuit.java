package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "circuits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Circuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String country;

    /** e.g. "IT", "BE" - handy for flag icons on the frontend */
    private String countryCode;

    private String city;

    private Double lengthKm;

    private Integer totalLaps;

    /** Path to the track-map image served from static resources, e.g. "/img/tracks/monza.png" */
    private String mapImageUrl;
}
