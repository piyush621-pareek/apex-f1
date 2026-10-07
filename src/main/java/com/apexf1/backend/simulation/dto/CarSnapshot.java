package com.apexf1.backend.simulation.dto;

/** Immutable view of one car at snapshot time (safe to serialize to JSON). */
public class CarSnapshot {
    public int position;
    public long driverId;
    public String driverCode;
    public String driverName;
    public String teamName;
    public String teamColor;
    public double speedKph;
    public int currentLap;
    public String compound;
    public int tyreAgeLaps;
    public long lastLapTimeMs;
    public double totalDistanceM;
    public double gapToLeaderM;
    public boolean finished;
}
