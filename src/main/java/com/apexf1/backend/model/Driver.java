package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 3-letter code shown in the UI, e.g. "VER" */
    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(nullable = false)
    private String fullName;

    private Integer carNumber;

    private String nationality;

    private String headshotUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    private Team team;
}
