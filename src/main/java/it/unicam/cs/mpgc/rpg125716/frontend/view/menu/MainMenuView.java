package it.unicam.cs.mpgc.rpg125716.frontend.view.menu;

import it.unicam.cs.mpgc.rpg125716.frontend.controller.menu.MainMenuController;
import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class MainMenuView {
    private final MainMenuController controller;
    private final SceneNavigator sceneNavigator;
    private final StackPane root;

    public MainMenuView(MainMenuController controller, SceneNavigator sceneNavigator) {
        this.controller = Objects.requireNonNull(controller, "controller cannot be null");
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
        this.root = buildRoot();
    }

    public Parent getRoot() {
        return root;
    }

    private StackPane buildRoot() {
        StackPane container = new StackPane();
        container.getStyleClass().add("app-root");

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(420);
        panel.setPadding(new Insets(48));
        panel.getStyleClass().add("menu-panel");

        Label eyebrow = new Label("RPG Demo");
        eyebrow.getStyleClass().add("eyebrow-label");

        Label title = new Label("The Forgotten Gate");
        title.getStyleClass().add("game-title");
        title.setWrapText(true);

        Label subtitle = new Label("Varca il portale. Scegli se iniziare una nuova run o riaprire un salvataggio.");
        subtitle.getStyleClass().add("body-label");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        Button newGameButton = createPrimaryButton("Nuova Partita");
        newGameButton.setOnAction(event -> handleNewGame());

        Button loadGameButton = createSecondaryButton("Carica Partita");
        loadGameButton.setOnAction(event -> sceneNavigator.showLoadSlots());

        panel.getChildren().addAll(eyebrow, title, subtitle, newGameButton, loadGameButton);
        container.getChildren().add(panel);
        return container;
    }

    private void handleNewGame() {
        CurrentGameState currentGameState = controller.startNewGame();
        sceneNavigator.showGameOverview(currentGameState);
    }

    private static Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().addAll("menu-button", "primary-button");
        return button;
    }

    private static Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().addAll("menu-button", "secondary-button");
        return button;
    }
}
