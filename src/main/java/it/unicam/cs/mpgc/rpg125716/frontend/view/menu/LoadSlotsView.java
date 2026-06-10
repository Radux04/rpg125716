package it.unicam.cs.mpgc.rpg125716.frontend.view.menu;

import it.unicam.cs.mpgc.rpg125716.frontend.controller.menu.LoadSlotsController;
import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlotInfo;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LoadSlotsView {
    private static final DateTimeFormatter SAVE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALY);

    private final LoadSlotsController controller;
    private final SceneNavigator sceneNavigator;
    private final BorderPane root;
    private final Label feedbackLabel = new Label();

    public LoadSlotsView(LoadSlotsController controller, SceneNavigator sceneNavigator) {
        this.controller = Objects.requireNonNull(controller, "controller cannot be null");
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
        this.root = buildRoot();
    }

    public Parent getRoot() {
        return root;
    }

    private BorderPane buildRoot() {
        BorderPane container = new BorderPane();
        container.getStyleClass().add("app-root");
        container.setPadding(new Insets(36));

        VBox content = new VBox(18);
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Carica Partita");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Scegli uno slot occupato per riprendere la tua run.");
        subtitle.getStyleClass().add("body-label");
        subtitle.setWrapText(true);

        VBox slotList = new VBox(14);
        slotList.setMaxWidth(760);
        List<SaveSlotInfo> slotInfos = controller.listSaveSlots();
        slotInfos.stream()
                .map(this::createSlotCard)
                .forEach(slotList.getChildren()::add);

        feedbackLabel.getStyleClass().add("feedback-label");
        feedbackLabel.setWrapText(true);

        Button backButton = new Button("Indietro");
        backButton.getStyleClass().addAll("menu-button", "secondary-button");
        backButton.setOnAction(event -> sceneNavigator.showMainMenu());

        content.getChildren().addAll(title, subtitle, slotList, feedbackLabel, backButton);
        container.setCenter(content);
        return container;
    }

    private HBox createSlotCard(SaveSlotInfo saveSlotInfo) {
        HBox card = new HBox(18);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(22));
        card.getStyleClass().add("slot-card");
        card.getStyleClass().add(saveSlotInfo.isOccupied() ? "slot-card-filled" : "slot-card-empty");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnMouseClicked(event -> handleSlotSelection(saveSlotInfo));

        VBox details = new VBox(6);
        Label slotLabel = new Label("Slot " + saveSlotInfo.getSlot().getSlotId());
        slotLabel.getStyleClass().add("slot-title");

        Label slotStateLabel = new Label(saveSlotInfo.isOccupied() ? "Salvataggio disponibile" : "Vuoto");
        slotStateLabel.getStyleClass().add(saveSlotInfo.isOccupied() ? "slot-state-filled" : "slot-state-empty");

        Label slotInfoLabel = new Label(buildSlotDescription(saveSlotInfo));
        slotInfoLabel.getStyleClass().add("body-label");
        slotInfoLabel.setWrapText(true);

        details.getChildren().addAll(slotLabel, slotStateLabel, slotInfoLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label slotActionLabel = new Label(saveSlotInfo.isOccupied() ? "Carica" : "Non disponibile");
        slotActionLabel.getStyleClass().add("slot-action-label");

        card.getChildren().addAll(details, spacer, slotActionLabel);
        return card;
    }

    private String buildSlotDescription(SaveSlotInfo saveSlotInfo) {
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

    private void handleSlotSelection(SaveSlotInfo saveSlotInfo) {
        if (!saveSlotInfo.isOccupied()) {
            feedbackLabel.setText("Slot " + saveSlotInfo.getSlot().getSlotId()
                    + ": non esiste ancora un salvataggio.");
            return;
        }

        try {
            CurrentGameState currentGameState = controller.loadGame(saveSlotInfo.getSlot());
            feedbackLabel.setText("");
            sceneNavigator.showGameOverview(currentGameState);
        } catch (RuntimeException exception) {
            feedbackLabel.setText("Impossibile caricare lo slot " + saveSlotInfo.getSlot().getSlotId()
                    + ": " + exception.getMessage());
        }
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
