package it.unicam.cs.mpgc.rpg125716.frontend.controller.menu;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.fxml.FXML;

import java.util.Objects;

public class MainMenuController {
    private final SceneNavigator sceneNavigator;
    private final GameService gameService;

    public MainMenuController(SceneNavigator sceneNavigator, GameService gameService) {
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
    }

    @FXML
    private void handleNewGame() {
        sceneNavigator.showGameOverview(gameService.newGame());
    }

    @FXML
    private void handleLoadGame() {
        sceneNavigator.showLoadSlots();
    }
}
