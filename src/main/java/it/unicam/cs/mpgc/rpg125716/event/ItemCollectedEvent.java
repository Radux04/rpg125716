package it.unicam.cs.mpgc.rpg125716.event;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;

import java.time.LocalDateTime;
import java.util.Objects;

public record ItemCollectedEvent(Player player, Item item, int totalCollectedItems, LocalDateTime occurredAt) implements GameEvent {
    public ItemCollectedEvent {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(item, "item cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");

        if (totalCollectedItems < 0) {
            throw new IllegalArgumentException("totalCollectedItems cannot be negative");
        }
    }

    public ItemCollectedEvent(Player player, Item item, int totalCollectedItems) {
        this(player, item, totalCollectedItems, LocalDateTime.now());
    }
}
