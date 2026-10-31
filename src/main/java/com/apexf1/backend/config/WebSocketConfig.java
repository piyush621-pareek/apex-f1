package com.apexf1.backend.config;

import com.apexf1.backend.simulation.SimulationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers the live telemetry WebSocket endpoint at {@code /ws/simulations}.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SimulationWebSocketHandler simulationWebSocketHandler;

    public WebSocketConfig(SimulationWebSocketHandler simulationWebSocketHandler) {
        this.simulationWebSocketHandler = simulationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(simulationWebSocketHandler, "/ws/simulations")
                .setAllowedOrigins("*");
    }
}
