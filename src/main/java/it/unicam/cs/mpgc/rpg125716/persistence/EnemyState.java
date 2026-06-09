package it.unicam.cs.mpgc.rpg125716.persistence;

import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class EnemyState {
    private String enemyType;
    private String name;
    private int startingHp;
    private int attack;
    private int defense;
    private int experienceReward;
    private int goldReward;
    private int detectionRange;
    private boolean chasesPlayerWhenDetected;

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
        this.enemyType = enemyType;
        this.name = name;
        this.startingHp = startingHp;
        this.attack = attack;
        this.defense = defense;
        this.experienceReward = experienceReward;
        this.goldReward = goldReward;
        this.detectionRange = detectionRange;
        this.chasesPlayerWhenDetected = chasesPlayerWhenDetected;
        validate();
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

    public static EnemyState copyOf(EnemyState other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new EnemyState(
                other.enemyType,
                other.name,
                other.startingHp,
                other.attack,
                other.defense,
                other.experienceReward,
                other.goldReward,
                other.detectionRange,
                other.chasesPlayerWhenDetected
        );
    }

    public void validate() {
        if (enemyType == null || enemyType.isBlank()) {
            throw new IllegalArgumentException("enemyType cannot be blank");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        if (startingHp < 0) {
            throw new IllegalArgumentException("startingHp cannot be negative");
        }
    }
}
