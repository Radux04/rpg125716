package it.unicam.cs.mpgc.rpg125716.model.level;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class DemoLevel {
    private final int number;
    private final String name;
    private final String description;
    private final boolean tutorial;
    private final boolean bossFight;
    private final boolean unlocksElementChoice;
    private final boolean endsDemoWithVictory;
    private final List<Enemy> enemies;
    private final Item completionDrop;
    private final Map<LevelRewardChoice, Item> rewardChoices;
    private boolean completionDropClaimed;
    private boolean rewardClaimed;

    public DemoLevel(
            int number,
            String name,
            String description,
            boolean tutorial,
            boolean bossFight,
            boolean unlocksElementChoice,
            boolean endsDemoWithVictory,
            List<Enemy> enemies,
            Item completionDrop,
            Map<LevelRewardChoice, Item> rewardChoices
    ) {
        if (number <= 0) {
            throw new IllegalArgumentException("number must be positive");
        }

        this.number = number;
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.tutorial = tutorial;
        this.bossFight = bossFight;
        this.unlocksElementChoice = unlocksElementChoice;
        this.endsDemoWithVictory = endsDemoWithVictory;
        this.enemies = List.copyOf(Objects.requireNonNull(enemies, "enemies cannot be null"));
        this.completionDrop = completionDrop;
        this.rewardChoices = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(rewardChoices, "rewardChoices cannot be null"))
        );

        if (this.enemies.isEmpty()) {
            throw new IllegalArgumentException("a level must contain at least one enemy");
        }
    }

    public boolean isCompleted() {
        return enemies.stream().noneMatch(Enemy::isAlive);
    }

    public int getRemainingEnemies() {
        return (int) enemies.stream()
                .filter(Enemy::isAlive)
                .count();
    }

    public boolean hasCompletionDrop() {
        return completionDrop != null;
    }

    public boolean hasRewardChoices() {
        return !rewardChoices.isEmpty();
    }

    public Item grantCompletionDrop(Player player) {
        ensureLevelCompleted();

        if (completionDrop == null) {
            throw new IllegalStateException("this level has no completion drop");
        }

        if (completionDropClaimed) {
            throw new IllegalStateException("completion drop already claimed");
        }

        player.collectItem(completionDrop);
        completionDropClaimed = true;
        return completionDrop;
    }

    public Item chooseReward(Player player, LevelRewardChoice rewardChoice) {
        ensureLevelCompleted();

        if (rewardChoices.isEmpty()) {
            throw new IllegalStateException("this level has no reward choices");
        }

        if (rewardClaimed) {
            throw new IllegalStateException("reward already claimed");
        }

        Item reward = rewardChoices.get(Objects.requireNonNull(rewardChoice, "rewardChoice cannot be null"));
        if (reward == null) {
            throw new IllegalArgumentException("rewardChoice is not available for this level");
        }

        player.collectItem(reward);
        rewardClaimed = true;
        return reward;
    }

    private void ensureLevelCompleted() {
        if (!isCompleted()) {
            throw new IllegalStateException("the level is not completed yet");
        }
    }
}
