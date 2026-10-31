package com.edhouse.room_server.domain.service;

import com.edhouse.room_server.domain.model.ClientCommand;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CommandDispatcher {

    private final RoomService roomService;

    public CommandDispatcher(RoomService roomService) {
        this.roomService = roomService;
    }

    public Mono<Void> process(String userId, ClientCommand command) {

        switch (command.type()) {
            case MOVE -> roomService.move(userId,command.x(),command.y());
            case MESSAGE -> roomService.send(userId,command.message());
        }

        return Mono.empty();
    }

}
