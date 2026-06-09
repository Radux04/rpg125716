package it.unicam.cs.mpgc.rpg125716.model.enemy;

import lombok.Data;

@Data
public abstract class Enemy {
    private String name;
    private int hp;
    private int attack;
    private int defense;
    private int experienceReward;
    private int goldReward;
    private int detectionRange;
    private boolean chasesPlayerWhenDetected;

    protected Enemy(String name, int hp, int attack, int defense, int experienceReward, int goldReward) {
        this(name, hp, attack, defense, experienceReward, goldReward, 0, false);
    }

    protected Enemy(
            String name,
            int hp,
            int attack,
            int defense,
            int experienceReward,
            int goldReward,
            int detectionRange,
            boolean chasesPlayerWhenDetected
    ) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.experienceReward = experienceReward;
        this.goldReward = goldReward;
        setDetectionRange(detectionRange);
        this.chasesPlayerWhenDetected = chasesPlayerWhenDetected;
    }

    public void setDetectionRange(int detectionRange) {
        if (detectionRange < 0) {
            throw new IllegalArgumentException("detectionRange cannot be negative");
        }

        this.detectionRange = detectionRange;
    }

    public boolean canDetectPlayer(int distanceFromPlayer) {
        return distanceFromPlayer >= 0 && distanceFromPlayer <= detectionRange;
    }

    public boolean shouldChasePlayer(int distanceFromPlayer) {
        return chasesPlayerWhenDetected && canDetectPlayer(distanceFromPlayer);
    }

    public boolean isAlive() {
        return hp > 0;
    }
}
