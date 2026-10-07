package com.apexf1.backend.simulation.dto;

import java.util.List;

/** Full state of a running/finished simulation for one API call. */
public class SimulationSnapshot {
    public long simulationId;
    public Long raceId;
    public String raceName;
    public String status;      // RUNNING | FINISHED | STOPPED
    public int totalLaps;
    public double lapLengthM;
    public double timeScale;
    public int leaderLap;
    public List<CarSnapshot> cars;
}
