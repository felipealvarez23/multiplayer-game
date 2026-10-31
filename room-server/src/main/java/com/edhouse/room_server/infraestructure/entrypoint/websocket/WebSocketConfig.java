package com.edhouse.room_server.infraestructure.entrypoint.websocket;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

@Configuration
public class WebSocketConfig {

	@Bean
	public HandlerMapping webSocketHandlerMapping(RoomWebSocketHandler roomWebSocketHandler) {
		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping(Map.of("/ws/room", roomWebSocketHandler));
		mapping.setOrder(-1);
		return mapping;
	}
}
