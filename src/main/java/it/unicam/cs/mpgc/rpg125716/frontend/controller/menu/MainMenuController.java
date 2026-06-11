package it.unicam.cs.mpgc.rpg125716.frontend.controller.menu;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Optional;
import java.util.Objects;

public class MainMenuController {
    private static final String DEFAULT_PLAYER_NAME = "Hero";

    private final SceneNavigator sceneNavigator;
    private final GameService gameService;

    public MainMenuController(SceneNavigator sceneNavigator, GameService gameService) {
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
    }

    @FXML
    private void handleNewGame() {
        promptPlayerName().ifPresent(playerName -> sceneNavigator.showGameOverview(gameService.newGame(playerName)));
    }

    @FXML
    private void handleLoadGame() {
        sceneNavigator.showLoadSlots();
    }

    private Optional<String> promptPlayerName() {
        Dialog<String> dialog = buildPlayerNameDialog();

        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return Optional.empty();
            }

            String playerName = result.get().trim();
            if (!playerName.isEmpty()) {
                return Optional.of(playerName);
            }

            showInvalidNameWarning();
        }
    }

    private Dialog<String> buildPlayerNameDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Nuova Partita");

        DialogPane dialogPane = dialog.getDialogPane();
        applyDialogTheme(dialogPane);

        ButtonType confirmButtonType = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        Label titleLabel = new Label("Scegli il nome del tuo eroe");
        titleLabel.getStyleClass().add("dialog-title");

        Label descriptionLabel = new Label("Inserisci il nome del protagonista della nuova run.");
        descriptionLabel.getStyleClass().add("dialog-body");
        descriptionLabel.setWrapText(true);

        TextField nameField = new TextField(DEFAULT_PLAYER_NAME);
        nameField.getStyleClass().add("dialog-text-field");
        nameField.setPromptText("Nome dell'eroe");
        nameField.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(12, titleLabel, descriptionLabel, nameField);
        content.getStyleClass().add("dialog-content");
        content.setFillWidth(true);
        dialogPane.setContent(content);

        Button confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
        confirmButton.getStyleClass().addAll("menu-button", "primary-button", "dialog-action-button");

        Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
        cancelButton.getStyleClass().addAll("menu-button", "secondary-button", "dialog-action-button");

        dialog.setResultConverter(buttonType -> buttonType == confirmButtonType ? nameField.getText() : null);
        dialog.setOnShown(event -> Platform.runLater(() -> {
            nameField.requestFocus();
            nameField.selectAll();
        }));

        return dialog;
    }

    private void showInvalidNameWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Nome non valido");
        alert.setHeaderText(null);
        alert.setContentText("Inserisci un nome per il tuo eroe.");
        applyDialogTheme(alert.getDialogPane());

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.getStyleClass().addAll("menu-button", "primary-button", "dialog-action-button");

        alert.showAndWait();
    }

    private static void applyDialogTheme(DialogPane dialogPane) {
        dialogPane.getStylesheets().add(resolveStylesheet());
        if (!dialogPane.getStyleClass().contains("themed-dialog")) {
            dialogPane.getStyleClass().add("themed-dialog");
        }
        dialogPane.setPrefWidth(460);
    }

    private static String resolveStylesheet() {
        URL stylesheetUrl = MainMenuController.class.getResource("/styles/the-forgotten-gate.css");
        if (stylesheetUrl == null) {
            throw new IllegalStateException("stylesheet /styles/the-forgotten-gate.css not found");
        }

        return stylesheetUrl.toExternalForm();
    }
}
