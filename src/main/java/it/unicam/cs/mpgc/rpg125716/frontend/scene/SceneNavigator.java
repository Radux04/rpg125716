package it.unicam.cs.mpgc.rpg125716.frontend.scene;

import it.unicam.cs.mpgc.rpg125716.frontend.controller.game.DemoCompletedController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.game.GameOverviewController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.game.GameViewController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.menu.LoadSlotsController;
import it.unicam.cs.mpgc.rpg125716.frontend.controller.menu.MainMenuController;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class SceneNavigator {
    private static final String MAIN_MENU_FXML = "/fxml/menu/main-menu.fxml";
    private static final String LOAD_SLOTS_FXML = "/fxml/menu/load-slots.fxml";
    private static final String GAME_OVERVIEW_FXML = "/fxml/game/game-overview.fxml";
    private static final String GAME_VIEW_FXML = "/fxml/game/game-view.fxml";
    private static final String DEMO_COMPLETED_FXML = "/fxml/game/demo-completed.fxml";

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

    public void showLevelTransitionToGameOverview(
            CurrentGameState currentGameState,
            String feedbackMessage,
            String progressionSummary
    ) {
        Parent root = loadRoot(GAME_OVERVIEW_FXML, currentGameState, feedbackMessage);
        showWithLevelTransition(root, currentGameState.getCurrentLevel().getName(), progressionSummary);
    }

    public void showGameView(CurrentGameState currentGameState) {
        showGameView(currentGameState, null);
    }

    public void showGameView(CurrentGameState currentGameState, String feedbackMessage) {
        show(GAME_VIEW_FXML, currentGameState, feedbackMessage);
    }

    public void showDemoCompleted() {
        Parent root = loadRoot(DEMO_COMPLETED_FXML, null, null);
        showWithDemoCompletionTransition(root);
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

        if (controllerType == DemoCompletedController.class) {
            return new DemoCompletedController(this);
        }

        throw new IllegalArgumentException("Unsupported controller type: " + controllerType.getName());
    }

    private void show(Parent root) {
        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        scene.getStylesheets().add(stylesheet);
        stage.setScene(scene);
    }

    private void showWithLevelTransition(Parent root, String levelTitle, String progressionSummary) {
        StackPane sceneRoot = new StackPane(root);
        StackPane transitionOverlay = buildTransitionOverlay(
                "Nuovo Livello",
                levelTitle,
                progressionSummary,
                "Premi qualsiasi tasto per continuare..."
        );
        sceneRoot.getChildren().add(transitionOverlay);

        Scene scene = new Scene(sceneRoot, sceneWidth, sceneHeight);
        scene.getStylesheets().add(stylesheet);
        stage.setScene(scene);

        playLevelTransition(scene, transitionOverlay);
    }

    private void showWithDemoCompletionTransition(Parent root) {
        StackPane sceneRoot = new StackPane(root);
        StackPane transitionOverlay = buildTransitionOverlay(
                "Demo completata",
                "Forgotten Gate",
                "Hai sconfitto il suo guardiano.",
                null
        );
        sceneRoot.getChildren().add(transitionOverlay);

        Scene scene = new Scene(sceneRoot, sceneWidth, sceneHeight);
        scene.getStylesheets().add(stylesheet);
        stage.setScene(scene);

        playAutomaticTransition(transitionOverlay);
    }

    private StackPane buildTransitionOverlay(
            String eyebrowText,
            String titleText,
            String summaryText,
            String continueText
    ) {
        Label eyebrowLabel = new Label(eyebrowText);
        eyebrowLabel.getStyleClass().add("level-transition-eyebrow");

        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("level-transition-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(900);

        VBox content = new VBox(14, eyebrowLabel, titleLabel);
        content.setAlignment(Pos.CENTER);

        if (summaryText != null && !summaryText.isBlank()) {
            Label summaryLabel = new Label(summaryText);
            summaryLabel.getStyleClass().add("level-transition-summary");
            summaryLabel.setWrapText(true);
            summaryLabel.setMaxWidth(820);
            content.getChildren().add(summaryLabel);
        }

        BorderPane overlayLayout = new BorderPane();
        overlayLayout.setCenter(content);
        BorderPane.setAlignment(content, Pos.CENTER);

        if (continueText != null && !continueText.isBlank()) {
            Label continueLabel = new Label(continueText);
            continueLabel.getStyleClass().add("level-transition-continue");
            overlayLayout.setBottom(continueLabel);
            BorderPane.setAlignment(continueLabel, Pos.BOTTOM_CENTER);
            BorderPane.setMargin(continueLabel, new Insets(0, 0, 40, 0));
        }

        overlayLayout.setOpacity(0);

        StackPane overlay = new StackPane(overlayLayout);
        overlay.getStyleClass().add("level-transition-overlay");
        overlay.setOpacity(1);
        overlay.setPickOnBounds(true);
        overlay.setUserData(overlayLayout);
        return overlay;
    }

    private void playLevelTransition(Scene scene, StackPane transitionOverlay) {
        BorderPane overlayLayout = (BorderPane) transitionOverlay.getUserData();

        FadeTransition contentFadeIn = new FadeTransition(Duration.millis(220), overlayLayout);
        contentFadeIn.setFromValue(0);
        contentFadeIn.setToValue(1);
        contentFadeIn.play();

        scene.addEventFilter(KeyEvent.KEY_PRESSED, new javafx.event.EventHandler<>() {
            @Override
            public void handle(KeyEvent event) {
                event.consume();
                scene.removeEventFilter(KeyEvent.KEY_PRESSED, this);

                FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(260), transitionOverlay);
                overlayFadeOut.setFromValue(1);
                overlayFadeOut.setToValue(0);
                overlayFadeOut.setOnFinished(fadeEvent -> {
                    if (transitionOverlay.getParent() instanceof StackPane parent) {
                        parent.getChildren().remove(transitionOverlay);
                    }
                });
                overlayFadeOut.play();
            }
        });
    }

    private void playAutomaticTransition(StackPane transitionOverlay) {
        BorderPane overlayLayout = (BorderPane) transitionOverlay.getUserData();

        FadeTransition contentFadeIn = new FadeTransition(Duration.millis(240), overlayLayout);
        contentFadeIn.setFromValue(0);
        contentFadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.millis(1100));

        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(420), transitionOverlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setOnFinished(event -> {
            if (transitionOverlay.getParent() instanceof StackPane parent) {
                parent.getChildren().remove(transitionOverlay);
            }
        });

        new SequentialTransition(contentFadeIn, hold, overlayFadeOut).play();
    }

    private String resolveStylesheet() {
        URL stylesheetUrl = SceneNavigator.class.getResource("/styles/the-forgotten-gate.css");
        if (stylesheetUrl == null) {
            throw new IllegalStateException("stylesheet /styles/the-forgotten-gate.css not found");
        }

        return stylesheetUrl.toExternalForm();
    }
}
