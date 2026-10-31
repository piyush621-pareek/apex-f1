package com.apexf1.backend.simulation;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Concrete {@link TelemetryObserver} that pushes real-time simulation snapshots
 * over WebSocket to connected Pit Wall / Race Sim browsers.
 * Throttles tick broadcasts to ~6-7 FPS (every 150 ms) while pushing lap-completion
 * and race-finish events immediately.
 */
public class WebSocketTelemetryObserver implements TelemetryObserver {

    private static final long MIN_BROADCAST_INTERVAL_MS = 150L;

    private final SimulationEngine engine;
    private final SimulationWebSocketHandler wsHandler;
    private final AtomicLong lastSentMs = new AtomicLong(0L);

    public WebSocketTelemetryObserver(SimulationEngine engine, SimulationWebSocketHandler wsHandler) {
        this.engine = engine;
        this.wsHandler = wsHandler;
    }

    @Override
    public void onTick(long simulationId, CarSimState car) {
        if (wsHandler == null || !wsHandler.hasSubscribers()) {
            return;
        }
        long now = System.currentTimeMillis();
        long prev = lastSentMs.get();
        if (now - prev >= MIN_BROADCAST_INTERVAL_MS && lastSentMs.compareAndSet(prev, now)) {
            wsHandler.broadcast(engine.snapshot(simulationId));
        }
    }

    @Override
    public void onLapCompleted(long simulationId, CarSimState car) {
        if (wsHandler != null && wsHandler.hasSubscribers()) {
            lastSentMs.set(System.currentTimeMillis());
            wsHandler.broadcast(engine.snapshot(simulationId));
        }
    }

    @Override
    public void onRaceFinished(long simulationId) {
        if (wsHandler != null && wsHandler.hasSubscribers()) {
            lastSentMs.set(System.currentTimeMillis());
            wsHandler.broadcast(engine.snapshot(simulationId));
        }
    }
}
