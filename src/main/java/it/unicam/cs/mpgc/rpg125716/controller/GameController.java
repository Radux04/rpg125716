package it.unicam.cs.mpgc.rpg125716.controller;

import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlotInfo;
import it.unicam.cs.mpgc.rpg125716.service.LoadService;
import it.unicam.cs.mpgc.rpg125716.service.LoadedGameSession;
import it.unicam.cs.mpgc.rpg125716.service.SaveService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GameController {
    private final SaveService saveService;
    private final LoadService loadService;
    private LoadedGameSession currentSession;

    public GameController() {
        this(new SaveService(), new LoadService());
    }

    public GameController(SaveService saveService, LoadService loadService) {
        this.saveService = Objects.requireNonNull(saveService, "saveService cannot be null");
        this.loadService = Objects.requireNonNull(loadService, "loadService cannot be null");
    }

    public List<SaveSlotInfo> listSaveSlots() {
        return saveService.listSlots();
    }

    public Optional<LoadedGameSession> loadGame(SaveSlot saveSlot) {
        Optional<LoadedGameSession> loadedGameSession = loadService.loadFromSlot(saveSlot);
        currentSession = loadedGameSession.orElse(null);
        return loadedGameSession;
    }

    public GameStateLog saveCurrentGame(SaveSlot saveSlot) {
        Objects.requireNonNull(saveSlot, "saveSlot cannot be null");

        LoadedGameSession session = requireCurrentSession();
        List<String> completedLevels = deriveCompletedLevels(session.getCampaign());
        GameStateLog gameStateLog = GameStateLog.fromCurrentGame(
                saveSlot.getSlotId(),
                session.getPlayer(),
                session.getCampaign(),
                completedLevels
        );

        saveService.saveGame(gameStateLog, saveSlot);

        currentSession = new LoadedGameSession(
                saveSlot,
                session.getPlayer(),
                session.getCampaign(),
                completedLevels,
                GameStateLog.copyOf(gameStateLog),
                LocalDateTime.now()
        );

        return GameStateLog.copyOf(gameStateLog);
    }

    public boolean deleteSave(SaveSlot saveSlot) {
        return saveService.deleteSave(saveSlot);
    }

    public Optional<LoadedGameSession> getCurrentSession() {
        return Optional.ofNullable(currentSession);
    }

    public LoadedGameSession requireCurrentSession() {
        if (currentSession == null) {
            throw new IllegalStateException("no game session is currently loaded");
        }

        return currentSession;
    }

    public void clearCurrentSession() {
        currentSession = null;
    }

    private List<String> deriveCompletedLevels(DemoCampaign campaign) {
        return campaign.getLevels().stream()
                .filter(DemoLevel::isCompleted)
                .map(DemoLevel::getName)
                .toList();
    }
}
