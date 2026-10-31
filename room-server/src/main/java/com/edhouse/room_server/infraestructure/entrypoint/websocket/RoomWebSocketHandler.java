package com.edhouse.room_server.infraestructure.entrypoint.websocket;

import com.edhouse.room_server.domain.model.ClientCommand;
import com.edhouse.room_server.domain.service.CommandDispatcher;
import com.edhouse.room_server.domain.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RoomWebSocketHandler implements WebSocketHandler {

	private final JsonMapper jsonMapper;
	private static final Logger log = LoggerFactory.getLogger(RoomWebSocketHandler.class);
	private final RoomService roomService;
	private final CommandDispatcher commandDispatcher;

	public RoomWebSocketHandler(RoomService roomService, CommandDispatcher commandDispatcher, JsonMapper jsonMapper) {
		this.roomService = roomService;
		this.jsonMapper = jsonMapper;
		this.commandDispatcher = commandDispatcher;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		String userId = session.getId();
		log.info("Cliente conectado: {}", userId);

		Mono<Void> input = session.receive()
				.map(WebSocketMessage::getPayloadAsText)
				.doOnNext(payload -> {
					try {
						ClientCommand command = jsonMapper.readValue(payload, ClientCommand.class);
						commandDispatcher.process(userId, command);
					} catch (Exception error) {
						log.warn("No se pudo procesar un comando WebSocket de {}", userId, error);
					}
				})
				.then();

		Mono<Void> output = session.send(
				roomService.join(userId)
						.map(event -> session
								.textMessage(jsonMapper.writeValueAsString(event)))
		);

		return Mono.when(input, output)
				.then()
				.doFinally(signal -> {
					roomService.leave(userId);
					log.info("Cliente desconectado: {} (señal: {})", userId, signal);
				});
	}
}
