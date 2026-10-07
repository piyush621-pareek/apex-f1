package com.apexf1.backend.simulation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * One running race simulation. Owns the shared, thread-safe state that the
 * per-car threads write to and the REST snapshot reads from.
 */
public class RaceSimulation {

    public enum Status { RUNNING, FINISHED, STOPPED }

    private final long id;
    private final Long raceId;
    private final String raceName;
    private final int totalLaps;
    private final double lapLengthM;
    private final double timeScale;          // model seconds per real second
    private final List<CarSimState> cars;
    private final CountDownLatch finishLatch; // counts down as each car finishes

    /** Observers get live telemetry — COW list is safe for concurrent notify. */
    private final List<TelemetryObserver> observers = new CopyOnWriteArrayList<>();

    private volatile Status status = Status.RUNNING;
    private final long startedAtEpochMs = System.currentTimeMillis();

    public RaceSimulation(long id, Long raceId, String raceName, int totalLaps,
                          double lapLengthM, double timeScale, List<CarSimState> cars) {
        this.id = id;
        this.raceId = raceId;
        this.raceName = raceName;
        this.totalLaps = totalLaps;
        this.lapLengthM = lapLengthM;
        this.timeScale = timeScale;
        this.cars = cars;
        this.finishLatch = new CountDownLatch(cars.size());
    }

    // ---- observer registry (Observer pattern) ----
    public void addObserver(TelemetryObserver o) { observers.add(o); }
    public void removeObserver(TelemetryObserver o) { observers.remove(o); }

    void notifyTick(CarSimState car) {
        for (TelemetryObserver o : observers) o.onTick(id, car);
    }
    void notifyLap(CarSimState car) {
        for (TelemetryObserver o : observers) o.onLapCompleted(id, car);
    }
    void notifyFinished() {
        for (TelemetryObserver o : observers) o.onRaceFinished(id);
    }

    /** Each car thread calls this once as it exits. */
    void carFinished() { finishLatch.countDown(); }

    public void awaitAllCars() throws InterruptedException { finishLatch.await(); }

    public boolean isRunning() { return status == Status.RUNNING; }
    public void stop() { this.status = Status.STOPPED; }
    public void markFinished() { if (status == Status.RUNNING) this.status = Status.FINISHED; }

    // ---- getters ----
    public long getId() { return id; }
    public Long getRaceId() { return raceId; }
    public String getRaceName() { return raceName; }
    public int getTotalLaps() { return totalLaps; }
    public double getLapLengthM() { return lapLengthM; }
    public double getTimeScale() { return timeScale; }
    public List<CarSimState> getCars() { return cars; }
    public Status getStatus() { return status; }
    public long getStartedAtEpochMs() { return startedAtEpochMs; }
}
