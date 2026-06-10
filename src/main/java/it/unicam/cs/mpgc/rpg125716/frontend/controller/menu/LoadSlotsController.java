package it.unicam.cs.mpgc.rpg125716.frontend.controller.menu;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlotInfo;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LoadSlotsController {
    private static final DateTimeFormatter SAVE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALY);

    private final SceneNavigator sceneNavigator;
    private final GameService gameService;

    @FXML
    private HBox slot1Card;
    @FXML
    private HBox slot2Card;
    @FXML
    private HBox slot3Card;
    @FXML
    private Label slot1StateLabel;
    @FXML
    private Label slot2StateLabel;
    @FXML
    private Label slot3StateLabel;
    @FXML
    private Label slot1DetailsLabel;
    @FXML
    private Label slot2DetailsLabel;
    @FXML
    private Label slot3DetailsLabel;
    @FXML
    private Label slot1ActionLabel;
    @FXML
    private Label slot2ActionLabel;
    @FXML
    private Label slot3ActionLabel;
    @FXML
    private Label feedbackLabel;

    public LoadSlotsController(SceneNavigator sceneNavigator, GameService gameService) {
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
    }

    @FXML
    private void initialize() {
        List<SaveSlotInfo> slotInfos = gameService.listSaveSlots();
        applySlotState(SaveSlot.SLOT_1, slotInfos);
        applySlotState(SaveSlot.SLOT_2, slotInfos);
        applySlotState(SaveSlot.SLOT_3, slotInfos);
    }

    @FXML
    private void handleSlot1Selection() {
        handleSlotSelection(SaveSlot.SLOT_1);
    }

    @FXML
    private void handleSlot2Selection() {
        handleSlotSelection(SaveSlot.SLOT_2);
    }

    @FXML
    private void handleSlot3Selection() {
        handleSlotSelection(SaveSlot.SLOT_3);
    }

    @FXML
    private void handleBack() {
        sceneNavigator.showMainMenu();
    }

    private void handleSlotSelection(SaveSlot saveSlot) {
        SaveSlotInfo saveSlotInfo = findSlotInfo(saveSlot);
        if (!saveSlotInfo.isOccupied()) {
            feedbackLabel.setText("Slot " + saveSlot.getSlotId() + ": non esiste ancora un salvataggio.");
            return;
        }

        try {
            feedbackLabel.setText("");
            sceneNavigator.showGameOverview(gameService.loadGame(saveSlot));
        } catch (RuntimeException exception) {
            feedbackLabel.setText("Impossibile caricare lo slot " + saveSlot.getSlotId()
                    + ": " + exception.getMessage());
        }
    }

    private SaveSlotInfo findSlotInfo(SaveSlot saveSlot) {
        return gameService.listSaveSlots().stream()
                .filter(slotInfo -> slotInfo.getSlot() == saveSlot)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing slot info for " + saveSlot));
    }

    private void applySlotState(SaveSlot saveSlot, List<SaveSlotInfo> slotInfos) {
        SaveSlotInfo saveSlotInfo = slotInfos.stream()
                .filter(slotInfo -> slotInfo.getSlot() == saveSlot)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing slot info for " + saveSlot));

        HBox slotCard = getSlotCard(saveSlot);
        Label slotStateLabel = getSlotStateLabel(saveSlot);
        Label slotDetailsLabel = getSlotDetailsLabel(saveSlot);
        Label slotActionLabel = getSlotActionLabel(saveSlot);

        slotCard.getStyleClass().removeAll("slot-card-filled", "slot-card-empty");
        slotCard.getStyleClass().add(saveSlotInfo.isOccupied() ? "slot-card-filled" : "slot-card-empty");

        slotStateLabel.getStyleClass().removeAll("slot-state-filled", "slot-state-empty");
        slotStateLabel.getStyleClass().add(saveSlotInfo.isOccupied() ? "slot-state-filled" : "slot-state-empty");
        slotStateLabel.setText(saveSlotInfo.isOccupied() ? "Salvataggio disponibile" : "Vuoto");

        slotActionLabel.setText(saveSlotInfo.isOccupied() ? "Carica" : "Non disponibile");
        slotDetailsLabel.setText(buildSlotDescription(saveSlotInfo));
    }

    private HBox getSlotCard(SaveSlot saveSlot) {
        return switch (saveSlot) {
            case SLOT_1 -> slot1Card;
            case SLOT_2 -> slot2Card;
            case SLOT_3 -> slot3Card;
        };
    }

    private Label getSlotStateLabel(SaveSlot saveSlot) {
        return switch (saveSlot) {
            case SLOT_1 -> slot1StateLabel;
            case SLOT_2 -> slot2StateLabel;
            case SLOT_3 -> slot3StateLabel;
        };
    }

    private Label getSlotDetailsLabel(SaveSlot saveSlot) {
        return switch (saveSlot) {
            case SLOT_1 -> slot1DetailsLabel;
            case SLOT_2 -> slot2DetailsLabel;
            case SLOT_3 -> slot3DetailsLabel;
        };
    }

    private Label getSlotActionLabel(SaveSlot saveSlot) {
        return switch (saveSlot) {
            case SLOT_1 -> slot1ActionLabel;
            case SLOT_2 -> slot2ActionLabel;
            case SLOT_3 -> slot3ActionLabel;
        };
    }

    private static String buildSlotDescription(SaveSlotInfo saveSlotInfo) {
        if (!saveSlotInfo.isOccupied()) {
            return "Non esiste ancora un salvataggio in questo slot.";
        }

        String lastSavedAt = saveSlotInfo.getLastSavedAt() == null
                ? "data sconosciuta"
                : SAVE_DATE_FORMATTER.format(saveSlotInfo.getLastSavedAt());

        return "Nome player: " + valueOrFallback(saveSlotInfo.getPlayerName(), "Sconosciuto")
                + System.lineSeparator()
                + "Livello corrente: " + valueOrFallback(saveSlotInfo.getCurrentLevel())
                + System.lineSeparator()
                + "Data ultimo salvataggio: " + lastSavedAt;
    }

    private static String valueOrFallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }

    private static String valueOrFallback(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
