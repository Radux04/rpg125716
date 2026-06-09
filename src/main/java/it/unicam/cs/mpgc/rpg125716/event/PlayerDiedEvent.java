package it.unicam.cs.mpgc.rpg125716.event;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;

import java.time.LocalDateTime;
import java.util.Objects;

public record PlayerDiedEvent(Player player, LocalDateTime occurredAt) implements GameEvent {
    public PlayerDiedEvent {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
    }

    public PlayerDiedEvent(Player player) {
        this(player, LocalDateTime.now());
    }
}
