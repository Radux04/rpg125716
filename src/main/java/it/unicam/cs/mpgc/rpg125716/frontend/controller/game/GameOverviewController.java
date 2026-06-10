package it.unicam.cs.mpgc.rpg125716.frontend.controller.game;

import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import it.unicam.cs.mpgc.rpg125716.service.GameService;

import java.util.Objects;

public class GameOverviewController {
    private final GameService gameService;

    public GameOverviewController(GameService gameService) {
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
    }

    public CurrentGameState startCurrentLevel() {
        return gameService.startLevel();
    }

    public CurrentGameState saveCurrentGame(SaveSlot saveSlot) {
        return gameService.saveCurrentGame(Objects.requireNonNull(saveSlot, "saveSlot cannot be null"));
    }
}
