package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class LoadService {
    private final SaveService saveService;

    public LoadService() {
        this(new SaveService());
    }

    public LoadService(SaveService saveService) {
        this.saveService = Objects.requireNonNull(saveService, "saveService cannot be null");
    }

    public Optional<LoadedGameSession> loadFromSlot(SaveSlot saveSlot) {
        Objects.requireNonNull(saveSlot, "saveSlot cannot be null");

        return saveService.loadGame(saveSlot)
                .map(gameStateLog -> toLoadedSession(saveSlot, gameStateLog));
    }

    public LoadedGameSession requireLoadedSession(SaveSlot saveSlot) {
        return loadFromSlot(saveSlot)
                .orElseThrow(() -> new IllegalStateException("no save present in slot " + saveSlot));
    }

    private LoadedGameSession toLoadedSession(SaveSlot saveSlot, GameStateLog gameStateLog) {
        gameStateLog.synchronizePlayerInventory();
        gameStateLog.validate();

        Player player = new Player(gameStateLog.getPlayer());
        DemoCampaign campaign = rebuildCampaign(gameStateLog);

        return new LoadedGameSession(
                saveSlot,
                player,
                campaign,
                gameStateLog.getCompletedLevels(),
                GameStateLog.copyOf(gameStateLog),
                LocalDateTime.now()
        );
    }

    private DemoCampaign rebuildCampaign(GameStateLog gameStateLog) {
        DemoCampaign campaign = new DemoCampaign();
        int currentLevelNumber = gameStateLog.getCurrentLevel();

        for (int levelNumber = 1; levelNumber < currentLevelNumber; levelNumber++) {
            campaign.getLevel(levelNumber).markCompletedForRestore();
        }

        DemoLevel currentLevel = campaign.moveToLevel(currentLevelNumber);
        currentLevel.restoreFromLevelState(gameStateLog.getCurrentLevelState());

        return campaign;
    }
}
