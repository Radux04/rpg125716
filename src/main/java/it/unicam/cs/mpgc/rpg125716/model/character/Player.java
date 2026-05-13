package it.unicam.cs.mpgc.rpg125716.model.character;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Player {
    private String name;
    private int level;
    private int experience;
    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private int gold;
    private List<String> inventory;

    public Player(String name, int maxHp, int attack, int defense) {
        this.name = name;
        this.level = 1;
        this.experience = 0;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.gold = 0;
        this.inventory = new ArrayList<>();
    }

    public void takeDamage(int damage) {
        int effectiveDamage = damage - defense;
        if (effectiveDamage <= 0) {
            effectiveDamage = 1;
        }
        currentHp = currentHp - effectiveDamage;
    }

    public void heal(int amount) {
        if (amount > 0) {
            currentHp = Math.min(maxHp, currentHp + amount);
        }
    }

    public void gainExperience(int amount) {
        if (amount > 0) {
            experience += amount;

            while (experience >= experienceToNextLevel()) {
                experience -= experienceToNextLevel();
                levelUp();
            }
        }
    }

    public void levelUp() {
        level++;
        maxHp += 10;
        currentHp += 10;
        attack += 2;
        defense += 1;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    private int experienceToNextLevel() {
        return level * 100;
    }

}
