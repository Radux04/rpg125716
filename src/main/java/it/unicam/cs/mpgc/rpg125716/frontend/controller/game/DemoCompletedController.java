package it.unicam.cs.mpgc.rpg125716.frontend.controller.game;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.util.Objects;

public class DemoCompletedController {
    private final SceneNavigator sceneNavigator;

    public DemoCompletedController(SceneNavigator sceneNavigator) {
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
    }

    @FXML
    private void handleBackToMenu() {
        sceneNavigator.showMainMenu();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}
