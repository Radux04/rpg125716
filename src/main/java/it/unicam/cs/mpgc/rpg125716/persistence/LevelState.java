package it.unicam.cs.mpgc.rpg125716.persistence;

import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;
import it.unicam.cs.mpgc.rpg125716.model.level.LevelRewardChoice;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

@Getter
@EqualsAndHashCode
@ToString
public class LevelState {
    private final int levelNumber;
    private final String levelName;
    private final boolean tutorial;
    private final boolean bossFight;
    private final boolean unlocksElementChoice;
    private final boolean endsDemoWithVictory;
    private final boolean completed;
    private final boolean completionDropClaimed;
    private final boolean rewardClaimed;
    private final boolean restartFromBeginningOnLoad;
    private final String completionDropName;
    private final List<String> rewardOptions;
    private final List<EnemyState> enemyStates;

    public LevelState(
            int levelNumber,
            String levelName,
            boolean tutorial,
            boolean bossFight,
            boolean unlocksElementChoice,
            boolean endsDemoWithVictory,
            boolean completed,
            boolean completionDropClaimed,
            boolean rewardClaimed,
            boolean restartFromBeginningOnLoad,
            String completionDropName,
            List<String> rewardOptions,
            List<EnemyState> enemyStates
    ) {
        if (levelNumber <= 0) {
            throw new IllegalArgumentException("levelNumber must be positive");
        }
        this.levelNumber = levelNumber;
        this.levelName = Objects.requireNonNull(levelName, "levelName cannot be null");
        this.tutorial = tutorial;
        this.bossFight = bossFight;
        this.unlocksElementChoice = unlocksElementChoice;
        this.endsDemoWithVictory = endsDemoWithVictory;
        this.completed = completed;
        this.completionDropClaimed = completionDropClaimed;
        this.rewardClaimed = rewardClaimed;
        this.restartFromBeginningOnLoad = restartFromBeginningOnLoad;
        this.completionDropName = completionDropName;
        this.rewardOptions = List.copyOf(Objects.requireNonNull(rewardOptions, "rewardOptions cannot be null"));
        this.enemyStates = List.copyOf(Objects.requireNonNull(enemyStates, "enemyStates cannot be null"));
    }

    public static LevelState fromLevel(DemoLevel level, boolean restartFromBeginningOnLoad) {
        Objects.requireNonNull(level, "level cannot be null");

        List<String> rewardOptions = level.getRewardChoices().keySet().stream()
                .map(LevelRewardChoice::getLabel)
                .toList();

        List<EnemyState> enemyStates = level.getEnemies().stream()
                .map(EnemyState::fromEnemy)
                .toList();

        return new LevelState(
                level.getNumber(),
                level.getName(),
                level.isTutorial(),
                level.isBossFight(),
                level.isUnlocksElementChoice(),
                level.isEndsDemoWithVictory(),
                level.isCompleted(),
                level.isCompletionDropClaimed(),
                level.isRewardClaimed(),
                restartFromBeginningOnLoad,
                level.getCompletionDrop() == null ? null : level.getCompletionDrop().getName(),
                rewardOptions,
                enemyStates
        );
    }

    public static LevelState restartSnapshotForLevel(int levelNumber) {
        DemoLevel pristineLevel = DemoCampaign.createFreshLevel(levelNumber);
        return fromLevel(pristineLevel, true);
    }
}
