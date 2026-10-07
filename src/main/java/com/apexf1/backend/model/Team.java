package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 3)
    private String shortName;

    private String nationality;

    /** Hex color used for charts/track position dots, e.g. "#00D2A0" */
    private String colorHex;

    /** Path to the team logo served from static resources, e.g. "/img/teams/fer.png" */
    private String logoUrl;
}
