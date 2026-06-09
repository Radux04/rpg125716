package it.unicam.cs.mpgc.rpg125716.event;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.BossEnemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;

import java.time.LocalDateTime;
import java.util.Objects;

public record EnemyDefeatedEvent(Player player, Enemy enemy, LocalDateTime occurredAt) implements GameEvent {
    public EnemyDefeatedEvent {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(enemy, "enemy cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
    }

    public EnemyDefeatedEvent(Player player, Enemy enemy) {
        this(player, enemy, LocalDateTime.now());
    }

    public boolean isBossDefeat() {
        return enemy instanceof BossEnemy;
    }
}
