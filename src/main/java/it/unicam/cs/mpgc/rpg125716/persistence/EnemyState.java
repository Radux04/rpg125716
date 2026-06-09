package it.unicam.cs.mpgc.rpg125716.persistence;

import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@EqualsAndHashCode
@ToString
public class EnemyState {
    private final String enemyType;
    private final String name;
    private final int startingHp;
    private final int attack;
    private final int defense;
    private final int experienceReward;
    private final int goldReward;
    private final int detectionRange;
    private final boolean chasesPlayerWhenDetected;

    public EnemyState(
            String enemyType,
            String name,
            int startingHp,
            int attack,
            int defense,
            int experienceReward,
            int goldReward,
            int detectionRange,
            boolean chasesPlayerWhenDetected
    ) {
        this.enemyType = Objects.requireNonNull(enemyType, "enemyType cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");

        if (startingHp <= 0) {
            throw new IllegalArgumentException("startingHp must be positive");
        }
        this.startingHp = startingHp;

        this.attack = attack;
        this.defense = defense;
        this.experienceReward = experienceReward;
        this.goldReward = goldReward;
        this.detectionRange = detectionRange;
        this.chasesPlayerWhenDetected = chasesPlayerWhenDetected;
    }

    public static EnemyState fromEnemy(Enemy enemy) {
        Objects.requireNonNull(enemy, "enemy cannot be null");
        return new EnemyState(
                enemy.getClass().getSimpleName(),
                enemy.getName(),
                enemy.getHp(),
                enemy.getAttack(),
                enemy.getDefense(),
                enemy.getExperienceReward(),
                enemy.getGoldReward(),
                enemy.getDetectionRange(),
                enemy.isChasesPlayerWhenDetected()
        );
    }
}
