package com.edhouse.room_server.domain.model;

public record ClientCommand(CommandType type, Integer x, Integer y, String message) {
}
