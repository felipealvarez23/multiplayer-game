package com.edhouse.room_server.domain.model;

public record Avatar(String id, int x, int y) {

	public Avatar {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("id requerido");
		}
		if (x < 0 || y < 0) {
			throw new IllegalArgumentException("posición inválida");
		}
	}

	public Avatar moveTo(int x, int y) {
		return new Avatar(id, x, y);
	}
}
