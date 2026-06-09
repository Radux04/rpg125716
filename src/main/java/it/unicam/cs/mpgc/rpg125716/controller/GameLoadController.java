package it.unicam.cs.mpgc.rpg125716.controller;

import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.service.LoadService;
import it.unicam.cs.mpgc.rpg125716.service.LoadedGameSession;

import java.util.Objects;
import java.util.Optional;

public class GameLoadController {
    private final LoadService loadService;
    private LoadedGameSession currentSession;

    public GameLoadController() {
        this(new LoadService());
    }

    public GameLoadController(LoadService loadService) {
        this.loadService = Objects.requireNonNull(loadService, "loadService cannot be null");
    }

    public Optional<LoadedGameSession> loadSelectedSlot(SaveSlot saveSlot) {
        Optional<LoadedGameSession> loadedGameSession = loadService.loadFromSlot(saveSlot);
        currentSession = loadedGameSession.orElse(null);
        return loadedGameSession;
    }

    public LoadedGameSession requireCurrentSession() {
        if (currentSession == null) {
            throw new IllegalStateException("no loaded session is currently available");
        }

        return currentSession;
    }

    public Optional<LoadedGameSession> getCurrentSession() {
        return Optional.ofNullable(currentSession);
    }

    public void clearCurrentSession() {
        currentSession = null;
    }
}
