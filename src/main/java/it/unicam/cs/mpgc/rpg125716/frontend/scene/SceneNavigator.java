package it.unicam.cs.mpgc.rpg125716.frontend.scene;

import it.unicam.cs.mpgc.rpg125716.frontend.controller.game.GameOverviewController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.game.GameViewController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.menu.LoadSlotsController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.menu.MainMenuController;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class SceneNavigator {
    private static final String MAIN_MENU_FXML = "/fxml/menu/main-menu.fxml";
    private static final String LOAD_SLOTS_FXML = "/fxml/menu/load-slots.fxml";
    private static final String GAME_OVERVIEW_FXML = "/fxml/game/game-overview.fxml";
    private static final String GAME_VIEW_FXML = "/fxml/game/game-view.fxml";

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
        show(MAIN_MENU_FXML, null, null);
    }

    public void showLoadSlots() {
        show(LOAD_SLOTS_FXML, null, null);
    }

    public void showGameOverview(CurrentGameState currentGameState) {
        showGameOverview(currentGameState, null);
    }

    public void showGameOverview(CurrentGameState currentGameState, String feedbackMessage) {
        show(GAME_OVERVIEW_FXML, currentGameState, feedbackMessage);
    }

    public void showGameView(CurrentGameState currentGameState) {
        showGameView(currentGameState, null);
    }

    public void showGameView(CurrentGameState currentGameState, String feedbackMessage) {
        show(GAME_VIEW_FXML, currentGameState, feedbackMessage);
    }

    private void show(String fxmlPath, CurrentGameState currentGameState, String feedbackMessage) {
        show(loadRoot(fxmlPath, currentGameState, feedbackMessage));
    }

    private Parent loadRoot(String fxmlPath, CurrentGameState currentGameState, String feedbackMessage) {
        URL resource = SceneNavigator.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalStateException("FXML resource not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(type -> instantiateController(type, currentGameState, feedbackMessage));

        try {
            return loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load FXML resource: " + fxmlPath, exception);
        }
    }

    private Object instantiateController(
            Class<?> controllerType,
            CurrentGameState currentGameState,
            String feedbackMessage
    ) {
        if (controllerType == MainMenuController.class) {
            return new MainMenuController(this, gameService);
        }

        if (controllerType == LoadSlotsController.class) {
            return new LoadSlotsController(this, gameService);
        }

        if (controllerType == GameOverviewController.class) {
            return new GameOverviewController(
                    this,
                    gameService,
                    Objects.requireNonNull(currentGameState, "currentGameState cannot be null"),
                    feedbackMessage
            );
        }

        if (controllerType == GameViewController.class) {
            return new GameViewController(
                    this,
                    gameService,
                    Objects.requireNonNull(currentGameState, "currentGameState cannot be null"),
                    feedbackMessage
            );
        }

        throw new IllegalArgumentException("Unsupported controller type: " + controllerType.getName());
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
