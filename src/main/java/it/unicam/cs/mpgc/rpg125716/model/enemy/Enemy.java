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

    protected Enemy(String name, int hp, int attack, int defense, int experienceReward, int goldReward) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.experienceReward = experienceReward;
        this.goldReward = goldReward;
    }
}
