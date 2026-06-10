package it.unicam.cs.mpgc.rpg125716.frontend.controller.menu;

import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import it.unicam.cs.mpgc.rpg125716.service.GameService;

import java.util.Objects;

public class MainMenuController {
    private final GameService gameService;

    public MainMenuController(GameService gameService) {
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
    }

    public CurrentGameState startNewGame() {
        return gameService.newGame();
    }
}
