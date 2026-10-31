package com.edhouse.room_server.domain.model;

import java.util.List;

public record RoomEvent(
        String type,
        String userId,
        Avatar avatar,
        List<Avatar> avatars,
        String message) {
}
