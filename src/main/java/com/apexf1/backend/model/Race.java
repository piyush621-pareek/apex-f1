package com.apexf1.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "races")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer season;

    /** Round number in the season, e.g. 7 for "R7" */
    @Column(nullable = false)
    private Integer round;

    /** Display name, e.g. "Barcelona GP" */
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "circuit_id")
    private Circuit circuit;

    private LocalDate raceDate;

    /** True once results are final, used to trigger FSP scoring */
    private boolean completed;

    /** Winner and pole are set once the race/quali finish, used to score FSP */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actual_pole_driver_id")
    private Driver actualPoleDriver;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actual_winner_driver_id")
    private Driver actualWinnerDriver;
}
