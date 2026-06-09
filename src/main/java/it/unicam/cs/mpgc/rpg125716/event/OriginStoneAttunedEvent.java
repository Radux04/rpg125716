package it.unicam.cs.mpgc.rpg125716.event;

import it.unicam.cs.mpgc.rpg125716.model.character.ElementType;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;

import java.time.LocalDateTime;
import java.util.Objects;

public record OriginStoneAttunedEvent(Player player, ElementType elementType, LocalDateTime occurredAt) implements GameEvent {
    public OriginStoneAttunedEvent {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(elementType, "elementType cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
    }

    public OriginStoneAttunedEvent(Player player, ElementType elementType) {
        this(player, elementType, LocalDateTime.now());
    }
}
