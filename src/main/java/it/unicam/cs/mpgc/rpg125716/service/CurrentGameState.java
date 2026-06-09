package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class CurrentGameState {
    private final SaveSlot saveSlot;
    private final Player player;
    private final DemoCampaign campaign;
    private final DemoLevel currentLevel;
    private final List<String> completedLevels;
    private final boolean currentLevelStarted;
    private final boolean demoCompleted;

    public CurrentGameState(
            SaveSlot saveSlot,
            Player player,
            DemoCampaign campaign,
            DemoLevel currentLevel,
            List<String> completedLevels,
            boolean currentLevelStarted,
            boolean demoCompleted
    ) {
        this.saveSlot = saveSlot;
        this.player = Objects.requireNonNull(player, "player cannot be null");
        this.campaign = Objects.requireNonNull(campaign, "campaign cannot be null");
        this.currentLevel = Objects.requireNonNull(currentLevel, "currentLevel cannot be null");
        this.completedLevels = List.copyOf(Objects.requireNonNull(completedLevels, "completedLevels cannot be null"));
        this.currentLevelStarted = currentLevelStarted;
        this.demoCompleted = demoCompleted;
    }

    public static CurrentGameState fromSession(LoadedGameSession session, boolean currentLevelStarted) {
        Objects.requireNonNull(session, "session cannot be null");
        DemoCampaign campaign = session.getCampaign();

        return new CurrentGameState(
                session.getSaveSlot(),
                session.getPlayer(),
                campaign,
                campaign.getCurrentLevel(),
                campaign.getLevels().stream()
                        .filter(DemoLevel::isCompleted)
                        .map(DemoLevel::getName)
                        .toList(),
                currentLevelStarted,
                campaign.isDemoCompleted()
        );
    }
}
