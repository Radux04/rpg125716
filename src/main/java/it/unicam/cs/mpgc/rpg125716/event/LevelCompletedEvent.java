package it.unicam.cs.mpgc.rpg125716.event;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;

import java.time.LocalDateTime;
import java.util.Objects;

public record LevelCompletedEvent(Player player, int levelNumber, String levelName, LocalDateTime occurredAt) implements GameEvent {
    public LevelCompletedEvent {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(levelName, "levelName cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");

        if (levelNumber <= 0) {
            throw new IllegalArgumentException("levelNumber must be positive");
        }
    }

    public LevelCompletedEvent(Player player, DemoLevel level) {
        this(
                player,
                Objects.requireNonNull(level, "level cannot be null").getNumber(),
                level.getName(),
                LocalDateTime.now()
        );
    }
}
