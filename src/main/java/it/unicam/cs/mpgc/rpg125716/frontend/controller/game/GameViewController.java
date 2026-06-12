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
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.Set;

public class GameViewController {
    private static final double BOARD_WIDTH = 1600;
    private static final double BOARD_HEIGHT = 540;
    private static final double TILE_SIZE = 102;
    private static final double PLAYER_NODE_SIZE = 116;
    private static final double ITEM_NODE_SIZE = 102;
    private static final double DOOR_NODE_WIDTH = 104;
    private static final double DOOR_NODE_HEIGHT = 136;
    private static final Point2D PLAYER_START_POSITION = new Point2D(258, (BOARD_HEIGHT / 2) + 6);
    private static final Point2D ITEM_POSITION = new Point2D((BOARD_WIDTH / 2) - 46, BOARD_HEIGHT - 165);
    private static final Point2D DOOR_POSITION = new Point2D(BOARD_WIDTH - 170, (TILE_SIZE * 2) - 17);
    private static final double PLAYER_COLLISION_RADIUS = 44;
    private static final double PLAYER_MOVE_SPEED = 180;
    private static final double PLAYER_SPEED_FACTOR = 8;
    private static final double ENEMY_MOVE_SPEED = 96;
    private static final double ENEMY_MOVE_SPEED_FACTOR = 3;
    private static final double ENTITY_COLLISION_PADDING = 10;
    private static final double INTERACTION_RANGE = 110;
    private static final double MELEE_ATTACK_RANGE = 170;
    private static final double ATTACK_COOLDOWN_SECONDS = 0.45;
    private static final double MAX_FRAME_DELTA_SECONDS = 0.05;
    private static final Color TILE_COLOR = Color.web("#1e1826");
    private static final Color TILE_ALT_COLOR = Color.web("#231c2d");
    private static final Color WALL_COLOR = Color.web("#0f0c14");
    private static final Color FOREST_TILE_COLOR = Color.web("#355c3a");
    private static final Color FOREST_TILE_ALT_COLOR = Color.web("#427347");
    private static final Color FOREST_TREE_TILE_COLOR = Color.web("#5a4330");
    private static final Color FOREST_TREE_TILE_ALT_COLOR = Color.web("#6b523c");
    private static final Color FOREST_WALL_COLOR = Color.web("#1f3624");
    private static final Color PLAYER_COLOR = Color.web("#d4af37");
    private static final Color PLAYER_INNER_COLOR = Color.web("#6b3fa0");
    private static final Color DROP_COLOR = Color.web("#d4af37");
    private static final Color REWARD_COLOR = Color.web("#6b3fa0");
    private static final Color DOOR_LOCKED_COLOR = Color.web("#41304d");
    private static final Color DOOR_OPEN_COLOR = Color.web("#d4af37");

    private final SceneNavigator sceneNavigator;
    private final GameService gameService;
    private CurrentGameState currentGameState;
    private final EventHandler<KeyEvent> globalKeyPressedHandler = this::handleGlobalKeyPressed;
    private final EventHandler<KeyEvent> globalKeyReleasedHandler = this::handleGlobalKeyReleased;
    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private final Map<Enemy, Point2D> enemyPositions = new LinkedHashMap<>();
    private final Map<Enemy, EnemyVisual> enemyVisuals = new LinkedHashMap<>();
    private final Map<Class<? extends Enemy>, Image> enemySpriteCache = new HashMap<>();

    private Enemy selectedEnemy;
    private boolean inventoryOverlayOpen;
    private boolean attackRequested;
    private boolean interactionRequested;
    private long lastFrameNanos;
    private double attackCooldownRemaining;
    private Point2D playerPosition = PLAYER_START_POSITION;
    private Scene boundScene;
    private AnimationTimer gameLoop;
    private StackPane playerNode;
    private StackPane doorNode;
    private Rectangle doorPortal;
    private Label doorNodeLabel;
    private StackPane itemNode;
    private Circle itemHalo;
    private Circle itemCore;
    private Label itemNodeLabel;

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
    private Label contextualHintLabel;
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

        initializeRuntimeState();
        initializeBoardScene();
        refreshView();
        bindGlobalShortcuts();
        startGameLoop();
    }

    @FXML
    private void handleCloseInventoryOverlay() {
        closeInventoryOverlay();
    }

    @FXML
    private void handleAttackSelectedEnemy() {
        attackRequested = true;
    }

    @FXML
    private void handleUsePotion() {
        Optional<Potion> potion = findFirstPotion();
        if (potion.isEmpty()) {
            return;
        }

        try {
            gameService.useItem(potion.get());
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    @FXML
    private void handleClaimDrop() {
        DemoLevel currentLevel = currentGameState.getCurrentLevel();
        if (currentLevel.getCompletionDrop() == null || !isPlayerNearInteractiveItem()) {
            return;
        }

        try {
            gameService.claimCurrentLevelCompletionDrop();
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    @FXML
    private void handleAdvanceDoor() {
        if (!canOpenDoor() || !isPlayerNearDoor()) {
            return;
        }

        try {
            CurrentGameState nextState = gameService.completeCurrentLevel();
            String message = nextState.isDemoCompleted()
                    ? "Hai completato la demo e sconfitto il boss finale."
                    : "Hai attraversato la porta. Il prossimo livello e pronto.";
            stopGameLoop();
            sceneNavigator.showGameOverview(nextState, message);
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    @FXML
    private void handleBack() {
        stopGameLoop();
        sceneNavigator.showGameOverview(currentGameState, "Riepilogo della run aperto.");
    }

    private void initializeRuntimeState() {
        playerPosition = clampToBoard(PLAYER_START_POSITION, PLAYER_COLLISION_RADIUS);
        seedEnemyPositions();
        ensureSelectedEnemy();
    }

    private void initializeBoardScene() {
        gameBoardPane.getChildren().clear();
        renderBoardTiles();
        doorNode = buildDoorNode();
        itemNode = buildItemNode();
        playerNode = buildPlayerNode();
        gameBoardPane.getChildren().addAll(doorNode, itemNode, playerNode);
        rebuildEnemyVisuals();
        renderDynamicScene();
    }

    private void refreshView() {
        currentGameState = gameService.getCurrentGameState();
        synchronizeEnemyPositionsWithCurrentLevel();
        ensureSelectedEnemy();
        updateHud();
        renderDynamicScene();

        if (inventoryOverlayOpen) {
            renderInventoryOverlay();
        }
    }

    private void bindGlobalShortcuts() {
        gameViewRoot.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, globalKeyPressedHandler);
                oldScene.removeEventFilter(KeyEvent.KEY_RELEASED, globalKeyReleasedHandler);
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, globalKeyPressedHandler);
                newScene.addEventFilter(KeyEvent.KEY_RELEASED, globalKeyReleasedHandler);
                boundScene = newScene;
            }
        });

        Platform.runLater(() -> {
            Scene scene = gameViewRoot.getScene();
            if (scene != null && scene != boundScene) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, globalKeyPressedHandler);
                scene.addEventFilter(KeyEvent.KEY_RELEASED, globalKeyReleasedHandler);
                boundScene = scene;
            }
            gameViewRoot.requestFocus();
        });
    }

    private void handleGlobalKeyPressed(KeyEvent event) {
        KeyCode keyCode = event.getCode();

        if (keyCode == KeyCode.M) {
            if (inventoryOverlayOpen) {
                closeInventoryOverlay();
            } else {
                openInventoryOverlay();
            }
            event.consume();
            return;
        }

        if (inventoryOverlayOpen) {
            if (keyCode == KeyCode.ESCAPE) {
                closeInventoryOverlay();
            }
            event.consume();
            return;
        }

        if (handleContextualSelectionKey(keyCode)) {
            event.consume();
            return;
        }

        if (keyCode == KeyCode.SPACE || keyCode == KeyCode.ENTER) {
            attackRequested = true;
            event.consume();
            return;
        }

        if (keyCode == KeyCode.E) {
            interactionRequested = true;
            event.consume();
            return;
        }

        if (isMovementKey(keyCode)) {
            pressedKeys.add(keyCode);
            event.consume();
        }
    }

    private void handleGlobalKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());

        if (inventoryOverlayOpen && (isMovementKey(event.getCode()) || event.getCode() == KeyCode.E)) {
            event.consume();
        }
    }

    private void openInventoryOverlay() {
        inventoryOverlayOpen = true;
        inventoryOverlay.setManaged(true);
        inventoryOverlay.setVisible(true);
        gameContentPane.setMouseTransparent(true);
        pressedKeys.clear();
        attackRequested = false;
        interactionRequested = false;
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
        lastFrameNanos = 0L;
    }

    private void startGameLoop() {
        stopGameLoop();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = now;
                    updateHud();
                    renderDynamicScene();
                    return;
                }

                double deltaSeconds = Math.min((now - lastFrameNanos) / 1_000_000_000d, MAX_FRAME_DELTA_SECONDS);
                lastFrameNanos = now;
                updateFrame(deltaSeconds);
            }
        };
        gameLoop.start();
    }

    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
        pressedKeys.clear();
        attackRequested = false;
        interactionRequested = false;
        lastFrameNanos = 0L;
    }

    private void updateFrame(double deltaSeconds) {
        if (!inventoryOverlayOpen && currentGameState.getPlayer().isAlive()) {
            updatePlayerMovement(deltaSeconds);
            updateEnemyMovement(deltaSeconds);
            resolveEntityCollisions();
            processInteractionRequest();
            processAttackRequest();
        } else {
            attackRequested = false;
            interactionRequested = false;
        }

        attackCooldownRemaining = Math.max(0, attackCooldownRemaining - deltaSeconds);
        updateHud();
        renderDynamicScene();

        if (inventoryOverlayOpen) {
            renderInventoryOverlay();
        }
    }

    private void updatePlayerMovement(double deltaSeconds) {
        Point2D movementDirection = resolveMovementDirection();
        if (movementDirection.magnitude() == 0) {
            return;
        }

        double movementSpeed = PLAYER_MOVE_SPEED + currentGameState.getPlayer().getSpeed() * PLAYER_SPEED_FACTOR;
        Point2D nextPosition = playerPosition.add(
                movementDirection.normalize().multiply(movementSpeed * deltaSeconds)
        );
        playerPosition = clampToBoard(nextPosition, PLAYER_COLLISION_RADIUS);
    }

    private void updateEnemyMovement(double deltaSeconds) {
        for (Enemy enemy : currentGameState.getCurrentLevel().getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }

            Point2D enemyPosition = enemyPositions.get(enemy);
            if (enemyPosition == null) {
                continue;
            }

            int tileDistanceFromPlayer = (int) Math.round(enemyPosition.distance(playerPosition) / TILE_SIZE);
            if (!enemy.shouldChasePlayer(tileDistanceFromPlayer)) {
                continue;
            }

            Point2D direction = playerPosition.subtract(enemyPosition);
            double minimumDistance = PLAYER_COLLISION_RADIUS + enemyCollisionRadius(enemy) + ENTITY_COLLISION_PADDING;
            if (direction.magnitude() <= minimumDistance) {
                continue;
            }

            double enemySpeed = ENEMY_MOVE_SPEED
                    + enemy.getDetectionRange() * ENEMY_MOVE_SPEED_FACTOR
                    + enemy.getAttack();

            Point2D nextPosition = enemyPosition.add(direction.normalize().multiply(enemySpeed * deltaSeconds));
            enemyPositions.put(enemy, clampToBoard(nextPosition, enemyCollisionRadius(enemy)));
        }
    }

    private void resolveEntityCollisions() {
        resolveEnemyEnemyCollisions();
        resolvePlayerEnemyCollisions();
    }

    private void resolveEnemyEnemyCollisions() {
        List<Enemy> aliveEnemies = currentGameState.getCurrentLevel().getEnemies().stream()
                .filter(Enemy::isAlive)
                .toList();

        for (int firstIndex = 0; firstIndex < aliveEnemies.size(); firstIndex++) {
            Enemy firstEnemy = aliveEnemies.get(firstIndex);
            Point2D firstPosition = enemyPositions.get(firstEnemy);
            if (firstPosition == null) {
                continue;
            }

            for (int secondIndex = firstIndex + 1; secondIndex < aliveEnemies.size(); secondIndex++) {
                Enemy secondEnemy = aliveEnemies.get(secondIndex);
                Point2D secondPosition = enemyPositions.get(secondEnemy);
                if (secondPosition == null) {
                    continue;
                }

                double minimumDistance = enemyCollisionRadius(firstEnemy)
                        + enemyCollisionRadius(secondEnemy)
                        + ENTITY_COLLISION_PADDING;
                Point2D delta = secondPosition.subtract(firstPosition);
                double distance = delta.magnitude();
                if (distance >= minimumDistance) {
                    continue;
                }

                Point2D normal = distance == 0 ? new Point2D(1, 0) : delta.normalize();
                double correction = (minimumDistance - distance) / 2d;
                enemyPositions.put(
                        firstEnemy,
                        clampToBoard(firstPosition.subtract(normal.multiply(correction)), enemyCollisionRadius(firstEnemy))
                );
                enemyPositions.put(
                        secondEnemy,
                        clampToBoard(secondPosition.add(normal.multiply(correction)), enemyCollisionRadius(secondEnemy))
                );
                firstPosition = enemyPositions.get(firstEnemy);
            }
        }
    }

    private void resolvePlayerEnemyCollisions() {
        for (Enemy enemy : currentGameState.getCurrentLevel().getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }

            Point2D enemyPosition = enemyPositions.get(enemy);
            if (enemyPosition == null) {
                continue;
            }

            Point2D delta = playerPosition.subtract(enemyPosition);
            double minimumDistance = PLAYER_COLLISION_RADIUS + enemyCollisionRadius(enemy) + ENTITY_COLLISION_PADDING;
            double distance = delta.magnitude();
            if (distance >= minimumDistance) {
                continue;
            }

            Point2D normal = distance == 0 ? new Point2D(-1, 0) : delta.normalize();
            playerPosition = clampToBoard(
                    enemyPosition.add(normal.multiply(minimumDistance)),
                    PLAYER_COLLISION_RADIUS
            );
        }
    }

    private void processInteractionRequest() {
        if (!interactionRequested) {
            return;
        }

        interactionRequested = false;

        if (hasPendingCompletionDrop() && isPlayerNearInteractiveItem()) {
            handleClaimDrop();
            return;
        }

        if (canOpenDoor() && isPlayerNearDoor()) {
            handleAdvanceDoor();
        }
    }

    private void processAttackRequest() {
        if (!attackRequested) {
            return;
        }

        attackRequested = false;
        if (attackCooldownRemaining > 0 || selectedEnemy == null || !selectedEnemy.isAlive()) {
            return;
        }

        if (!isEnemyInAttackRange(selectedEnemy)) {
            return;
        }

        try {
            CombatTurnResult turnResult = gameService.attackCurrentLevelEnemy(selectedEnemy);
            currentGameState = turnResult.getCurrentGameState();
            attackCooldownRemaining = ATTACK_COOLDOWN_SECONDS;
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
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
        contextualHintLabel.setText(buildContextualHintText());
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
                            + (isEnemyInAttackRange(selectedEnemy) ? "  IN PORTATA" : "  FUORI PORTATA")
            );
        }
    }

    private void renderDynamicScene() {
        renderDoorNodeState();
        renderItemNodeState();
        renderPlayerNodeState();
        renderEnemyNodeStates();
    }

    private void renderBoardTiles() {
        BoardPalette boardPalette = resolveBoardPalette();
        int columns = Math.max(1, (int) Math.floor(BOARD_WIDTH / TILE_SIZE));
        int rows = Math.max(1, (int) Math.floor(BOARD_HEIGHT / TILE_SIZE));

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Rectangle tile = new Rectangle(TILE_SIZE - 3, TILE_SIZE - 3);
                tile.setArcWidth(12);
                tile.setArcHeight(12);
                tile.setFill(resolveBoardTileColor(boardPalette, row, column, rows, columns));
                tile.setLayoutX(column * TILE_SIZE);
                tile.setLayoutY(row * TILE_SIZE);
                gameBoardPane.getChildren().add(tile);
            }
        }

        Rectangle border = new Rectangle(BOARD_WIDTH, BOARD_HEIGHT);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(boardPalette.borderColor());
        border.setStrokeWidth(10);
        border.setArcWidth(32);
        border.setArcHeight(32);
        gameBoardPane.getChildren().add(border);
    }

    private BoardPalette resolveBoardPalette() {
        if (currentGameState.getCurrentLevel().getNumber() == 1) {
            return new BoardPalette(FOREST_TILE_COLOR, FOREST_TILE_ALT_COLOR, FOREST_WALL_COLOR);
        }

        return new BoardPalette(TILE_COLOR, TILE_ALT_COLOR, WALL_COLOR);
    }

    private Color resolveBoardTileColor(BoardPalette boardPalette, int row, int column, int rows, int columns) {
        if (currentGameState.getCurrentLevel().getNumber() == 1 && isForestTreeTile(row, column, rows, columns)) {
            return (row + column) % 2 == 0 ? FOREST_TREE_TILE_COLOR : FOREST_TREE_TILE_ALT_COLOR;
        }

        return (row + column) % 2 == 0
                ? boardPalette.primaryTileColor()
                : boardPalette.secondaryTileColor();
    }

    private boolean isForestTreeTile(int row, int column, int rows, int columns) {
        boolean leftGrove = column <= 1 && (row <= 1 || row >= rows - 2);
        boolean rightGrove = column >= columns - 2 && (row == 1 || row == rows - 2);
        boolean bottomPatch = row == rows - 1 && column >= 5 && column <= 7;
        return leftGrove || rightGrove || bottomPatch;
    }

    private StackPane buildDoorNode() {
        StackPane builtDoorNode = new StackPane();
        builtDoorNode.getStyleClass().add("board-door");
        builtDoorNode.setPrefSize(DOOR_NODE_WIDTH, DOOR_NODE_HEIGHT);
        builtDoorNode.setLayoutX(DOOR_POSITION.getX());
        builtDoorNode.setLayoutY(DOOR_POSITION.getY());
        builtDoorNode.setOnMouseClicked(this::handleDoorClicked);

        doorPortal = new Rectangle(92, 124);
        doorPortal.setArcWidth(28);
        doorPortal.setArcHeight(28);

        doorNodeLabel = new Label("Porta");
        doorNodeLabel.getStyleClass().add("board-door-label");

        builtDoorNode.getChildren().addAll(doorPortal, doorNodeLabel);
        return builtDoorNode;
    }

    private StackPane buildPlayerNode() {
        StackPane builtPlayerNode = new StackPane();
        builtPlayerNode.getStyleClass().add("board-player");
        builtPlayerNode.setPrefSize(PLAYER_NODE_SIZE, PLAYER_NODE_SIZE);

        Circle outer = new Circle(42, PLAYER_COLOR);
        outer.setOpacity(0.28);
        Circle inner = new Circle(31, PLAYER_INNER_COLOR);
        inner.setStroke(PLAYER_COLOR);
        inner.setStrokeWidth(3);

        String playerName = currentGameState.getPlayer().getName();
        String playerInitial = (playerName == null || playerName.isBlank()) ? "?" : playerName.substring(0, 1).toUpperCase();
        Label label = new Label(playerInitial);
        label.getStyleClass().add("board-player-label");

        builtPlayerNode.getChildren().addAll(outer, inner, label);
        return builtPlayerNode;
    }

    private StackPane buildItemNode() {
        StackPane builtItemNode = new StackPane();
        builtItemNode.getStyleClass().add("board-item-node");
        builtItemNode.setPrefSize(ITEM_NODE_SIZE, ITEM_NODE_SIZE);
        builtItemNode.setLayoutX(ITEM_POSITION.getX());
        builtItemNode.setLayoutY(ITEM_POSITION.getY());
        builtItemNode.setVisible(false);
        builtItemNode.setManaged(false);

        itemHalo = new Circle(38, DROP_COLOR);
        itemHalo.setOpacity(0.24);
        itemCore = new Circle(26, Color.web("#1b1623"));
        itemCore.setStroke(DROP_COLOR);
        itemCore.setStrokeWidth(3);
        itemNodeLabel = new Label("+");
        itemNodeLabel.getStyleClass().add("board-item-label");

        builtItemNode.getChildren().addAll(itemHalo, itemCore, itemNodeLabel);
        return builtItemNode;
    }

    private void rebuildEnemyVisuals() {
        enemyVisuals.values().forEach(enemyVisual -> {
            if (enemyVisual.rangeIndicator() != null) {
                gameBoardPane.getChildren().remove(enemyVisual.rangeIndicator());
            }
            gameBoardPane.getChildren().remove(enemyVisual.node());
        });
        enemyVisuals.clear();

        for (Enemy enemy : currentGameState.getCurrentLevel().getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }

            Circle rangeIndicator = null;
            if (enemy.getDetectionRange() > 0) {
                rangeIndicator = new Circle();
                rangeIndicator.getStyleClass().add("enemy-range-indicator");
                gameBoardPane.getChildren().add(rangeIndicator);
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
            enemyNode.setPrefSize(width, height);
            enemyNode.setOnMouseClicked(event -> handleEnemySelected(enemy));

            gameBoardPane.getChildren().add(enemyNode);
            enemyVisuals.put(enemy, new EnemyVisual(enemyNode, hpChip, rangeIndicator, width, height));
        }
    }

    private void renderDoorNodeState() {
        boolean doorOpen = canOpenDoor();

        doorNode.getStyleClass().removeAll("board-door-open", "board-door-locked");
        doorNode.getStyleClass().add(doorOpen ? "board-door-open" : "board-door-locked");
        doorPortal.setFill(doorOpen ? DOOR_OPEN_COLOR : DOOR_LOCKED_COLOR);
        doorPortal.setOpacity(doorOpen ? 0.95 : 0.78);
        doorNodeLabel.setText(doorOpen ? "Porta" : "Sigillo");
    }

    private void renderItemNodeState() {
        if (!currentGameState.getCurrentLevel().isCompleted()) {
            itemNode.setVisible(false);
            itemNode.setManaged(false);
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
            itemNode.setVisible(false);
            itemNode.setManaged(false);
            return;
        }

        itemNode.setVisible(true);
        itemNode.setManaged(true);
        itemHalo.setFill(color);
        itemCore.setStroke(color);
        itemNodeLabel.setText(symbol);
    }

    private void renderPlayerNodeState() {
        playerNode.setLayoutX(playerPosition.getX() - (PLAYER_NODE_SIZE / 2));
        playerNode.setLayoutY(playerPosition.getY() - (PLAYER_NODE_SIZE / 2));
        playerNode.toFront();
    }

    private void renderEnemyNodeStates() {
        if (enemyVisuals.size() != currentGameState.getCurrentLevel().getEnemies().stream().filter(Enemy::isAlive).count()) {
            rebuildEnemyVisuals();
        }

        for (Enemy enemy : currentGameState.getCurrentLevel().getEnemies()) {
            EnemyVisual enemyVisual = enemyVisuals.get(enemy);
            Point2D enemyPosition = enemyPositions.get(enemy);
            if (enemyVisual == null || enemyPosition == null || !enemy.isAlive()) {
                continue;
            }

            if (enemyVisual.rangeIndicator() != null) {
                enemyVisual.rangeIndicator().setCenterX(enemyPosition.getX());
                enemyVisual.rangeIndicator().setCenterY(enemyPosition.getY() + 42);
                enemyVisual.rangeIndicator().setRadius(38 + enemy.getDetectionRange() * 10d);
            }

            enemyVisual.hpChip().setText("HP " + enemy.getHp());
            enemyVisual.node().setLayoutX(enemyPosition.getX() - enemyVisual.width() / 2);
            enemyVisual.node().setLayoutY(enemyPosition.getY() - enemyVisual.height() / 2);
            enemyVisual.node().getStyleClass().remove("enemy-node-selected");
            if (enemy.equals(selectedEnemy)) {
                enemyVisual.node().getStyleClass().add("enemy-node-selected");
            }
        }

        doorNode.toFront();
        itemNode.toFront();
        playerNode.toFront();
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

    private void ensureSelectedEnemy() {
        if (selectedEnemy != null && selectedEnemy.isAlive() && currentGameState.getCurrentLevel().getEnemies().contains(selectedEnemy)) {
            return;
        }

        selectedEnemy = currentGameState.getCurrentLevel().getEnemies().stream()
                .filter(Enemy::isAlive)
                .findFirst()
                .orElse(null);
    }

    private void synchronizeEnemyPositionsWithCurrentLevel() {
        Map<Enemy, Point2D> synchronizedPositions = new LinkedHashMap<>();
        List<Enemy> aliveEnemies = currentGameState.getCurrentLevel().getEnemies().stream()
                .filter(Enemy::isAlive)
                .toList();

        for (int index = 0; index < aliveEnemies.size(); index++) {
            Enemy enemy = aliveEnemies.get(index);
            synchronizedPositions.put(
                    enemy,
                    enemyPositions.getOrDefault(enemy, computeEnemyPosition(index, aliveEnemies.size()))
            );
        }

        enemyPositions.clear();
        enemyPositions.putAll(synchronizedPositions);
        rebuildEnemyVisuals();
    }

    private void seedEnemyPositions() {
        enemyPositions.clear();
        List<Enemy> aliveEnemies = currentGameState.getCurrentLevel().getEnemies().stream()
                .filter(Enemy::isAlive)
                .toList();

        for (int index = 0; index < aliveEnemies.size(); index++) {
            enemyPositions.put(aliveEnemies.get(index), computeEnemyPosition(index, aliveEnemies.size()));
        }
    }

    private void handleEnemySelected(Enemy enemy) {
        selectedEnemy = enemy;
        updateHud();
        renderDynamicScene();
    }

    private void handleDoorClicked(MouseEvent event) {
        event.consume();
        interactionRequested = true;
    }

    private void handleChooseElement(ElementType elementType) {
        try {
            gameService.attuneCurrentPlayerToOriginStone(elementType);
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    private void handleChooseReward(LevelRewardChoice rewardChoice) {
        try {
            gameService.chooseCurrentLevelReward(rewardChoice);
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    private void handleUseInventoryOverlayItem(Item item) {
        try {
            if (item instanceof Potion) {
                CombatResult result = gameService.useItem(item);
                inventoryOverlayFeedbackLabel.setText(result.getMessage());
            } else if (item instanceof Weapon || item instanceof Armor) {
                CombatResult result = gameService.useItem(item);
                currentGameState = gameService.getCurrentGameState();
                currentGameState.getPlayer().removeItem(item);
                inventoryOverlayFeedbackLabel.setText(result.getMessage() + ". Equipaggiato.");
            } else {
                inventoryOverlayFeedbackLabel.setText(item.getName() + " non e usabile da questo menu.");
            }

            refreshView();
        } catch (RuntimeException exception) {
            inventoryOverlayFeedbackLabel.setText("Uso inventario fallito: " + exception.getMessage());
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
            return "Muoviti nell'arena, evita i nemici e attacca quando il bersaglio e in portata.";
        }

        if (hasPendingCompletionDrop()) {
            return isPlayerNearInteractiveItem()
                    ? "Raccogli l'oggetto del livello."
                    : "Raggiungi l'oggetto del livello per raccoglierlo.";
        }

        if (needsElementChoice()) {
            return "Scegli un elemento per attivare la Pietra dell'Origine.";
        }

        if (hasPendingRewardChoice()) {
            return "Scegli una ricompensa prima di aprire la porta.";
        }

        if (currentGameState.isDemoCompleted()) {
            return "La porta finale e aperta: la demo e completa.";
        }

        return isPlayerNearDoor()
                ? "La porta e aperta. Attraversala per raggiungere il prossimo livello."
                : "La porta e aperta: raggiungila per accedere al prossimo livello.";
    }

    private String buildContextualHintText() {
        if (needsElementChoice()) {
            return "1-4: Scegli elemento  |  1 Fuoco, 2 Acqua, 3 Vento, 4 Terra";
        }

        if (hasPendingRewardChoice()) {
            StringJoiner stringJoiner = new StringJoiner(
                    "  |  ",
                    "1-" + currentGameState.getCurrentLevel().getRewardChoices().size() + ": Ricompensa  |  ",
                    ""
            );
            int optionIndex = 1;
            for (LevelRewardChoice rewardChoice : currentGameState.getCurrentLevel().getRewardChoices().keySet()) {
                stringJoiner.add(optionIndex + " " + rewardChoice.getLabel());
                optionIndex++;
            }
            return stringJoiner.toString();
        }

        if (selectedEnemy != null && !isEnemyInAttackRange(selectedEnemy)) {
            return "Avvicinati al bersaglio prima di attaccare.";
        }

        if (hasPendingCompletionDrop() && !isPlayerNearInteractiveItem()) {
            return "Raggiungi l'oggetto e premi E per raccoglierlo.";
        }

        if (canOpenDoor() && !isPlayerNearDoor()) {
            return "Raggiungi la porta e premi E per attraversarla.";
        }

        return "";
    }

    private Point2D resolveMovementDirection() {
        double horizontal = 0;
        double vertical = 0;

        if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)) {
            horizontal -= 1;
        }
        if (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) {
            horizontal += 1;
        }
        if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)) {
            vertical -= 1;
        }
        if (pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)) {
            vertical += 1;
        }

        return new Point2D(horizontal, vertical);
    }

    private boolean isMovementKey(KeyCode keyCode) {
        return keyCode == KeyCode.W
                || keyCode == KeyCode.A
                || keyCode == KeyCode.S
                || keyCode == KeyCode.D
                || keyCode == KeyCode.UP
                || keyCode == KeyCode.DOWN
                || keyCode == KeyCode.LEFT
                || keyCode == KeyCode.RIGHT;
    }

    private boolean handleContextualSelectionKey(KeyCode keyCode) {
        int selectionIndex = resolveSelectionIndex(keyCode);
        if (selectionIndex < 0) {
            return false;
        }

        if (needsElementChoice()) {
            ElementType[] elementTypes = ElementType.values();
            if (selectionIndex >= elementTypes.length) {
                return true;
            }
            handleChooseElement(elementTypes[selectionIndex]);
            return true;
        }

        if (hasPendingRewardChoice()) {
            List<LevelRewardChoice> rewardChoices = currentGameState.getCurrentLevel().getRewardChoices()
                    .keySet()
                    .stream()
                    .toList();
            if (selectionIndex >= rewardChoices.size()) {
                return true;
            }
            handleChooseReward(rewardChoices.get(selectionIndex));
            return true;
        }

        return false;
    }

    private int resolveSelectionIndex(KeyCode keyCode) {
        return switch (keyCode) {
            case DIGIT1, NUMPAD1 -> 0;
            case DIGIT2, NUMPAD2 -> 1;
            case DIGIT3, NUMPAD3 -> 2;
            case DIGIT4, NUMPAD4 -> 3;
            default -> -1;
        };
    }

    private Point2D clampToBoard(Point2D point, double radius) {
        double clampedX = Math.max(radius, Math.min(BOARD_WIDTH - radius, point.getX()));
        double clampedY = Math.max(radius, Math.min(BOARD_HEIGHT - radius, point.getY()));
        return new Point2D(clampedX, clampedY);
    }

    private boolean isPlayerNearInteractiveItem() {
        return playerPosition.distance(getInteractiveItemCenter()) <= INTERACTION_RANGE;
    }

    private boolean isPlayerNearDoor() {
        return playerPosition.distance(getDoorCenter()) <= INTERACTION_RANGE;
    }

    private boolean isEnemyInAttackRange(Enemy enemy) {
        Point2D enemyPosition = enemyPositions.get(enemy);
        if (enemyPosition == null) {
            return false;
        }

        return playerPosition.distance(enemyPosition) <= MELEE_ATTACK_RANGE;
    }

    private double enemyCollisionRadius(Enemy enemy) {
        if (enemy instanceof BossEnemy) {
            return 72;
        }
        if (enemy instanceof Slime) {
            return 44;
        }
        return 52;
    }

    private Point2D getInteractiveItemCenter() {
        return new Point2D(
                ITEM_POSITION.getX() + (ITEM_NODE_SIZE / 2),
                ITEM_POSITION.getY() + (ITEM_NODE_SIZE / 2)
        );
    }

    private Point2D getDoorCenter() {
        return new Point2D(
                DOOR_POSITION.getX() + (DOOR_NODE_WIDTH / 2),
                DOOR_POSITION.getY() + (DOOR_NODE_HEIGHT / 2)
        );
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
        Class<? extends Enemy> enemyType = enemy.getClass();
        Image cachedImage = enemySpriteCache.get(enemyType);
        if (cachedImage != null) {
            return cachedImage;
        }

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

        Image enemySprite = new Image(stream);
        enemySpriteCache.put(enemyType, enemySprite);
        return enemySprite;
    }

    private record BoardPalette(Color primaryTileColor, Color secondaryTileColor, Color borderColor) {
    }

    private record EnemyVisual(
            StackPane node,
            Label hpChip,
            Circle rangeIndicator,
            double width,
            double height
    ) {
    }
}
