package it.unicam.cs.mpgc.rpg125716.frontend.controller.game;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.Objects;

public class GameOverviewController {
    private final SceneNavigator sceneNavigator;
    private final GameService gameService;
    private CurrentGameState currentGameState;
    private final String initialFeedbackMessage;

    @FXML
    private Label slotBadgeLabel;
    @FXML
    private Label playerNameValueLabel;
    @FXML
    private Label playerHpValueLabel;
    @FXML
    private Label playerAttackValueLabel;
    @FXML
    private Label playerDefenseValueLabel;
    @FXML
    private Label playerSpeedValueLabel;
    @FXML
    private Label playerElementValueLabel;
    @FXML
    private Label levelNameLabel;
    @FXML
    private Label levelDescriptionLabel;
    @FXML
    private Label remainingEnemiesValueLabel;
    @FXML
    private Label levelStatusValueLabel;
    @FXML
    private Label completedLevelsValueLabel;
    @FXML
    private Label levelFlagsLabel;
    @FXML
    private Label feedbackLabel;
    @FXML
    private Button startLevelButton;

    public GameOverviewController(
            SceneNavigator sceneNavigator,
            GameService gameService,
            CurrentGameState currentGameState,
            String initialFeedbackMessage
    ) {
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
        this.currentGameState = Objects.requireNonNull(currentGameState, "currentGameState cannot be null");
        this.initialFeedbackMessage = initialFeedbackMessage;
    }

    @FXML
    private void initialize() {
        refreshView();
    }

    @FXML
    private void handleStartLevel() {
        try {
            currentGameState = gameService.startLevel();
            sceneNavigator.showGameOverview(currentGameState);
        } catch (RuntimeException exception) {
            feedbackLabel.setText("Impossibile iniziare il livello: " + exception.getMessage());
        }
    }

    @FXML
    private void handleSaveSlot1() {
        handleSave(SaveSlot.SLOT_1);
    }

    @FXML
    private void handleSaveSlot2() {
        handleSave(SaveSlot.SLOT_2);
    }

    @FXML
    private void handleSaveSlot3() {
        handleSave(SaveSlot.SLOT_3);
    }

    @FXML
    private void handleBack() {
        sceneNavigator.showMainMenu();
    }

    private void handleSave(SaveSlot saveSlot) {
        try {
            currentGameState = gameService.saveCurrentGame(Objects.requireNonNull(saveSlot, "saveSlot cannot be null"));
            sceneNavigator.showGameOverview(
                    currentGameState,
                    "Partita salvata nello slot " + saveSlot.getSlotId() + "."
            );
        } catch (RuntimeException exception) {
            feedbackLabel.setText("Salvataggio fallito: " + exception.getMessage());
        }
    }

    private void refreshView() {
        slotBadgeLabel.setText(buildSlotBadge());

        playerNameValueLabel.setText(currentGameState.getPlayer().getName());
        playerHpValueLabel.setText(currentGameState.getPlayer().getCurrentHp() + "/" + currentGameState.getPlayer().getMaxHp());
        playerAttackValueLabel.setText(String.valueOf(currentGameState.getPlayer().getAttack()));
        playerDefenseValueLabel.setText(String.valueOf(currentGameState.getPlayer().getDefense()));
        playerSpeedValueLabel.setText(String.valueOf(currentGameState.getPlayer().getSpeed()));
        playerElementValueLabel.setText(
                currentGameState.getPlayer().getElementType() == null
                        ? "Non scelto"
                        : currentGameState.getPlayer().getElementType().name()
        );

        levelNameLabel.setText(currentGameState.getCurrentLevel().getName());
        levelDescriptionLabel.setText(currentGameState.getCurrentLevel().getDescription());
        remainingEnemiesValueLabel.setText(String.valueOf(currentGameState.getCurrentLevel().getRemainingEnemies()));
        levelStatusValueLabel.setText(currentGameState.isCurrentLevelStarted() ? "In corso" : "Pronto");
        completedLevelsValueLabel.setText(String.valueOf(currentGameState.getCompletedLevels().size()));
        levelFlagsLabel.setText(buildLevelFlags());

        startLevelButton.setText(currentGameState.isCurrentLevelStarted() ? "Livello in Corso" : "Inizia Livello");
        startLevelButton.setDisable(currentGameState.isCurrentLevelStarted() || currentGameState.isDemoCompleted());

        if (initialFeedbackMessage != null && !initialFeedbackMessage.isBlank()) {
            feedbackLabel.setText(initialFeedbackMessage);
        }
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
}
