package com.apexf1.backend.simulation;

import com.apexf1.backend.simulation.dto.SimulationSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active WebSocket connections for live Race Simulation telemetry streaming.
 * Clients connect to {@code ws://localhost:8080/ws/simulations?simId=1001} and
 * receive pushed {@link SimulationSnapshot} JSON frames whenever cars advance or complete laps.
 */
@Component
public class SimulationWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;

    public SimulationWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
    }

    public boolean hasSubscribers() {
        return !sessions.isEmpty();
    }

    /** Push a live snapshot to all matching WebSocket clients. */
    public void broadcast(SimulationSnapshot snapshot) {
        if (sessions.isEmpty() || snapshot == null) {
            return;
        }
        String payload;
        try {
            payload = objectMapper.writeValueAsString(snapshot);
        } catch ( Exception e) {
            return;
        }
        TextMessage message = new TextMessage(payload);
        String targetSimParam = "simId=" + snapshot.simulationId;

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }
            URI uri = session.getUri();
            String query = (uri != null) ? uri.getQuery() : null;
            if (query != null && query.contains("simId=") && !query.contains(targetSimParam)) {
                continue;
            }
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                }
            } catch (IOException e) {
                sessions.remove(session);
            }
        }
    }
}
