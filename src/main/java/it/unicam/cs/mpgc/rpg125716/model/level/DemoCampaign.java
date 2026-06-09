package it.unicam.cs.mpgc.rpg125716.model.level;

import it.unicam.cs.mpgc.rpg125716.model.enemy.BossEnemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Goblin;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Skeleton;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Slime;
import it.unicam.cs.mpgc.rpg125716.model.item.BossSword;
import it.unicam.cs.mpgc.rpg125716.model.item.Helmet;
import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class DemoCampaign {
    private final List<DemoLevel> levels;
    private int currentLevelIndex;

    public DemoCampaign() {
        this.levels = List.of(
                createFreshLevel(1),
                createFreshLevel(2),
                createFreshLevel(3)
        );
    }

    public static DemoLevel createFreshLevel(int levelNumber) {
        return switch (levelNumber) {
            case 1 -> createLevel1();
            case 2 -> createLevel2();
            case 3 -> createLevel3();
            default -> throw new IllegalArgumentException("unsupported level number: " + levelNumber);
        };
    }

    public DemoLevel getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    public boolean hasNextLevel() {
        return currentLevelIndex < levels.size() - 1;
    }

    public DemoLevel advanceToNextLevel() {
        if (!getCurrentLevel().isCompleted()) {
            throw new IllegalStateException("the current level is not completed yet");
        }

        if (!hasNextLevel()) {
            throw new IllegalStateException("there are no more levels in the demo");
        }

        currentLevelIndex++;
        return getCurrentLevel();
    }

    public boolean isDemoCompleted() {
        return currentLevelIndex == levels.size() - 1 && getCurrentLevel().isCompleted();
    }

    private static DemoLevel createLevel1() {
        return new DemoLevel(
                1,
                "Livello 1 - Tutorial",
                "Un solo nemico introduce il combattimento e lascia la Pietra dell'Origine.",
                true,
                false,
                true,
                false,
                List.of(new Slime()),
                new OriginStone(),
                Map.of()
        );
    }

    private static DemoLevel createLevel2() {
        Goblin goblin = new Goblin();
        goblin.setDetectionRange(4);
        goblin.setChasesPlayerWhenDetected(true);

        Skeleton skeleton = new Skeleton();
        skeleton.setDetectionRange(5);
        skeleton.setChasesPlayerWhenDetected(true);

        Map<LevelRewardChoice, it.unicam.cs.mpgc.rpg125716.model.item.Item> rewards = new LinkedHashMap<>();
        rewards.put(
                LevelRewardChoice.HEALING_POTION,
                new Potion("Pozione curativa", "Permette al player di recuperare punti vita.", 25)
        );
        rewards.put(
                LevelRewardChoice.DEFENSE_HELMET,
                new Helmet()
        );

        return new DemoLevel(
                2,
                "Livello 2 - Inseguimento",
                "Almeno due nemici inseguono il player nel loro raggio di rilevamento e rilasciano XP.",
                false,
                false,
                false,
                false,
                List.of(goblin, skeleton),
                null,
                rewards
        );
    }

    private static DemoLevel createLevel3() {
        BossEnemy bossEnemy = new BossEnemy();
        bossEnemy.setDetectionRange(6);
        bossEnemy.setChasesPlayerWhenDetected(true);

        return new DemoLevel(
                3,
                "Livello 3 - Bossfight",
                "Il boss finale e piu forte dei nemici normali e conclude la demo con una vittoria.",
                false,
                true,
                false,
                true,
                List.of(bossEnemy),
                new BossSword(),
                Map.of()
        );
    }
}
