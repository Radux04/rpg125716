package it.unicam.cs.mpgc.rpg125716.frontend.scene;

import it.unicam.cs.mpgc.rpg125716.frontend.controller.game.GameOverviewController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.menu.LoadSlotsController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.menu.MainMenuController;
import it.unicam.cs.mpgc.rpg125716.frontend.view.game.GameOverviewView;
import it.unicam.cs.mpgc.rpg125716.frontend.view.menu.LoadSlotsView;
import it.unicam.cs.mpgc.rpg125716.frontend.view.menu.MainMenuView;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class SceneNavigator {
    private final Stage stage;
    private final GameService gameService;
    private final double sceneWidth;
    private final double sceneHeight;
    private final String stylesheet;

    public SceneNavigator(Stage stage, GameService gameService, double sceneWidth, double sceneHeight) {
        this.stage = Objects.requireNonNull(stage, "stage cannot be null");
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.stylesheet = resolveStylesheet();
    }

    public void showMainMenu() {
        MainMenuView view = new MainMenuView(new MainMenuController(gameService), this);
        show(view.getRoot());
    }

    public void showLoadSlots() {
        LoadSlotsView view = new LoadSlotsView(new LoadSlotsController(gameService), this);
        show(view.getRoot());
    }

    public void showGameOverview(CurrentGameState currentGameState) {
        showGameOverview(currentGameState, null);
    }

    public void showGameOverview(CurrentGameState currentGameState, String feedbackMessage) {
        GameOverviewView view = new GameOverviewView(
                new GameOverviewController(gameService),
                this,
                currentGameState,
                feedbackMessage
        );
        show(view.getRoot());
    }

    private void show(Parent root) {
        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        scene.getStylesheets().add(stylesheet);
        stage.setScene(scene);
    }

    private String resolveStylesheet() {
        URL stylesheetUrl = SceneNavigator.class.getResource("/styles/the-forgotten-gate.css");
        if (stylesheetUrl == null) {
            throw new IllegalStateException("stylesheet /styles/the-forgotten-gate.css not found");
        }

        return stylesheetUrl.toExternalForm();
    }
}
