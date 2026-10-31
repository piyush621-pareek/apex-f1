package com.apexf1.backend.simulation;

/**
 * Live, mutable state for one car in a running simulation.
 *
 * Threading: each car is advanced by its OWN thread (see {@link CarRunner}),
 * which is the only writer. Snapshot readers on other threads read the
 * volatile fields. The compound update in {@link #completeLap()} and the
 * distance accumulation in {@link #addDistance(double)} are synchronized so a
 * reader never sees a half-updated lap.
 */
public class CarSimState {

    private final long driverId;
    private final String driverCode;
    private final String driverName;
    private final String teamName;
    private final String teamColor;
    private final double basePaceKph;
    private final int plannedPitLap;
    private final String nextCompound;

    private volatile double speedKph = 0.0;
    private volatile double totalDistanceM = 0.0;
    private volatile int currentLap = 1;
    private volatile int tyreAgeLaps = 0;
    private volatile String compound = "MEDIUM";
    private volatile long lastLapTimeMs = 0L;
    private volatile boolean finished = false;
    private volatile boolean pittingThisLap = false;

    private double lapModelMs = 0.0;  // sub-millisecond model time accumulated on the current lap

    public CarSimState(long driverId, String driverCode, String driverName,
                       String teamName, String teamColor, double basePaceKph) {
        this(driverId, driverCode, driverName, teamName, teamColor, basePaceKph, "MEDIUM", 5, "HARD");
    }

    public CarSimState(long driverId, String driverCode, String driverName,
                       String teamName, String teamColor, double basePaceKph,
                       String startCompound, int plannedPitLap, String nextCompound) {
        this.driverId = driverId;
        this.driverCode = driverCode;
        this.driverName = driverName;
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.basePaceKph = basePaceKph;
        this.compound = startCompound != null ? startCompound : "MEDIUM";
        this.plannedPitLap = plannedPitLap;
        this.nextCompound = nextCompound != null ? nextCompound : "HARD";
    }

    /** Advance distance for a tick (called only by the car's own thread). */
    public synchronized void addDistance(double metres) {
        this.totalDistanceM += metres;
    }

    public synchronized void addLapModelMs(long ms) {
        this.lapModelMs += (double) ms;
    }

    public synchronized void addLapModelMs(double ms) {
        this.lapModelMs += ms;
    }

    /** Cross the line: record the lap time, bump lap + tyre age, reset lap clock. */
    public synchronized void completeLap() {
        completeLap(0.0, 0.0, 0.0);
    }

    /**
     * Cross the line with exact sub-tick interpolation so every driver records
     * a distinct, millisecond-accurate lap time and carries overshoot time into the next lap.
     */
    public synchronized void completeLap(double timeBeforeLineMs, double timeAfterLineMs, double jitterMs) {
        double rawLapMs = this.lapModelMs + timeBeforeLineMs + jitterMs;
        this.lastLapTimeMs = Math.max(40000L, Math.round(rawLapMs));
        this.lapModelMs = Math.max(0.0, timeAfterLineMs);
        this.currentLap += 1;
        this.tyreAgeLaps += 1;
    }

    /** Simulate a pit stop: fresh tyres, reset degradation. */
    public synchronized void fitTyres(String newCompound) {
        this.compound = newCompound;
        this.tyreAgeLaps = 0;
    }

    // ---- getters (volatile reads are thread-safe) ----
    public long getDriverId() { return driverId; }
    public String getDriverCode() { return driverCode; }
    public String getDriverName() { return driverName; }
    public String getTeamName() { return teamName; }
    public String getTeamColor() { return teamColor; }
    public double getBasePaceKph() { return basePaceKph; }
    public int getPlannedPitLap() { return plannedPitLap; }
    public String getNextCompound() { return nextCompound; }
    public double getSpeedKph() { return speedKph; }
    public double getTotalDistanceM() { return totalDistanceM; }
    public int getCurrentLap() { return currentLap; }
    public int getTyreAgeLaps() { return tyreAgeLaps; }
    public String getCompound() { return compound; }
    public long getLastLapTimeMs() { return lastLapTimeMs; }
    public boolean isFinished() { return finished; }
    public boolean isPittingThisLap() { return pittingThisLap; }

    public void setSpeedKph(double v) { this.speedKph = v; }
    public void setFinished(boolean v) { this.finished = v; }
    public void setPittingThisLap(boolean v) { this.pittingThisLap = v; }
}
