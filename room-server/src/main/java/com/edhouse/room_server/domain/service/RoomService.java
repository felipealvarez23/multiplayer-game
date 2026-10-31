package com.edhouse.room_server.domain.service;

import com.edhouse.room_server.domain.model.Avatar;
import com.edhouse.room_server.domain.model.RoomEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);
    private static final int GRID_SIZE = 10;

    private final Map<String, Avatar> avatars = new HashMap<>();

    private final Sinks.Many<RoomEvent> roomEventSink = Sinks.many().multicast().directBestEffort();

    public synchronized Flux<RoomEvent> join(String userId) {
        Avatar avatar = createAvatarAtRandomPosition(userId);

        avatars.put(userId, avatar);

        roomEventSink.tryEmitNext(new RoomEvent("joined", userId, avatar, null, null));

        var snapshot = new RoomEvent("join", userId, avatar, List.copyOf(avatars.values()), null);

        return Flux.concat(Mono.just(snapshot), roomEventSink.asFlux());
    }

    public synchronized void leave(String userId) {
        Avatar avatar = avatars.remove(userId);
        if (avatar != null) {
            roomEventSink.tryEmitNext(
                    new RoomEvent("left", userId, avatar, null, null)
            );
        }
    }

    public synchronized boolean move(String userId, int x, int y) {
        long startedAt = System.nanoTime();
        try {
            Avatar current = avatars.get(userId);

            if (current == null || x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE) {
                return false;
            }

            int deltaX = Math.abs(current.x() - x);
            int deltaY = Math.abs(current.y() - y);

            if (Math.max(deltaX, deltaY) != 1 || isOccupied(x, y)) {
                return false;
            }

            Avatar moved = current.moveTo(x, y);
            avatars.put(userId, moved);
            roomEventSink.tryEmitNext(new RoomEvent("moved", userId, moved, null, null));
            return true;
        } finally {
            long elapsedNanos = System.nanoTime() - startedAt;
            double elapsedMillis = elapsedNanos / 1_000_000.0;
            log.debug("session: {} - event: MOVE - message: Tiempo de ejecución {} ms", userId, elapsedMillis);
        }
    }

    public synchronized void send(String userId, String text) {
        Avatar avatar = avatars.get(userId);
        if (avatar == null || text == null || text.isBlank()) {
            return;
        }

        String message = text.trim();
        if (message.length() > 120) {
            message = message.substring(0, 120);
        }

        roomEventSink.tryEmitNext(new RoomEvent("message", userId, avatar, null, message));
    }

    private Avatar createAvatarAtRandomPosition(String userId) {
        int x;
        int y;

        do {
            x = ThreadLocalRandom.current().nextInt(GRID_SIZE);
            y = ThreadLocalRandom.current().nextInt(GRID_SIZE);
        } while (isOccupied(x, y));

        return new Avatar(userId, x, y);
    }

    private boolean isOccupied(int x, int y) {
        return avatars.values().stream()
                .anyMatch(avatar -> avatar.x() == x && avatar.y() == y);
    }
}
