package it.unicam.cs.mpgc.rpg125716.persistence;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;
import it.unicam.cs.mpgc.rpg125716.model.level.LevelRewardChoice;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
public class LevelState {
    private int levelNumber;
    private String levelName;
    private boolean tutorial;
    private boolean bossFight;
    private boolean unlocksElementChoice;
    private boolean endsDemoWithVictory;
    private boolean completed;
    private boolean completionDropClaimed;
    private boolean rewardClaimed;
    private boolean restartFromBeginningOnLoad;
    private String completionDropName;

    @JacksonXmlElementWrapper(localName = "reward-options")
    @JacksonXmlProperty(localName = "reward-option")
    private List<String> rewardOptions = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "enemy-states")
    @JacksonXmlProperty(localName = "enemy-state")
    private List<EnemyState> enemyStates = new ArrayList<>();

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
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.tutorial = tutorial;
        this.bossFight = bossFight;
        this.unlocksElementChoice = unlocksElementChoice;
        this.endsDemoWithVictory = endsDemoWithVictory;
        this.completed = completed;
        this.completionDropClaimed = completionDropClaimed;
        this.rewardClaimed = rewardClaimed;
        this.restartFromBeginningOnLoad = restartFromBeginningOnLoad;
        this.completionDropName = completionDropName;
        this.rewardOptions = rewardOptions == null ? new ArrayList<>() : new ArrayList<>(rewardOptions);
        this.enemyStates = enemyStates == null ? new ArrayList<>() : enemyStates.stream()
                .map(EnemyState::copyOf)
                .toList();
        validate();
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

    public static LevelState copyOf(LevelState other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new LevelState(
                other.levelNumber,
                other.levelName,
                other.tutorial,
                other.bossFight,
                other.unlocksElementChoice,
                other.endsDemoWithVictory,
                other.completed,
                other.completionDropClaimed,
                other.rewardClaimed,
                other.restartFromBeginningOnLoad,
                other.completionDropName,
                other.rewardOptions,
                other.enemyStates
        );
    }

    public void validate() {
        if (levelNumber <= 0) {
            throw new IllegalArgumentException("levelNumber must be positive");
        }

        if (levelName == null || levelName.isBlank()) {
            throw new IllegalArgumentException("levelName cannot be blank");
        }

        if (rewardOptions == null) {
            rewardOptions = new ArrayList<>();
        }

        if (enemyStates == null) {
            enemyStates = new ArrayList<>();
        }
    }
}
