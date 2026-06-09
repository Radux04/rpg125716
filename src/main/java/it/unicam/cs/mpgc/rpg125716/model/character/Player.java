package it.unicam.cs.mpgc.rpg125716.model.character;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unicam.cs.mpgc.rpg125716.model.item.Inventory;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;
import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import lombok.Data;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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
    private int speed;
    private Inventory inventory;
    private ElementType elementType;
    private ElementalPower elementalPower;
    private Set<AchievementType> unlockedAchievements;

    public Player() {
        this.inventory = new Inventory();
        this.unlockedAchievements = new LinkedHashSet<>();
    }

    public Player(String name, int maxHp, int attack, int defense, int speed) {
        this();
        this.name = name;
        this.level = 1;
        this.experience = 0;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.gold = 0;
        this.speed = speed;
    }

    public Player(Player other) {
        Objects.requireNonNull(other, "other cannot be null");
        this.name = other.name;
        this.level = other.level;
        this.experience = other.experience;
        this.maxHp = other.maxHp;
        this.currentHp = other.currentHp;
        this.attack = other.attack;
        this.defense = other.defense;
        this.gold = other.gold;
        this.speed = other.speed;
        this.inventory = new Inventory(other.inventory);
        this.elementType = other.elementType;
        this.elementalPower = copyElementalPower(other.elementalPower);
        this.unlockedAchievements = new LinkedHashSet<>(other.unlockedAchievements);
    }

    public void chooseElement(ElementType elementType) {
        if (this.elementType != null) {
            throw new IllegalStateException("the player has already chosen an element");
        }

        this.elementType = Objects.requireNonNull(elementType, "elementType cannot be null");
        this.elementalPower = ElementalPower.fromElementType(elementType);

        switch (elementType) {
            case FIRE -> {
                this.attack += 8;
                this.speed += 5;
            }
            case WATER -> {
                this.maxHp += 25;
                this.currentHp += 25;
                this.speed += 5;
            }
            case WIND -> this.speed += 20;
            case EARTH -> {
                this.defense += 5;
                this.attack += 2;
            }
        }
    }

    public void attuneToOriginStone(ElementType elementType) {
        if (!inventory.containsItem(new OriginStone())) {
            throw new IllegalStateException("the player does not possess the Pietra dell'Origine");
        }

        chooseElement(elementType);
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

    @JsonIgnore
    public boolean isAlive() {
        return currentHp > 0;
    }

    public void collectItem(Item item) {
        inventory.addItem(item);
    }

    public boolean removeItem(Item item) {
        return inventory.removeItem(item);
    }

    public boolean useItem(Item item) {
        return inventory.useItem(this, item);
    }

    public boolean unlockAchievement(AchievementType achievementType) {
        return unlockedAchievements.add(Objects.requireNonNull(achievementType, "achievementType cannot be null"));
    }

    public boolean hasAchievement(AchievementType achievementType) {
        return unlockedAchievements.contains(Objects.requireNonNull(achievementType, "achievementType cannot be null"));
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory == null ? new Inventory() : inventory;
    }

    public void setUnlockedAchievements(Set<AchievementType> unlockedAchievements) {
        this.unlockedAchievements = unlockedAchievements == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(unlockedAchievements);
    }

    public Set<AchievementType> getUnlockedAchievements() {
        return Collections.unmodifiableSet(unlockedAchievements);
    }

    private int experienceToNextLevel() {
        return level * 100;
    }

    private static ElementalPower copyElementalPower(ElementalPower elementalPower) {
        if (elementalPower == null
                || elementalPower.getElementType() == null
                || elementalPower.getName() == null
                || elementalPower.getDescription() == null) {
            return null;
        }

        return new ElementalPower(
                elementalPower.getElementType(),
                elementalPower.getName(),
                elementalPower.getDescription()
        );
    }

}
