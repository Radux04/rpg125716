package it.unicam.cs.mpgc.rpg125716.frontend.controller.game;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.model.character.ElementType;
import it.unicam.cs.mpgc.rpg125716.model.enemy.BossEnemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Goblin;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Skeleton;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Slime;
import it.unicam.cs.mpgc.rpg125716.model.item.Armor;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;
import it.unicam.cs.mpgc.rpg125716.model.item.ItemType;
import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import it.unicam.cs.mpgc.rpg125716.model.item.Weapon;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;
import it.unicam.cs.mpgc.rpg125716.model.level.LevelRewardChoice;
import it.unicam.cs.mpgc.rpg125716.service.CombatResult;
import it.unicam.cs.mpgc.rpg125716.service.CombatTurnResult;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import it.unicam.cs.mpgc.rpg125716.service.GameService;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.Scene;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GameViewController {
    private static final double BOARD_WIDTH = 1720;
    private static final double BOARD_HEIGHT = 620;
    private static final double TILE_SIZE = 102;
    private static final Point2D PLAYER_POSITION = new Point2D(200, BOARD_HEIGHT / 2);
    private static final Point2D ITEM_POSITION = new Point2D((BOARD_WIDTH / 2) - 46, BOARD_HEIGHT - 165);
    private static final Point2D DOOR_POSITION = new Point2D(BOARD_WIDTH - 170, 82);
    private static final Color TILE_COLOR = Color.web("#1e1826");
    private static final Color TILE_ALT_COLOR = Color.web("#231c2d");
    private static final Color WALL_COLOR = Color.web("#0f0c14");
    private static final Color PLAYER_COLOR = Color.web("#d4af37");
    private static final Color PLAYER_INNER_COLOR = Color.web("#6b3fa0");
    private static final Color DROP_COLOR = Color.web("#d4af37");
    private static final Color REWARD_COLOR = Color.web("#6b3fa0");
    private static final Color DOOR_LOCKED_COLOR = Color.web("#41304d");
    private static final Color DOOR_OPEN_COLOR = Color.web("#d4af37");

    private final SceneNavigator sceneNavigator;
    private final GameService gameService;
    private CurrentGameState currentGameState;
    private final EventHandler<KeyEvent> globalKeyHandler = this::handleGlobalKeyPressed;

    private final Map<Enemy, StackPane> enemyNodes = new LinkedHashMap<>();
    private Enemy selectedEnemy;
    private boolean inventoryOverlayOpen;
    private Scene boundScene;

    @FXML
    private StackPane gameViewRoot;
    @FXML
    private BorderPane gameContentPane;
    @FXML
    private Pane gameBoardPane;
    @FXML
    private Label levelSceneTitleLabel;
    @FXML
    private Label objectiveLabel;
    @FXML
    private Label playerNameHudLabel;
    @FXML
    private Label playerLevelHudLabel;
    @FXML
    private Label playerHpHudLabel;
    @FXML
    private Label playerExperienceHudLabel;
    @FXML
    private Label playerStatsHudLabel;
    @FXML
    private Label playerElementHudLabel;
    @FXML
    private Label remainingEnemiesHudLabel;
    @FXML
    private Label doorStatusLabel;
    @FXML
    private Label selectedEnemyLabel;
    @FXML
    private Label selectedEnemyStatsLabel;
    @FXML
    private StackPane inventoryOverlay;
    @FXML
    private VBox inventoryOverlayListBox;
    @FXML
    private Label inventoryOverlayFeedbackLabel;
    @FXML
    private FlowPane rewardActionPane;
    @FXML
    private FlowPane elementActionPane;
    @FXML
    private Button attackButton;
    @FXML
    private Button usePotionButton;
    @FXML
    private Button claimDropButton;
    @FXML
    private Button advanceDoorButton;

    public GameViewController(
            SceneNavigator sceneNavigator,
            GameService gameService,
            CurrentGameState currentGameState,
            String initialFeedbackMessage
    ) {
        this.sceneNavigator = Objects.requireNonNull(sceneNavigator, "sceneNavigator cannot be null");
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
        this.currentGameState = Objects.requireNonNull(currentGameState, "currentGameState cannot be null");
    }

    @FXML
    private void initialize() {
        gameBoardPane.setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        inventoryOverlay.setManaged(false);
        inventoryOverlay.setVisible(false);
        bindGlobalShortcuts();

        refreshView();
    }

    @FXML
    private void handleCloseInventoryOverlay() {
        closeInventoryOverlay();
    }

    @FXML
    private void handleAttackSelectedEnemy() {
        if (selectedEnemy == null) {
            return;
        }

        try {
            CombatTurnResult turnResult = gameService.attackCurrentLevelEnemy(selectedEnemy);
            currentGameState = turnResult.getCurrentGameState();
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    @FXML
    private void handleUsePotion() {
        Optional<Potion> potion = findFirstPotion();
        if (potion.isEmpty()) {
            return;
        }

        try {
            gameService.useItem(potion.get());
            currentGameState = gameService.getCurrentGameState();
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    @FXML
    private void handleClaimDrop() {
        DemoLevel currentLevel = currentGameState.getCurrentLevel();
        Item completionDrop = currentLevel.getCompletionDrop();
        if (completionDrop == null) {
            return;
        }

        try {
            currentGameState = gameService.claimCurrentLevelCompletionDrop();
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    @FXML
    private void handleAdvanceDoor() {
        if (!canOpenDoor()) {
            return;
        }

        try {
            currentGameState = gameService.completeCurrentLevel();
            String message = currentGameState.isDemoCompleted()
                    ? "Hai completato la demo e sconfitto il boss finale."
                    : "Hai attraversato la porta. Il prossimo livello e pronto.";
            sceneNavigator.showGameOverview(currentGameState, message);
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    @FXML
    private void handleBack() {
        sceneNavigator.showGameOverview(currentGameState, "Riepilogo della run aperto.");
    }

    private void refreshView() {
        currentGameState = gameService.getCurrentGameState();
        ensureSelectedEnemy();
        updateHud();
        renderBoard();
        renderRewardActions();
        renderElementActions();
        updateActionButtons();

        if (inventoryOverlayOpen) {
            renderInventoryOverlay();
        }
    }

    private void bindGlobalShortcuts() {
        gameViewRoot.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, globalKeyHandler);
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, globalKeyHandler);
                boundScene = newScene;
            }
        });

        Platform.runLater(() -> {
            Scene scene = gameViewRoot.getScene();
            if (scene != null && scene != boundScene) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, globalKeyHandler);
                boundScene = scene;
            }
            gameViewRoot.requestFocus();
        });
    }

    private void handleGlobalKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.M) {
            if (inventoryOverlayOpen) {
                closeInventoryOverlay();
            } else {
                openInventoryOverlay();
            }
            event.consume();
            return;
        }

        if (inventoryOverlayOpen) {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeInventoryOverlay();
            }
            event.consume();
        }
    }

    private void openInventoryOverlay() {
        inventoryOverlayOpen = true;
        inventoryOverlay.setManaged(true);
        inventoryOverlay.setVisible(true);
        gameContentPane.setMouseTransparent(true);
        renderInventoryOverlay();
        inventoryOverlay.requestFocus();
    }

    private void closeInventoryOverlay() {
        inventoryOverlayOpen = false;
        inventoryOverlay.setVisible(false);
        inventoryOverlay.setManaged(false);
        gameContentPane.setMouseTransparent(false);
        inventoryOverlayFeedbackLabel.setText("");
        gameViewRoot.requestFocus();
    }

    private void updateHud() {
        DemoLevel currentLevel = currentGameState.getCurrentLevel();
        int experienceToNextLevel = currentGameState.getPlayer().getLevel() * 100;

        levelSceneTitleLabel.setText(currentLevel.getName());
        objectiveLabel.setText(buildObjectiveText());
        playerNameHudLabel.setText(currentGameState.getPlayer().getName());
        playerLevelHudLabel.setText("LV " + currentGameState.getPlayer().getLevel());
        playerHpHudLabel.setText("HP " + currentGameState.getPlayer().getCurrentHp() + " / " + currentGameState.getPlayer().getMaxHp());
        playerExperienceHudLabel.setText("EXP " + currentGameState.getPlayer().getExperience() + " / " + experienceToNextLevel);
        playerStatsHudLabel.setText(
                "ATK " + currentGameState.getPlayer().getAttack()
                        + "  DEF " + currentGameState.getPlayer().getDefense()
                        + "  SPD " + currentGameState.getPlayer().getSpeed()
        );
        playerElementHudLabel.setText(
                currentGameState.getPlayer().getElementType() == null
                        ? "Elemento non scelto"
                        : "Elemento: " + currentGameState.getPlayer().getElementType().name()
        );
        remainingEnemiesHudLabel.setText("Nemici: " + currentLevel.getRemainingEnemies());
        doorStatusLabel.setText(canOpenDoor() ? "Porta aperta" : "Porta sigillata");

        if (selectedEnemy == null) {
            selectedEnemyLabel.setText("Nessun bersaglio");
            selectedEnemyStatsLabel.setText("Seleziona un nemico sulla mappa.");
        } else {
            selectedEnemyLabel.setText(selectedEnemy.getName());
            selectedEnemyStatsLabel.setText(
                    "HP " + selectedEnemy.getHp()
                            + "  ATK " + selectedEnemy.getAttack()
                            + "  DEF " + selectedEnemy.getDefense()
            );
        }

    }

    private void renderBoard() {
        gameBoardPane.getChildren().clear();
        enemyNodes.clear();

        renderBoardTiles();
        renderDoorNode();
        renderPlayerNode();
        renderInteractiveItemNode();
        renderEnemies();
    }

    private void renderBoardTiles() {
        int columns = (int) Math.ceil(BOARD_WIDTH / TILE_SIZE);
        int rows = (int) Math.ceil(BOARD_HEIGHT / TILE_SIZE);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Rectangle tile = new Rectangle(TILE_SIZE - 3, TILE_SIZE - 3);
                tile.setArcWidth(12);
                tile.setArcHeight(12);
                tile.setFill((row + column) % 2 == 0 ? TILE_COLOR : TILE_ALT_COLOR);
                tile.setLayoutX(column * TILE_SIZE);
                tile.setLayoutY(row * TILE_SIZE);
                gameBoardPane.getChildren().add(tile);
            }
        }

        Rectangle border = new Rectangle(BOARD_WIDTH, BOARD_HEIGHT);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(WALL_COLOR);
        border.setStrokeWidth(10);
        border.setArcWidth(32);
        border.setArcHeight(32);
        gameBoardPane.getChildren().add(border);
    }

    private void renderDoorNode() {
        StackPane doorNode = new StackPane();
        doorNode.getStyleClass().add("board-door");
        doorNode.getStyleClass().add(canOpenDoor() ? "board-door-open" : "board-door-locked");
        doorNode.setPrefSize(104, 136);
        doorNode.setLayoutX(DOOR_POSITION.getX());
        doorNode.setLayoutY(DOOR_POSITION.getY());
        doorNode.setOnMouseClicked(this::handleDoorClicked);

        Rectangle portal = new Rectangle(92, 124);
        portal.setArcWidth(28);
        portal.setArcHeight(28);
        portal.setFill(canOpenDoor() ? DOOR_OPEN_COLOR : DOOR_LOCKED_COLOR);
        portal.setOpacity(canOpenDoor() ? 0.95 : 0.78);

        Label label = new Label(canOpenDoor() ? "Porta" : "Sigillo");
        label.getStyleClass().add("board-door-label");

        doorNode.getChildren().addAll(portal, label);
        gameBoardPane.getChildren().add(doorNode);
    }

    private void renderPlayerNode() {
        StackPane playerNode = new StackPane();
        playerNode.getStyleClass().add("board-player");
        playerNode.setPrefSize(116, 116);
        playerNode.setLayoutX(PLAYER_POSITION.getX());
        playerNode.setLayoutY(PLAYER_POSITION.getY() - 52);

        Circle outer = new Circle(42, PLAYER_COLOR);
        outer.setOpacity(0.28);
        Circle inner = new Circle(31, PLAYER_INNER_COLOR);
        inner.setStroke(PLAYER_COLOR);
        inner.setStrokeWidth(3);
        String playerName = currentGameState.getPlayer().getName();
        String playerInitial = (playerName == null || playerName.isBlank()) ? "?" : playerName.substring(0, 1).toUpperCase();
        Label label = new Label(playerInitial);
        label.getStyleClass().add("board-player-label");

        playerNode.getChildren().addAll(outer, inner, label);
        gameBoardPane.getChildren().add(playerNode);
    }

    private void renderInteractiveItemNode() {
        DemoLevel currentLevel = currentGameState.getCurrentLevel();
        if (!currentLevel.isCompleted()) {
            return;
        }

        String symbol;
        Color color;

        if (hasPendingCompletionDrop()) {
            symbol = "+";
            color = DROP_COLOR;
        } else if (hasPendingRewardChoice()) {
            symbol = "R";
            color = REWARD_COLOR;
        } else {
            return;
        }

        StackPane itemNode = new StackPane();
        itemNode.getStyleClass().add("board-item-node");
        itemNode.setPrefSize(102, 102);
        itemNode.setLayoutX(ITEM_POSITION.getX());
        itemNode.setLayoutY(ITEM_POSITION.getY());

        Circle halo = new Circle(38, color);
        halo.setOpacity(0.24);
        Circle core = new Circle(26, Color.web("#1b1623"));
        core.setStroke(color);
        core.setStrokeWidth(3);
        Label label = new Label(symbol);
        label.getStyleClass().add("board-item-label");

        itemNode.getChildren().addAll(halo, core, label);
        gameBoardPane.getChildren().add(itemNode);
    }

    private void renderEnemies() {
        int livingEnemies = (int) currentGameState.getCurrentLevel().getEnemies().stream()
                .filter(Enemy::isAlive)
                .count();

        if (livingEnemies == 0) {
            return;
        }

        int livingIndex = 0;
        for (Enemy enemy : currentGameState.getCurrentLevel().getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }

            Point2D position = computeEnemyPosition(livingIndex, livingEnemies);
            livingIndex++;

            if (enemy.getDetectionRange() > 0) {
                Circle range = new Circle(position.getX(), position.getY() + 42, 38 + enemy.getDetectionRange() * 10d);
                range.getStyleClass().add("enemy-range-indicator");
                gameBoardPane.getChildren().add(range);
            }

            ImageView imageView = new ImageView(loadEnemySprite(enemy));
            double width = enemy instanceof BossEnemy ? 250 : enemy instanceof Slime ? 150 : 170;
            double height = enemy instanceof BossEnemy ? 250 : enemy instanceof Slime ? 150 : 170;
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);

            Label hpChip = new Label("HP " + enemy.getHp());
            hpChip.getStyleClass().add("entity-chip");
            StackPane.setAlignment(hpChip, javafx.geometry.Pos.TOP_CENTER);

            StackPane enemyNode = new StackPane(imageView, hpChip);
            enemyNode.getStyleClass().add("enemy-node");
            if (enemy.equals(selectedEnemy)) {
                enemyNode.getStyleClass().add("enemy-node-selected");
            }
            enemyNode.setLayoutX(position.getX() - width / 2);
            enemyNode.setLayoutY(position.getY() - height / 2);
            enemyNode.setPrefSize(width, height);
            enemyNode.setOnMouseClicked(event -> handleEnemySelected(enemy));

            enemyNodes.put(enemy, enemyNode);
            gameBoardPane.getChildren().add(enemyNode);
        }
    }

    private void renderInventoryOverlay() {
        inventoryOverlayListBox.getChildren().clear();

        if (currentGameState.getPlayer().getInventory().getItems().isEmpty()) {
            Label emptyLabel = new Label("Inventario vuoto.");
            emptyLabel.getStyleClass().add("body-label");
            inventoryOverlayListBox.getChildren().add(emptyLabel);
            return;
        }

        currentGameState.getPlayer().getInventory().getItems().forEach((item, quantity) -> {
            inventoryOverlayListBox.getChildren().add(buildInventoryEntry(item, quantity));
        });
    }

    private HBox buildInventoryEntry(Item item, int quantity) {
        Label nameLabel = new Label(item.getName() + " x" + quantity);
        nameLabel.getStyleClass().add("hud-main-value");

        Label descriptionLabel = new Label(item.getDescription());
        descriptionLabel.getStyleClass().add("body-label");
        descriptionLabel.setWrapText(true);

        Label typeLabel = new Label(resolveInventoryTypeLabel(item));
        typeLabel.getStyleClass().add("muted-label");

        VBox textBox = new VBox(4, nameLabel, descriptionLabel, typeLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button useButton = new Button(resolveInventoryActionLabel(item));
        useButton.getStyleClass().addAll("menu-button", "primary-button");
        useButton.setDisable(!canUseFromInventoryOverlay(item));
        useButton.setOnAction(event -> handleUseInventoryOverlayItem(item));

        HBox entry = new HBox(18, textBox, useButton);
        entry.getStyleClass().add("inventory-entry-card");
        return entry;
    }

    private void renderRewardActions() {
        rewardActionPane.getChildren().clear();
        if (!hasPendingRewardChoice()) {
            return;
        }

        currentGameState.getCurrentLevel().getRewardChoices().forEach((choice, reward) -> {
            Button button = new Button(choice.getLabel());
            button.getStyleClass().addAll("menu-button", "secondary-button", "contextual-action-button");
            button.setOnAction(event -> handleChooseReward(choice, reward.getName()));
            rewardActionPane.getChildren().add(button);
        });
    }

    private void renderElementActions() {
        elementActionPane.getChildren().clear();
        if (!needsElementChoice()) {
            return;
        }

        for (ElementType elementType : ElementType.values()) {
            Button button = new Button(elementType.name());
            button.getStyleClass().addAll("menu-button", "secondary-button", "contextual-action-button");
            button.setOnAction(event -> handleChooseElement(elementType));
            elementActionPane.getChildren().add(button);
        }
    }

    private void updateActionButtons() {
        boolean levelCompleted = currentGameState.getCurrentLevel().isCompleted();
        boolean playerAlive = currentGameState.getPlayer().isAlive();

        attackButton.setDisable(selectedEnemy == null || levelCompleted || !playerAlive);
        usePotionButton.setDisable(findFirstPotion().isEmpty() || !playerAlive);
        claimDropButton.setDisable(!hasPendingCompletionDrop() || !playerAlive);
        advanceDoorButton.setDisable(!canOpenDoor());
    }

    private void ensureSelectedEnemy() {
        if (selectedEnemy != null && selectedEnemy.isAlive() && currentGameState.getCurrentLevel().getEnemies().contains(selectedEnemy)) {
            return;
        }

        selectedEnemy = currentGameState.getCurrentLevel().getEnemies().stream()
                .filter(Enemy::isAlive)
                .findFirst()
                .orElse(null);
    }

    private void handleEnemySelected(Enemy enemy) {
        selectedEnemy = enemy;
        refreshView();
    }

    private void handleDoorClicked(MouseEvent event) {
        event.consume();
        handleAdvanceDoor();
    }

    private void handleChooseElement(ElementType elementType) {
        try {
            currentGameState = gameService.attuneCurrentPlayerToOriginStone(elementType);
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    private void handleChooseReward(LevelRewardChoice rewardChoice, String rewardName) {
        try {
            currentGameState = gameService.chooseCurrentLevelReward(rewardChoice);
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    private void handleUseInventoryOverlayItem(Item item) {
        try {
            if (item instanceof Potion) {
                CombatResult result = gameService.useItem(item);
                currentGameState = gameService.getCurrentGameState();
                inventoryOverlayFeedbackLabel.setText(result.getMessage());
            } else if (item instanceof Weapon || item instanceof Armor) {
                CombatResult result = gameService.useItem(item);
                currentGameState = gameService.getCurrentGameState();
                currentGameState.getPlayer().removeItem(item);
                String message = result.getMessage() + ". Equipaggiato.";
                inventoryOverlayFeedbackLabel.setText(message);
            } else {
                inventoryOverlayFeedbackLabel.setText(item.getName() + " non e usabile da questo menu.");
            }

            refreshView();
        } catch (RuntimeException exception) {
            String message = "Uso inventario fallito: " + exception.getMessage();
            inventoryOverlayFeedbackLabel.setText(message);
        }
    }

    private boolean canUseFromInventoryOverlay(Item item) {
        return item.getType() != ItemType.KEY_ITEM;
    }

    private String resolveInventoryActionLabel(Item item) {
        return switch (item.getType()) {
            case POTION -> "Usa";
            case WEAPON, ARMOR -> "Equipaggia";
            case KEY_ITEM -> "Chiave";
        };
    }

    private String resolveInventoryTypeLabel(Item item) {
        return switch (item.getType()) {
            case POTION -> "Consumabile";
            case WEAPON -> "Equipaggiamento offensivo";
            case ARMOR -> "Equipaggiamento difensivo";
            case KEY_ITEM -> "Oggetto chiave";
        };
    }

    private boolean canOpenDoor() {
        return currentGameState.getPlayer().isAlive()
                && currentGameState.getCurrentLevel().isCompleted()
                && !hasPendingCompletionDrop()
                && !needsElementChoice()
                && !hasPendingRewardChoice();
    }

    private boolean hasPendingCompletionDrop() {
        DemoLevel currentLevel = currentGameState.getCurrentLevel();
        return currentLevel.hasCompletionDrop() && !currentLevel.isCompletionDropClaimed();
    }

    private boolean hasPendingRewardChoice() {
        DemoLevel currentLevel = currentGameState.getCurrentLevel();
        return currentLevel.hasRewardChoices() && !currentLevel.isRewardClaimed();
    }

    private boolean needsElementChoice() {
        return currentGameState.getCurrentLevel().isUnlocksElementChoice()
                && currentGameState.getPlayer().getElementType() == null
                && currentGameState.getPlayer().getInventory().getItems().keySet().stream().anyMatch(OriginStone.class::isInstance);
    }

    private Optional<Potion> findFirstPotion() {
        return currentGameState.getPlayer().getInventory().getItems().keySet().stream()
                .filter(Potion.class::isInstance)
                .map(Potion.class::cast)
                .findFirst();
    }

    private String buildObjectiveText() {
        if (!currentGameState.getPlayer().isAlive()) {
            return "Il tuo eroe e stato sconfitto. Torna al riepilogo o carica un salvataggio.";
        }

        if (currentGameState.getCurrentLevel().getRemainingEnemies() > 0) {
            return "Elimina i nemici rimasti e mantieni il controllo dell'arena.";
        }

        if (hasPendingCompletionDrop()) {
            return "Raccogli l'oggetto del livello prima di avanzare.";
        }

        if (needsElementChoice()) {
            return "Scegli un elemento per attivare la Pietra dell'Origine.";
        }

        if (hasPendingRewardChoice()) {
            return "Scegli una ricompensa prima di aprire la porta.";
        }

        return currentGameState.isDemoCompleted()
                ? "La porta finale e aperta: la demo e completa."
                : "La porta e aperta. Attraversala per raggiungere il prossimo livello.";
    }

    private Point2D computeEnemyPosition(int index, int totalLivingEnemies) {
        if (totalLivingEnemies == 1) {
            return new Point2D(BOARD_WIDTH - 470, BOARD_HEIGHT / 2);
        }

        double spacing = totalLivingEnemies == 2 ? 200 : 160;
        double startY = (BOARD_HEIGHT / 2) - ((totalLivingEnemies - 1) * spacing / 2);
        return new Point2D(BOARD_WIDTH - 470, startY + index * spacing);
    }

    private Image loadEnemySprite(Enemy enemy) {
        String spritePath;
        if (enemy instanceof Goblin) {
            spritePath = "/images/enemies/goblin.png";
        } else if (enemy instanceof Skeleton) {
            spritePath = "/images/enemies/skeleton.png";
        } else if (enemy instanceof BossEnemy) {
            spritePath = "/images/enemies/boss.png";
        } else if (enemy instanceof Slime) {
            spritePath = "/images/enemies/slime.png";
        } else {
            throw new IllegalArgumentException("No sprite configured for enemy " + enemy.getClass().getSimpleName());
        }

        InputStream stream = GameViewController.class.getResourceAsStream(spritePath);
        if (stream == null) {
            throw new IllegalStateException("Sprite resource not found: " + spritePath);
        }

        return new Image(stream);
    }
}
