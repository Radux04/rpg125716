package it.unicam.cs.mpgc.rpg125716.frontend.view.game;

import it.unicam.cs.mpgc.rpg125716.frontend.controller.game.GameOverviewController;
import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class GameOverviewView {
    private final GameOverviewController controller;
    private final SceneNavigator sceneNavigator;
    private final BorderPane root;
    private final String initialFeedbackMessage;
    private CurrentGameState currentGameState;
    private Label feedbackLabel;

    public GameOverviewView(
            GameOverviewController controller,
            SceneNavigator sceneNavigator,
            CurrentGameState currentGameState,
            String initialFeedbackMessage
    ) {
        this.controller = Objects.requireNonNull(controller, "controller cannot be null");
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
        this.currentGameState = Objects.requireNonNull(currentGameState, "currentGameState cannot be null");
        this.initialFeedbackMessage = initialFeedbackMessage;
        this.root = buildRoot();
    }

    public Parent getRoot() {
        return root;
    }

    private BorderPane buildRoot() {
        BorderPane container = new BorderPane();
        container.getStyleClass().add("app-root");
        container.setPadding(new Insets(32));

        VBox content = new VBox(22);
        content.getChildren().addAll(
                buildHeader(),
                buildHeroCard(),
                buildLevelCard(),
                buildSaveCard(),
                buildFooter()
        );

        container.setCenter(content);
        return container;
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(6);
        Label title = new Label("Run Avviata");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label(buildSubtitle());
        subtitle.getStyleClass().add("body-label");
        subtitle.setWrapText(true);

        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label slotBadge = new Label(buildSlotBadge());
        slotBadge.getStyleClass().add("slot-badge");

        header.getChildren().addAll(titles, spacer, slotBadge);
        return header;
    }

    private VBox buildHeroCard() {
        VBox card = createCard();

        Label title = new Label("Eroe");
        title.getStyleClass().add("card-title");

        FlowPane stats = new FlowPane();
        stats.setHgap(12);
        stats.setVgap(12);

        stats.getChildren().addAll(
                createStatPill("Nome", currentGameState.getPlayer().getName()),
                createStatPill("HP", currentGameState.getPlayer().getCurrentHp() + "/" + currentGameState.getPlayer().getMaxHp()),
                createStatPill("Attacco", String.valueOf(currentGameState.getPlayer().getAttack())),
                createStatPill("Difesa", String.valueOf(currentGameState.getPlayer().getDefense())),
                createStatPill("Velocita", String.valueOf(currentGameState.getPlayer().getSpeed())),
                createStatPill(
                        "Elemento",
                        currentGameState.getPlayer().getElementType() == null
                                ? "Non scelto"
                                : currentGameState.getPlayer().getElementType().name()
                )
        );

        card.getChildren().addAll(title, stats);
        return card;
    }

    private VBox buildLevelCard() {
        VBox card = createCard();

        Label title = new Label(currentGameState.getCurrentLevel().getName());
        title.getStyleClass().add("card-title");

        Label description = new Label(currentGameState.getCurrentLevel().getDescription());
        description.getStyleClass().add("body-label");
        description.setWrapText(true);

        FlowPane badges = new FlowPane();
        badges.setHgap(12);
        badges.setVgap(12);
        badges.getChildren().addAll(
                createStatPill("Nemici rimasti", String.valueOf(currentGameState.getCurrentLevel().getRemainingEnemies())),
                createStatPill("Stato livello", currentGameState.isCurrentLevelStarted() ? "In corso" : "Pronto"),
                createStatPill("Livelli completati", String.valueOf(currentGameState.getCompletedLevels().size()))
        );

        Label flags = new Label(buildLevelFlags());
        flags.getStyleClass().add("muted-label");
        flags.setWrapText(true);

        card.getChildren().addAll(title, description, badges, flags);
        return card;
    }

    private VBox buildSaveCard() {
        VBox card = createCard();

        Label title = new Label("Salvataggi");
        title.getStyleClass().add("card-title");

        Label description = new Label("Puoi fissare subito la run su uno dei tre slot disponibili.");
        description.getStyleClass().add("body-label");
        description.setWrapText(true);

        HBox actions = new HBox(10);
        actions.getChildren().addAll(
                createSaveButton("Salva Slot 1", SaveSlot.SLOT_1),
                createSaveButton("Salva Slot 2", SaveSlot.SLOT_2),
                createSaveButton("Salva Slot 3", SaveSlot.SLOT_3)
        );

        feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("feedback-label");
        feedbackLabel.setWrapText(true);
        if (initialFeedbackMessage != null && !initialFeedbackMessage.isBlank()) {
            feedbackLabel.setText(initialFeedbackMessage);
        }

        card.getChildren().addAll(title, description, actions, feedbackLabel);
        return card;
    }

    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        Button startLevelButton = new Button(currentGameState.isCurrentLevelStarted() ? "Livello in Corso" : "Inizia Livello");
        startLevelButton.getStyleClass().addAll("menu-button", "primary-button");
        startLevelButton.setDisable(currentGameState.isCurrentLevelStarted() || currentGameState.isDemoCompleted());
        startLevelButton.setOnAction(event -> handleStartLevel());

        Button backButton = new Button("Torna al Menu");
        backButton.getStyleClass().addAll("menu-button", "secondary-button");
        backButton.setOnAction(event -> sceneNavigator.showMainMenu());

        footer.getChildren().addAll(startLevelButton, backButton);
        return footer;
    }

    private void handleStartLevel() {
        try {
            currentGameState = controller.startCurrentLevel();
            sceneNavigator.showGameOverview(currentGameState);
        } catch (RuntimeException exception) {
            feedbackLabel.setText("Impossibile iniziare il livello: " + exception.getMessage());
        }
    }

    private Button createSaveButton(String text, SaveSlot saveSlot) {
        Button button = new Button(text);
        button.getStyleClass().addAll("menu-button", "secondary-button");
        button.setOnAction(event -> handleSave(saveSlot));
        return button;
    }

    private void handleSave(SaveSlot saveSlot) {
        try {
            currentGameState = controller.saveCurrentGame(saveSlot);
            sceneNavigator.showGameOverview(
                    currentGameState,
                    "Partita salvata nello slot " + saveSlot.getSlotId() + "."
            );
        } catch (RuntimeException exception) {
            feedbackLabel.setText("Salvataggio fallito: " + exception.getMessage());
        }
    }

    private String buildSubtitle() {
        if (currentGameState.isDemoCompleted()) {
            return "La demo e completa. Puoi comunque rivedere il riepilogo o salvare la run.";
        }

        return "La sessione backend e pronta. Da qui puoi entrare nel livello corrente o salvare la partita.";
    }

    private String buildSlotBadge() {
        return currentGameState.getSaveSlot() == null
                ? "Nuova Run"
                : "Slot " + currentGameState.getSaveSlot().getSlotId();
    }

    private String buildLevelFlags() {
        StringBuilder builder = new StringBuilder();
        if (currentGameState.getCurrentLevel().isTutorial()) {
            builder.append("Tutorial attivo. ");
        }
        if (currentGameState.getCurrentLevel().isBossFight()) {
            builder.append("Bossfight. ");
        }
        if (currentGameState.getCurrentLevel().isUnlocksElementChoice()) {
            builder.append("Questo livello sblocca la scelta dell'elemento. ");
        }
        if (currentGameState.getCurrentLevel().isEndsDemoWithVictory()) {
            builder.append("Completandolo concludi la demo. ");
        }

        if (builder.isEmpty()) {
            builder.append("Nessun vincolo speciale per questo livello.");
        }

        return builder.toString().trim();
    }

    private static VBox createCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(22));
        card.getStyleClass().add("content-card");
        return card;
    }

    private static VBox createStatPill(String labelText, String valueText) {
        VBox pill = new VBox(4);
        pill.setPadding(new Insets(10, 14, 10, 14));
        pill.getStyleClass().add("stat-pill");

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-pill-label");

        Label value = new Label(valueText);
        value.getStyleClass().add("stat-pill-value");

        pill.getChildren().addAll(label, value);
        return pill;
    }
}
