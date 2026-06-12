package it.unicam.cs.mpgc.rpg125716.frontend.controller.game;

import it.unicam.cs.mpgc.rpg125716.frontend.scene.SceneNavigator;
import it.unicam.cs.mpgc.rpg125716.model.character.ElementType;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.BossEnemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Goblin;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Skeleton;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Slime;
import it.unicam.cs.mpgc.rpg125716.model.item.Armor;
import it.unicam.cs.mpgc.rpg125716.model.item.BossSword;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
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
import javafx.scene.transform.Scale;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
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
    private static final double BOARD_DISPLAY_SCALE = 0.80d;
    private static final double DISPLAY_BOARD_WIDTH = BOARD_WIDTH * BOARD_DISPLAY_SCALE;
    private static final double DISPLAY_BOARD_HEIGHT = BOARD_HEIGHT * BOARD_DISPLAY_SCALE;
    private static final double TILE_SIZE = 102;
    private static final double PLAYER_NODE_SIZE = 116;
    private static final double ITEM_NODE_SIZE = 102;
    private static final double DOOR_NODE_WIDTH = 104;
    private static final double DOOR_NODE_HEIGHT = 136;
    private static final String ORIGIN_STONE_DROP_SPRITE_PATH = "/images/items/origin-stone-drop.png";
    private static final String PLAYER_SPRITE_PATH = "/images/player/hero-player-source.png";
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
    private static final double FIREBALL_SPEED = 860;
    private static final double WATER_SURGE_DURATION_SECONDS = 10;
    private static final double WIND_TORNADO_DURATION_SECONDS = 5;
    private static final double WIND_TORNADO_TICK_SECONDS = 2;
    private static final double HEART_PULSE_DURATION_SECONDS = 1.4;
    private static final double WATER_KNOCKBACK_DISTANCE = 180;
    private static final int FIREBALL_BONUS_DAMAGE = 4;
    private static final int STONE_POWER_HIT_CHARGE_TARGET = 5;
    private static final int STONE_POWER_HIT_TAKEN_CHARGE_TARGET = 3;
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
    private static final Color PLAYER_FIRE_AURA_COLOR = Color.web("#cf3c2f");
    private static final Color PLAYER_WATER_AURA_COLOR = Color.web("#2d78d2");
    private static final Color PLAYER_WIND_AURA_COLOR = Color.web("#3f9c4b");
    private static final Color PLAYER_EARTH_AURA_COLOR = Color.web("#6f4a2e");
    private static final Color DROP_COLOR = Color.web("#d4af37");
    private static final Color REWARD_COLOR = Color.web("#6b3fa0");
    private static final Color DOOR_LOCKED_COLOR = Color.web("#41304d");
    private static final Color DOOR_OPEN_COLOR = Color.web("#d4af37");
    private static final Color FIRE_POWER_COLOR = Color.web("#ff7b39");
    private static final Color WATER_POWER_COLOR = Color.web("#3fb9d8");
    private static final Color WIND_POWER_COLOR = Color.web("#93d7f7");
    private static final Color HEART_POWER_COLOR = Color.web("#ff647f");

    private final SceneNavigator sceneNavigator;
    private final GameService gameService;
    private CurrentGameState currentGameState;
    private final EventHandler<KeyEvent> globalKeyPressedHandler = this::handleGlobalKeyPressed;
    private final EventHandler<KeyEvent> globalKeyReleasedHandler = this::handleGlobalKeyReleased;
    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private final Map<Enemy, Point2D> enemyPositions = new IdentityHashMap<>();
    private final Map<Enemy, EnemyVisual> enemyVisuals = new IdentityHashMap<>();
    private final Map<Class<? extends Enemy>, Image> enemySpriteCache = new HashMap<>();
    private final List<ArenaObstacle> arenaObstacles = new ArrayList<>();
    private final List<ProjectileEffect> activeProjectiles = new ArrayList<>();

    private Enemy selectedEnemy;
    private boolean inventoryOverlayOpen;
    private boolean attackRequested;
    private boolean interactionRequested;
    private boolean specialPowerRequested;
    private boolean waterDodgesNextCounterAttack;
    private long lastFrameNanos;
    private double attackCooldownRemaining;
    private double waterSurgeRemaining;
    private Enemy windTornadoEnemy;
    private double windTornadoRemaining;
    private double windTornadoTickAccumulator;
    private double heartPulseRemaining;
    private Point2D playerPosition = PLAYER_START_POSITION;
    private Scene boundScene;
    private AnimationTimer gameLoop;
    private Pane boardContentPane;
    private ImageView playerSpriteView;
    private StackPane playerNode;
    private StackPane doorNode;
    private Rectangle doorPortal;
    private Label doorNodeLabel;
    private StackPane itemNode;
    private Circle itemHalo;
    private Circle itemCore;
    private Label itemNodeLabel;
    private ImageView itemSpriteView;
    private Pane specialEffectsPane;

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
    private Label stonePowerHudLabel;
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
    @FXML
    private StackPane elementChoiceOverlay;
    @FXML
    private Label elementChoiceDescriptionLabel;
    @FXML
    private Button elementFireButton;
    @FXML
    private Button elementWaterButton;
    @FXML
    private Button elementWindButton;
    @FXML
    private Button elementEarthButton;
    @FXML
    private StackPane rewardChoiceOverlay;
    @FXML
    private Label rewardChoiceDescriptionLabel;
    @FXML
    private Button rewardPotionButton;
    @FXML
    private Button rewardHelmetButton;

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
        gameBoardPane.setPrefSize(DISPLAY_BOARD_WIDTH, DISPLAY_BOARD_HEIGHT);
        gameBoardPane.setMinSize(DISPLAY_BOARD_WIDTH, DISPLAY_BOARD_HEIGHT);
        gameBoardPane.setMaxSize(DISPLAY_BOARD_WIDTH, DISPLAY_BOARD_HEIGHT);
        gameBoardPane.setClip(new Rectangle(DISPLAY_BOARD_WIDTH, DISPLAY_BOARD_HEIGHT));
        inventoryOverlay.setManaged(false);
        inventoryOverlay.setVisible(false);
        elementChoiceOverlay.setManaged(false);
        elementChoiceOverlay.setVisible(false);
        rewardChoiceOverlay.setManaged(false);
        rewardChoiceOverlay.setVisible(false);

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
            CurrentGameState claimedState = gameService.claimCurrentLevelCompletionDrop();
            currentGameState = claimedState;

            if (currentLevel.isEndsDemoWithVictory() && currentLevel.getCompletionDrop() instanceof BossSword) {
                gameService.completeCurrentLevel();
                stopGameLoop();
                sceneNavigator.showDemoCompleted();
                return;
            }

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
            Player playerBeforeAdvance = new Player(currentGameState.getPlayer());
            CurrentGameState nextState = gameService.completeCurrentLevel();
            String message = nextState.isDemoCompleted()
                    ? "Hai completato la demo e sconfitto il boss finale."
                    : "Hai attraversato la porta. Il prossimo livello e pronto.";
            stopGameLoop();
            if (nextState.isDemoCompleted()) {
                sceneNavigator.showGameOverview(nextState, message);
            } else {
                sceneNavigator.showLevelTransitionToGameOverview(
                        nextState,
                        message,
                        buildLevelTransitionProgressionSummary(playerBeforeAdvance, nextState.getPlayer())
                );
            }
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
        arenaObstacles.clear();
        arenaObstacles.addAll(buildArenaObstacles());
        playerPosition = resolveArenaPosition(PLAYER_START_POSITION, PLAYER_COLLISION_RADIUS);
        seedEnemyPositions();
        ensureSelectedEnemy();
    }

    private void initializeBoardScene() {
        gameBoardPane.getChildren().clear();
        boardContentPane = new Pane();
        boardContentPane.setManaged(false);
        boardContentPane.setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        boardContentPane.getTransforms().setAll(new Scale(BOARD_DISPLAY_SCALE, BOARD_DISPLAY_SCALE, 0, 0));
        gameBoardPane.getChildren().add(boardContentPane);
        renderBoardTiles();
        renderArenaStructures();
        specialEffectsPane = buildSpecialEffectsPane();
        doorNode = buildDoorNode();
        itemNode = buildItemNode();
        playerNode = buildPlayerNode();
        boardContentPane.getChildren().addAll(specialEffectsPane, doorNode, itemNode, playerNode);
        rebuildEnemyVisuals();
        renderDynamicScene();
    }

    private void refreshView() {
        currentGameState = gameService.getCurrentGameState();
        synchronizeEnemyPositionsWithCurrentLevel();
        ensureSelectedEnemy();
        updateElementChoiceOverlay();
        updateRewardChoiceOverlay();
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
            if (needsElementChoice() || hasPendingRewardChoice()) {
                event.consume();
                return;
            }
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

        if (needsElementChoice() || hasPendingRewardChoice()) {
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

        if (keyCode == KeyCode.R) {
            specialPowerRequested = true;
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
        updateMouseTransparencyState();
        pressedKeys.clear();
        attackRequested = false;
        interactionRequested = false;
        specialPowerRequested = false;
        renderInventoryOverlay();
        inventoryOverlay.requestFocus();
    }

    private void closeInventoryOverlay() {
        inventoryOverlayOpen = false;
        inventoryOverlay.setVisible(false);
        inventoryOverlay.setManaged(false);
        updateMouseTransparencyState();
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
        specialPowerRequested = false;
        lastFrameNanos = 0L;
    }

    private void updateFrame(double deltaSeconds) {
        if (!inventoryOverlayOpen
                && !needsElementChoice()
                && !hasPendingRewardChoice()
                && currentGameState.getPlayer().isAlive()) {
            updateStonePowerEffects(deltaSeconds);
            updatePlayerMovement(deltaSeconds);
            updateEnemyMovement(deltaSeconds);
            resolveEntityCollisions();
            processInteractionRequest();
            processSpecialPowerRequest();
            processAttackRequest();
        } else {
            attackRequested = false;
            interactionRequested = false;
            specialPowerRequested = false;
        }

        attackCooldownRemaining = Math.max(0, attackCooldownRemaining - deltaSeconds);
        heartPulseRemaining = Math.max(0, heartPulseRemaining - deltaSeconds);
        updateHud();
        renderDynamicScene();
    }

    private void updatePlayerMovement(double deltaSeconds) {
        Point2D movementDirection = resolveMovementDirection();
        if (movementDirection.magnitude() == 0) {
            return;
        }

        double movementSpeed = PLAYER_MOVE_SPEED + currentGameState.getPlayer().getSpeed() * PLAYER_SPEED_FACTOR;
        Point2D movementDelta = movementDirection.normalize().multiply(movementSpeed * deltaSeconds);
        playerPosition = moveInsideArena(
                playerPosition,
                movementDelta,
                PLAYER_COLLISION_RADIUS
        );
    }

    private void updateEnemyMovement(double deltaSeconds) {
        for (Enemy enemy : currentGameState.getCurrentLevel().getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }

            if (enemy.equals(windTornadoEnemy) && windTornadoRemaining > 0) {
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

            Point2D movementDelta = direction.normalize().multiply(enemySpeed * deltaSeconds);
            enemyPositions.put(
                    enemy,
                    moveInsideArena(enemyPosition, movementDelta, enemyCollisionRadius(enemy))
            );
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
                        resolveArenaPosition(firstPosition.subtract(normal.multiply(correction)), enemyCollisionRadius(firstEnemy))
                );
                enemyPositions.put(
                        secondEnemy,
                        resolveArenaPosition(secondPosition.add(normal.multiply(correction)), enemyCollisionRadius(secondEnemy))
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
            playerPosition = resolveArenaPosition(
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

        resolveAttackOnEnemy(selectedEnemy, shouldEnemyCounterAttackOnBasicAttack(selectedEnemy), true, true);
        attackCooldownRemaining = ATTACK_COOLDOWN_SECONDS;
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
                        + "  SCHIVA " + currentGameState.getPlayer().getDodgeChancePercentage() + "%"
        );
        playerElementHudLabel.setText(
                currentGameState.getPlayer().getElementType() == null
                        ? "Elemento non scelto"
                        : "Elemento: " + currentGameState.getPlayer().getElementType().name()
                        + "  |  Potere pietra: " + resolveStonePowerName()
        );
        stonePowerHudLabel.setText(buildStonePowerHudText());
        contextualHintLabel.setText(buildContextualHintText());
        remainingEnemiesHudLabel.setText("Nemici: " + currentLevel.getRemainingEnemies());
        doorStatusLabel.setText(canOpenDoor() ? "Porta aperta" : "Porta sigillata");

        if (selectedEnemy == null) {
            selectedEnemyLabel.setText("Nessun bersaglio");
            selectedEnemyStatsLabel.setText("Seleziona un nemico sulla mappa.");
        } else {
            String windLockSuffix = selectedEnemy.equals(windTornadoEnemy) && windTornadoRemaining > 0
                    ? "  TORNADO " + (int) Math.ceil(windTornadoRemaining) + "s"
                    : "";
            selectedEnemyLabel.setText(selectedEnemy.getName());
            selectedEnemyStatsLabel.setText(
                    "HP " + selectedEnemy.getHp()
                            + "  ATK " + selectedEnemy.getAttack()
                            + "  DEF " + selectedEnemy.getDefense()
                            + (isEnemyInAttackRange(selectedEnemy) ? "  IN PORTATA" : "  FUORI PORTATA")
                            + windLockSuffix
            );
        }
    }

    private void renderDynamicScene() {
        renderDoorNodeState();
        renderItemNodeState();
        renderPlayerNodeState();
        renderEnemyNodeStates();
        renderStonePowerEffects();
        updateElementChoiceOverlay();
        updateRewardChoiceOverlay();
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
                boardContentPane.getChildren().add(tile);
            }
        }

        Rectangle border = new Rectangle(BOARD_WIDTH, BOARD_HEIGHT);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(boardPalette.borderColor());
        border.setStrokeWidth(10);
        border.setArcWidth(32);
        border.setArcHeight(32);
        boardContentPane.getChildren().add(border);
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
        boolean upperCluster = row == 0 && column >= 4 && column <= 5;
        boolean bottomPatch = row == rows - 1 && column >= 3 && column <= 5;
        return leftGrove || upperCluster || bottomPatch;
    }

    private void renderArenaStructures() {
        for (ArenaObstacle obstacle : arenaObstacles) {
            boardContentPane.getChildren().add(buildArenaObstacleNode(obstacle));
        }
    }

    private Pane buildArenaObstacleNode(ArenaObstacle obstacle) {
        Pane obstacleNode = new Pane();
        obstacleNode.setMouseTransparent(true);
        obstacleNode.setManaged(false);
        obstacleNode.setLayoutX(obstacle.x());
        obstacleNode.setLayoutY(obstacle.y());
        obstacleNode.setPrefSize(obstacle.width(), obstacle.height());

        switch (obstacle.style()) {
            case TREE -> populateTreeObstacleNode(obstacleNode, obstacle);
            case FORTRESS_WALL -> populateFortressWallNode(obstacleNode, obstacle);
            case STONE_WALL -> populateStoneWallNode(obstacleNode, obstacle);
            case CLOSED_DOOR -> populateClosedDoorNode(obstacleNode, obstacle);
            case RUBBLE -> populateRubbleNode(obstacleNode, obstacle);
            case PILLAR -> populatePillarNode(obstacleNode, obstacle);
        }

        return obstacleNode;
    }

    private void populateTreeObstacleNode(Pane obstacleNode, ArenaObstacle obstacle) {
        Rectangle trunk = new Rectangle(obstacle.width() * 0.22, obstacle.height() * 0.34, Color.web("#5a4330"));
        trunk.setArcWidth(10);
        trunk.setArcHeight(10);
        trunk.setLayoutX((obstacle.width() - trunk.getWidth()) / 2);
        trunk.setLayoutY(obstacle.height() * 0.54);

        Circle canopyLeft = new Circle(obstacle.width() * 0.24, Color.web("#2f6a39"));
        canopyLeft.setCenterX(obstacle.width() * 0.34);
        canopyLeft.setCenterY(obstacle.height() * 0.42);
        Circle canopyRight = new Circle(obstacle.width() * 0.22, Color.web("#3c7c47"));
        canopyRight.setCenterX(obstacle.width() * 0.64);
        canopyRight.setCenterY(obstacle.height() * 0.40);
        Circle canopyTop = new Circle(obstacle.width() * 0.26, Color.web("#498a4f"));
        canopyTop.setCenterX(obstacle.width() * 0.50);
        canopyTop.setCenterY(obstacle.height() * 0.26);

        obstacleNode.getChildren().addAll(trunk, canopyLeft, canopyRight, canopyTop);
    }

    private void populateFortressWallNode(Pane obstacleNode, ArenaObstacle obstacle) {
        Rectangle wallBody = buildArenaRectangle(
                obstacle.width(),
                obstacle.height(),
                Color.web("#3f4654"),
                Color.web("#6d7384"),
                18
        );
        Rectangle wallInset = buildArenaRectangle(
                Math.max(0, obstacle.width() - 18),
                Math.max(0, obstacle.height() - 18),
                Color.web("#2f3440"),
                Color.TRANSPARENT,
                14
        );
        wallInset.setLayoutX(9);
        wallInset.setLayoutY(9);
        obstacleNode.getChildren().addAll(wallBody, wallInset);
    }

    private void populateStoneWallNode(Pane obstacleNode, ArenaObstacle obstacle) {
        Rectangle wallBody = buildArenaRectangle(
                obstacle.width(),
                obstacle.height(),
                Color.web("#4e5668"),
                Color.web("#7b8397"),
                18
        );
        Rectangle wallInset = buildArenaRectangle(
                Math.max(0, obstacle.width() - 16),
                Math.max(0, obstacle.height() - 16),
                Color.web("#3f4756"),
                Color.TRANSPARENT,
                14
        );
        wallInset.setLayoutX(8);
        wallInset.setLayoutY(8);
        obstacleNode.getChildren().addAll(wallBody, wallInset);
    }

    private void populateClosedDoorNode(Pane obstacleNode, ArenaObstacle obstacle) {
        Rectangle frame = buildArenaRectangle(
                obstacle.width(),
                obstacle.height(),
                Color.web("#241b16"),
                Color.web("#9b7d56"),
                18
        );
        Rectangle doorLeaf = buildArenaRectangle(
                Math.max(0, obstacle.width() - 16),
                Math.max(0, obstacle.height() - 14),
                Color.web("#5f3928"),
                Color.web("#d4af37"),
                14
        );
        doorLeaf.setLayoutX(8);
        doorLeaf.setLayoutY(7);

        Rectangle barTop = buildArenaRectangle(obstacle.width() * 0.68, 10, Color.web("#9b7d56"), Color.TRANSPARENT, 8);
        barTop.setLayoutX(obstacle.width() * 0.16);
        barTop.setLayoutY(obstacle.height() * 0.32);

        Rectangle barBottom = buildArenaRectangle(obstacle.width() * 0.68, 10, Color.web("#9b7d56"), Color.TRANSPARENT, 8);
        barBottom.setLayoutX(obstacle.width() * 0.16);
        barBottom.setLayoutY(obstacle.height() * 0.60);

        obstacleNode.getChildren().addAll(frame, doorLeaf, barTop, barBottom);
    }

    private void populateRubbleNode(Pane obstacleNode, ArenaObstacle obstacle) {
        Rectangle body = buildArenaRectangle(
                obstacle.width(),
                obstacle.height(),
                Color.web("#66554a"),
                Color.web("#8a7361"),
                20
        );
        Rectangle shardOne = buildArenaRectangle(obstacle.width() * 0.34, obstacle.height() * 0.28, Color.web("#857263"), Color.TRANSPARENT, 10);
        shardOne.setLayoutX(obstacle.width() * 0.12);
        shardOne.setLayoutY(obstacle.height() * 0.18);
        Rectangle shardTwo = buildArenaRectangle(obstacle.width() * 0.28, obstacle.height() * 0.24, Color.web("#736153"), Color.TRANSPARENT, 10);
        shardTwo.setLayoutX(obstacle.width() * 0.56);
        shardTwo.setLayoutY(obstacle.height() * 0.44);
        obstacleNode.getChildren().addAll(body, shardOne, shardTwo);
    }

    private void populatePillarNode(Pane obstacleNode, ArenaObstacle obstacle) {
        Rectangle base = buildArenaRectangle(
                obstacle.width(),
                obstacle.height(),
                Color.web("#505767"),
                Color.web("#848c9f"),
                26
        );
        Rectangle core = buildArenaRectangle(
                obstacle.width() * 0.58,
                obstacle.height() * 0.58,
                Color.web("#3f4758"),
                Color.TRANSPARENT,
                18
        );
        core.setLayoutX((obstacle.width() - core.getWidth()) / 2);
        core.setLayoutY((obstacle.height() - core.getHeight()) / 2);
        obstacleNode.getChildren().addAll(base, core);
    }

    private Rectangle buildArenaRectangle(double width, double height, Color fill, Color stroke, double arc) {
        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setArcWidth(arc);
        rectangle.setArcHeight(arc);
        rectangle.setFill(fill);
        rectangle.setStroke(stroke);
        rectangle.setStrokeWidth(stroke == Color.TRANSPARENT ? 0 : 2);
        return rectangle;
    }

    private List<ArenaObstacle> buildArenaObstacles() {
        List<ArenaObstacle> obstacles = new ArrayList<>();
        int columns = Math.max(1, (int) Math.floor(BOARD_WIDTH / TILE_SIZE));
        int rows = Math.max(1, (int) Math.floor(BOARD_HEIGHT / TILE_SIZE));
        int levelNumber = currentGameState.getCurrentLevel().getNumber();

        if (levelNumber == 1) {
            addForestArenaObstacles(obstacles, rows, columns);
        } else if (levelNumber == 2) {
            addPursuitArenaObstacles(obstacles);
        } else if (levelNumber == 3) {
            addBossArenaObstacles(obstacles);
        }

        return obstacles;
    }

    private void addForestArenaObstacles(List<ArenaObstacle> obstacles, int rows, int columns) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (!isForestTreeTile(row, column, rows, columns)) {
                    continue;
                }
                obstacles.add(new ArenaObstacle(
                        column * TILE_SIZE + 6,
                        row * TILE_SIZE + 6,
                        TILE_SIZE - 14,
                        TILE_SIZE - 14,
                        ArenaObstacleStyle.TREE
                ));
            }
        }

        double fortressX = BOARD_WIDTH - 250;
        double topWallHeight = DOOR_POSITION.getY() - 24;
        double bottomWallY = DOOR_POSITION.getY() + DOOR_NODE_HEIGHT + 20;
        obstacles.add(new ArenaObstacle(fortressX, 0, 250, topWallHeight, ArenaObstacleStyle.FORTRESS_WALL));
        obstacles.add(new ArenaObstacle(
                fortressX,
                bottomWallY,
                250,
                BOARD_HEIGHT - bottomWallY,
                ArenaObstacleStyle.FORTRESS_WALL
        ));
        obstacles.add(new ArenaObstacle(BOARD_WIDTH - 346, 0, 96, 116, ArenaObstacleStyle.FORTRESS_WALL));
        obstacles.add(new ArenaObstacle(BOARD_WIDTH - 346, BOARD_HEIGHT - 116, 96, 116, ArenaObstacleStyle.FORTRESS_WALL));
    }

    private void addPursuitArenaObstacles(List<ArenaObstacle> obstacles) {
        obstacles.add(new ArenaObstacle(610, 0, 178, 118, ArenaObstacleStyle.STONE_WALL));
        obstacles.add(new ArenaObstacle(988, BOARD_HEIGHT - 118, 178, 118, ArenaObstacleStyle.STONE_WALL));
        obstacles.add(new ArenaObstacle(648, 24, 102, 90, ArenaObstacleStyle.CLOSED_DOOR));
        obstacles.add(new ArenaObstacle(1026, BOARD_HEIGHT - 114, 102, 90, ArenaObstacleStyle.CLOSED_DOOR));
        obstacles.add(new ArenaObstacle(560, 220, 96, 126, ArenaObstacleStyle.PILLAR));
        obstacles.add(new ArenaObstacle(858, 204, 132, 92, ArenaObstacleStyle.RUBBLE));
        obstacles.add(new ArenaObstacle(964, 92, 124, 72, ArenaObstacleStyle.RUBBLE));
        obstacles.add(new ArenaObstacle(952, 354, 130, 68, ArenaObstacleStyle.RUBBLE));
    }

    private void addBossArenaObstacles(List<ArenaObstacle> obstacles) {
        obstacles.add(new ArenaObstacle(544, 124, 116, 116, ArenaObstacleStyle.PILLAR));
        obstacles.add(new ArenaObstacle(544, 306, 116, 116, ArenaObstacleStyle.PILLAR));
        obstacles.add(new ArenaObstacle(924, 124, 116, 116, ArenaObstacleStyle.PILLAR));
        obstacles.add(new ArenaObstacle(924, 306, 116, 116, ArenaObstacleStyle.PILLAR));
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
        builtPlayerNode.setPickOnBounds(false);

        playerSpriteView = new ImageView(loadPlayerSprite());
        playerSpriteView.setFitWidth(128);
        playerSpriteView.setFitHeight(128);
        playerSpriteView.setPreserveRatio(true);
        playerSpriteView.setSmooth(true);

        builtPlayerNode.getChildren().add(playerSpriteView);
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
        itemSpriteView = new ImageView(loadOriginStoneDropSprite());
        itemSpriteView.setFitWidth(82);
        itemSpriteView.setFitHeight(82);
        itemSpriteView.setPreserveRatio(true);
        itemSpriteView.setVisible(false);
        itemSpriteView.setManaged(false);
        itemNodeLabel = new Label("+");
        itemNodeLabel.getStyleClass().add("board-item-label");

        builtItemNode.getChildren().addAll(itemHalo, itemCore, itemSpriteView, itemNodeLabel);
        return builtItemNode;
    }

    private Pane buildSpecialEffectsPane() {
        Pane pane = new Pane();
        pane.setManaged(false);
        pane.setMouseTransparent(true);
        pane.setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        return pane;
    }

    private void rebuildEnemyVisuals() {
        enemyVisuals.values().forEach(enemyVisual -> {
            if (enemyVisual.rangeIndicator() != null) {
                boardContentPane.getChildren().remove(enemyVisual.rangeIndicator());
            }
            boardContentPane.getChildren().remove(enemyVisual.node());
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
                boardContentPane.getChildren().add(rangeIndicator);
            }

            ImageView imageView = new ImageView(loadEnemySprite(enemy));
            double width = enemy instanceof BossEnemy ? 184 : enemy instanceof Slime ? 104 : 122;
            double height = enemy instanceof BossEnemy ? 184 : enemy instanceof Slime ? 104 : 122;
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

            boardContentPane.getChildren().add(enemyNode);
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
        if (shouldRenderOriginStoneDropSprite()) {
            itemCore.setVisible(false);
            itemCore.setManaged(false);
            itemSpriteView.setVisible(true);
            itemSpriteView.setManaged(true);
            itemNodeLabel.setVisible(false);
            itemNodeLabel.setManaged(false);
            itemHalo.setOpacity(0.34);
        } else {
            itemCore.setVisible(true);
            itemCore.setManaged(true);
            itemSpriteView.setVisible(false);
            itemSpriteView.setManaged(false);
            itemNodeLabel.setVisible(true);
            itemNodeLabel.setManaged(true);
            itemNodeLabel.setText(symbol);
            itemHalo.setOpacity(0.24);
        }
    }

    private void renderPlayerNodeState() {
        Color playerAuraColor = resolvePlayerAuraColor();
        playerSpriteView.setEffect(buildPlayerSpriteAuraEffect(playerAuraColor));
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
                    resolveArenaPosition(
                            enemyPositions.getOrDefault(enemy, computeEnemyPosition(index, aliveEnemies.size())),
                            enemyCollisionRadius(enemy)
                    )
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
            Enemy enemy = aliveEnemies.get(index);
            enemyPositions.put(
                    enemy,
                    resolveArenaPosition(computeEnemyPosition(index, aliveEnemies.size()), enemyCollisionRadius(enemy))
            );
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

    @FXML
    private void handleChooseFireElement() {
        handleChooseElement(ElementType.FIRE);
    }

    @FXML
    private void handleChooseWaterElement() {
        handleChooseElement(ElementType.WATER);
    }

    @FXML
    private void handleChooseWindElement() {
        handleChooseElement(ElementType.WIND);
    }

    @FXML
    private void handleChooseEarthElement() {
        handleChooseElement(ElementType.EARTH);
    }

    private void handleChooseReward(LevelRewardChoice rewardChoice) {
        try {
            gameService.chooseCurrentLevelReward(rewardChoice);
            refreshView();
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    @FXML
    private void handleChoosePotionReward() {
        handleChooseReward(LevelRewardChoice.HEALING_POTION);
    }

    @FXML
    private void handleChooseHelmetReward() {
        handleChooseReward(LevelRewardChoice.DEFENSE_HELMET);
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

    private String buildLevelTransitionProgressionSummary(Player playerBeforeAdvance, Player playerAfterAdvance) {
        StringBuilder summaryBuilder = new StringBuilder();
        appendProgressionLine(
                summaryBuilder,
                "Level Up! LV " + playerBeforeAdvance.getLevel() + " -> " + playerAfterAdvance.getLevel()
        );

        appendDeltaLine(summaryBuilder, "HP", playerAfterAdvance.getMaxHp() - playerBeforeAdvance.getMaxHp());
        appendDeltaLine(summaryBuilder, "ATK", playerAfterAdvance.getAttack() - playerBeforeAdvance.getAttack());
        appendDeltaLine(summaryBuilder, "DEF", playerAfterAdvance.getDefense() - playerBeforeAdvance.getDefense());
        appendDeltaLine(summaryBuilder, "SPD", playerAfterAdvance.getSpeed() - playerBeforeAdvance.getSpeed());

        if (!playerBeforeAdvance.isStoneSuperPowerUnlocked() && playerAfterAdvance.isStoneSuperPowerUnlocked()) {
            appendProgressionLine(summaryBuilder, "Pietra sbloccata: " + resolveStonePowerName(playerAfterAdvance) + " [R]");
        }

        return summaryBuilder.toString();
    }

    private void appendDeltaLine(StringBuilder summaryBuilder, String statLabel, int delta) {
        if (delta <= 0) {
            return;
        }

        appendProgressionLine(summaryBuilder, statLabel + " +" + delta);
    }

    private void appendProgressionLine(StringBuilder summaryBuilder, String line) {
        if (summaryBuilder.length() > 0) {
            summaryBuilder.append(System.lineSeparator());
        }
        summaryBuilder.append(line);
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
        return currentLevel.isCompleted()
                && currentLevel.hasRewardChoices()
                && !currentLevel.isRewardClaimed();
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

    private boolean shouldRenderOriginStoneDropSprite() {
        return hasPendingCompletionDrop()
                && currentGameState.getCurrentLevel().getCompletionDrop() instanceof OriginStone;
    }

    private String buildObjectiveText() {
        if (!currentGameState.getPlayer().isAlive()) {
            return "Il tuo eroe e stato sconfitto. Torna al riepilogo o carica un salvataggio.";
        }

        if (currentGameState.getCurrentLevel().getRemainingEnemies() > 0) {
            if (currentGameState.getCurrentLevel().getNumber() == 2) {
                return "Sconfiggi entrambi i nemici per sbloccare la scelta tra pozione ed elmo +2 DEF.";
            }
            return "Muoviti nell'arena, evita i nemici e attacca quando il bersaglio e in portata.";
        }

        if (hasPendingCompletionDrop()) {
            if (currentGameState.getCurrentLevel().isEndsDemoWithVictory()) {
                return isPlayerNearInteractiveItem()
                        ? "Raccogli la spada del guardiano per concludere la demo."
                        : "Raggiungi la spada del guardiano per concludere la demo.";
            }
            return isPlayerNearInteractiveItem()
                    ? "Raccogli l'oggetto del livello."
                    : "Raggiungi l'oggetto del livello per raccoglierlo.";
        }

        if (needsElementChoice()) {
            return "Scegli un elemento cliccando uno dei quattro cerchi della Pietra dell'Origine.";
        }

        if (hasPendingRewardChoice()) {
            return "Entrambi i nemici sono sconfitti: scegli subito una ricompensa prima di aprire la porta.";
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
            return "Clicca un cerchio oppure premi 1-4  |  FIRE, WATER, WIND, EARTH";
        }

        if (hasPendingRewardChoice()) {
            return "1 Pozione curativa  |  2 Elmo con +2 difesa";
        }

        if (currentGameState.getPlayer().canUseStoneSuperPower()) {
            return "R - " + resolveStonePowerName() + " pronta all'uso";
        }

        if (waterSurgeRemaining > 0) {
            return "Acqua attiva per " + (int) Math.ceil(waterSurgeRemaining)
                    + "s: ogni hit respinge il bersaglio e schivi un attacco si e uno no.";
        }

        if (windTornadoEnemy != null && windTornadoRemaining > 0) {
            return "Tornado attivo su " + windTornadoEnemy.getName()
                    + " per " + (int) Math.ceil(windTornadoRemaining) + "s.";
        }

        if (currentGameState.getPlayer().isStoneSuperPowerUnlocked()) {
            return "Ricarica pietra: "
                    + currentGameState.getPlayer().getStonePowerHitsDealtCharge() + "/" + STONE_POWER_HIT_CHARGE_TARGET
                    + " hit oppure "
                    + currentGameState.getPlayer().getStonePowerHitsTakenCharge() + "/" + STONE_POWER_HIT_TAKEN_CHARGE_TARGET
                    + " colpi subiti.";
        }

        if (selectedEnemy != null && !isEnemyInAttackRange(selectedEnemy)) {
            return "Avvicinati al bersaglio prima di attaccare.";
        }

        if (hasPendingCompletionDrop() && !isPlayerNearInteractiveItem()) {
            return currentGameState.getCurrentLevel().isEndsDemoWithVictory()
                    ? "Raggiungi la spada e premi E per concludere la demo."
                    : "Raggiungi l'oggetto e premi E per raccoglierlo.";
        }

        if (canOpenDoor() && !isPlayerNearDoor()) {
            return "Raggiungi la porta e premi E per attraversarla.";
        }

        return "";
    }

    private String buildStonePowerHudText() {
        Player player = currentGameState.getPlayer();
        if (player.getElementType() == null) {
            return "Pietra: nessun elemento";
        }

        if (!player.isStoneSuperPowerUnlocked()) {
            return "Pietra: bloccata fino al level up del livello 2";
        }

        if (player.canUseStoneSuperPower()) {
            return "Pietra: pronta [R] - " + resolveStonePowerName(player);
        }

        if (waterSurgeRemaining > 0) {
            return "Pietra: Acqua attiva " + (int) Math.ceil(waterSurgeRemaining) + "s - schiva alternata";
        }

        if (windTornadoEnemy != null && windTornadoRemaining > 0) {
            return "Pietra: Tornado attivo " + (int) Math.ceil(windTornadoRemaining) + "s";
        }

        return "Pietra: ricarica "
                + player.getStonePowerHitsDealtCharge() + "/" + STONE_POWER_HIT_CHARGE_TARGET
                + " hit oppure "
                + player.getStonePowerHitsTakenCharge() + "/" + STONE_POWER_HIT_TAKEN_CHARGE_TARGET
                + " colpi";
    }

    private void processSpecialPowerRequest() {
        if (!specialPowerRequested) {
            return;
        }

        specialPowerRequested = false;
        Player player = currentGameState.getPlayer();
        if (!player.canUseStoneSuperPower()) {
            return;
        }

        ElementType elementType = player.getElementType();
        if (elementType == null) {
            return;
        }

        switch (elementType) {
            case FIRE -> activateFireStonePower(player);
            case WATER -> activateWaterStonePower(player);
            case WIND -> activateWindStonePower(player);
            case EARTH -> activateHeartStonePower(player);
        }
    }

    private void activateFireStonePower(Player player) {
        if (selectedEnemy == null || !selectedEnemy.isAlive()) {
            return;
        }

        player.consumeStoneSuperPower();
        activeProjectiles.add(new ProjectileEffect(selectedEnemy, playerPosition, FIREBALL_SPEED, FIRE_POWER_COLOR, 16));
    }

    private void activateWaterStonePower(Player player) {
        player.consumeStoneSuperPower();
        waterSurgeRemaining = WATER_SURGE_DURATION_SECONDS;
        waterDodgesNextCounterAttack = true;
    }

    private void activateWindStonePower(Player player) {
        if (selectedEnemy == null || !selectedEnemy.isAlive()) {
            return;
        }

        player.consumeStoneSuperPower();
        windTornadoEnemy = selectedEnemy;
        windTornadoRemaining = WIND_TORNADO_DURATION_SECONDS;
        windTornadoTickAccumulator = 0;
    }

    private void activateHeartStonePower(Player player) {
        player.consumeStoneSuperPower();
        player.setCurrentHp(player.getMaxHp());
        heartPulseRemaining = HEART_PULSE_DURATION_SECONDS;
    }

    private void updateStonePowerEffects(double deltaSeconds) {
        updateWaterStonePower(deltaSeconds);
        updateWindStonePower(deltaSeconds);
        updateFireballProjectiles(deltaSeconds);
    }

    private void updateWaterStonePower(double deltaSeconds) {
        waterSurgeRemaining = Math.max(0, waterSurgeRemaining - deltaSeconds);
        if (waterSurgeRemaining <= 0) {
            waterDodgesNextCounterAttack = false;
        }
    }

    private void updateWindStonePower(double deltaSeconds) {
        if (windTornadoEnemy == null) {
            windTornadoRemaining = 0;
            windTornadoTickAccumulator = 0;
            return;
        }

        if (!windTornadoEnemy.isAlive()) {
            clearWindTornado();
            return;
        }

        windTornadoRemaining = Math.max(0, windTornadoRemaining - deltaSeconds);
        windTornadoTickAccumulator += deltaSeconds;

        while (windTornadoRemaining > 0
                && windTornadoTickAccumulator >= WIND_TORNADO_TICK_SECONDS
                && windTornadoEnemy.isAlive()) {
            windTornadoTickAccumulator -= WIND_TORNADO_TICK_SECONDS;
            resolveAttackOnEnemy(windTornadoEnemy, false, false, false);
        }

        if (windTornadoRemaining <= 0 || !windTornadoEnemy.isAlive()) {
            clearWindTornado();
        }
    }

    private void updateFireballProjectiles(double deltaSeconds) {
        if (activeProjectiles.isEmpty()) {
            return;
        }

        List<ProjectileEffect> resolvedProjectiles = new ArrayList<>();
        for (ProjectileEffect projectile : activeProjectiles) {
            Enemy targetEnemy = projectile.targetEnemy();
            if (targetEnemy == null || !targetEnemy.isAlive()) {
                continue;
            }

            Point2D targetPosition = enemyPositions.get(targetEnemy);
            if (targetPosition == null) {
                continue;
            }

            Point2D direction = targetPosition.subtract(projectile.position());
            double distance = direction.magnitude();
            if (distance <= projectile.speed() * deltaSeconds) {
                resolveAttackOnEnemy(targetEnemy, false, true, false, FIREBALL_BONUS_DAMAGE);
                continue;
            }

            Point2D nextPosition = projectile.position().add(direction.normalize().multiply(projectile.speed() * deltaSeconds));
            resolvedProjectiles.add(projectile.withPosition(nextPosition));
        }

        activeProjectiles.clear();
        activeProjectiles.addAll(resolvedProjectiles);
    }

    private void resolveAttackOnEnemy(
            Enemy enemy,
            boolean enemyCanCounterAttack,
            boolean countsTowardStoneCharge,
            boolean applyWaterKnockback
    ) {
        resolveAttackOnEnemy(enemy, enemyCanCounterAttack, countsTowardStoneCharge, applyWaterKnockback, 0);
    }

    private void resolveAttackOnEnemy(
            Enemy enemy,
            boolean enemyCanCounterAttack,
            boolean countsTowardStoneCharge,
            boolean applyWaterKnockback,
            int bonusDamage
    ) {
        try {
            CombatTurnResult turnResult = enemyCanCounterAttack
                    ? gameService.attackCurrentLevelEnemy(enemy)
                    : bonusDamage > 0
                    ? gameService.attackCurrentLevelEnemyWithBonusDamageWithoutCounterAttack(enemy, bonusDamage)
                    : gameService.attackCurrentLevelEnemyWithoutCounterAttack(enemy);
            applyCombatTurnResult(enemy, turnResult, countsTowardStoneCharge, applyWaterKnockback);
        } catch (RuntimeException exception) {
            refreshView();
        }
    }

    private void applyCombatTurnResult(
            Enemy enemy,
            CombatTurnResult turnResult,
            boolean countsTowardStoneCharge,
            boolean applyWaterKnockback
    ) {
        currentGameState = turnResult.getCurrentGameState();
        Player player = currentGameState.getPlayer();

        if (countsTowardStoneCharge && turnResult.getPlayerActionResult().getDamage() > 0) {
            player.registerStonePowerHitDealt();
        }
        if (countsTowardStoneCharge
                && turnResult.getEnemyActionResult() != null
                && turnResult.getEnemyActionResult().getDamage() > 0) {
            player.registerStonePowerHitTaken();
        }

        if (applyWaterKnockback
                && waterSurgeRemaining > 0
                && enemy != null
                && enemy.isAlive()
                && turnResult.getPlayerActionResult().getDamage() > 0) {
            pushEnemyAwayFromPlayer(enemy, WATER_KNOCKBACK_DISTANCE);
        }

        if (enemy != null && enemy.equals(windTornadoEnemy) && !enemy.isAlive()) {
            clearWindTornado();
        }

        synchronizeEnemyPositionsWithCurrentLevel();
        ensureSelectedEnemy();
        updateRewardChoiceOverlay();
    }

    private void pushEnemyAwayFromPlayer(Enemy enemy, double distance) {
        Point2D enemyPosition = enemyPositions.get(enemy);
        if (enemyPosition == null) {
            return;
        }

        Point2D direction = enemyPosition.subtract(playerPosition);
        Point2D normal = direction.magnitude() < 0.001d ? new Point2D(1, 0) : direction.normalize();
        Point2D pushedPosition = resolveArenaPosition(
                enemyPosition.add(normal.multiply(distance)),
                enemyCollisionRadius(enemy)
        );
        enemyPositions.put(enemy, pushedPosition);
    }

    private boolean shouldEnemyCounterAttackOnBasicAttack(Enemy enemy) {
        if (enemy.equals(windTornadoEnemy) && windTornadoRemaining > 0) {
            return false;
        }

        if (waterSurgeRemaining > 0) {
            boolean dodgeCurrentCounterAttack = waterDodgesNextCounterAttack;
            waterDodgesNextCounterAttack = !waterDodgesNextCounterAttack;
            return !dodgeCurrentCounterAttack;
        }

        return true;
    }

    private void clearWindTornado() {
        windTornadoEnemy = null;
        windTornadoRemaining = 0;
        windTornadoTickAccumulator = 0;
    }

    private void renderStonePowerEffects() {
        if (specialEffectsPane == null) {
            return;
        }

        specialEffectsPane.getChildren().clear();

        if (waterSurgeRemaining > 0) {
            Circle aura = new Circle(playerPosition.getX(), playerPosition.getY(), 52, WATER_POWER_COLOR);
            aura.setOpacity(0.14);
            aura.setStroke(WATER_POWER_COLOR);
            aura.setStrokeWidth(4);
            specialEffectsPane.getChildren().add(aura);
        }

        if (heartPulseRemaining > 0) {
            Circle pulse = new Circle(playerPosition.getX(), playerPosition.getY(), 36 + (heartPulseRemaining * 18), HEART_POWER_COLOR);
            pulse.setOpacity(0.18);
            pulse.setStroke(HEART_POWER_COLOR);
            pulse.setStrokeWidth(3);
            specialEffectsPane.getChildren().add(pulse);
        }

        if (windTornadoEnemy != null && windTornadoRemaining > 0) {
            Point2D windEnemyPosition = enemyPositions.get(windTornadoEnemy);
            if (windEnemyPosition != null) {
                Circle tornadoCore = new Circle(windEnemyPosition.getX(), windEnemyPosition.getY() + 16, 44, WIND_POWER_COLOR);
                tornadoCore.setOpacity(0.12);
                tornadoCore.setStroke(WIND_POWER_COLOR);
                tornadoCore.setStrokeWidth(3);

                Circle tornadoRingOne = new Circle(windEnemyPosition.getX(), windEnemyPosition.getY() - 12, 34);
                tornadoRingOne.setFill(Color.TRANSPARENT);
                tornadoRingOne.setStroke(WIND_POWER_COLOR);
                tornadoRingOne.setStrokeWidth(3);
                tornadoRingOne.setOpacity(0.8);

                Circle tornadoRingTwo = new Circle(windEnemyPosition.getX(), windEnemyPosition.getY() + 18, 50);
                tornadoRingTwo.setFill(Color.TRANSPARENT);
                tornadoRingTwo.setStroke(WIND_POWER_COLOR);
                tornadoRingTwo.setStrokeWidth(2);
                tornadoRingTwo.setOpacity(0.55);

                specialEffectsPane.getChildren().addAll(tornadoCore, tornadoRingOne, tornadoRingTwo);
            }
        }

        for (ProjectileEffect projectile : activeProjectiles) {
            Circle outerFireball = new Circle(projectile.position().getX(), projectile.position().getY(), projectile.radius(), projectile.color());
            outerFireball.setOpacity(0.42);
            Circle innerFireball = new Circle(projectile.position().getX(), projectile.position().getY(), projectile.radius() * 0.58, Color.web("#fff3c4"));
            innerFireball.setOpacity(0.92);
            specialEffectsPane.getChildren().addAll(outerFireball, innerFireball);
        }

        specialEffectsPane.toFront();
        itemNode.toFront();
        playerNode.toFront();
        doorNode.toFront();
    }

    private void updateRewardChoiceOverlay() {
        boolean visible = hasPendingRewardChoice();
        rewardChoiceOverlay.setVisible(visible);
        rewardChoiceOverlay.setManaged(visible);

        if (visible) {
            pressedKeys.clear();
            attackRequested = false;
            interactionRequested = false;
            specialPowerRequested = false;
            rewardChoiceDescriptionLabel.setText(
                    "Scegli una reward per il livello 2: pozione curativa oppure elmo con +2 difesa."
            );
        }

        rewardPotionButton.setDisable(!currentGameState.getCurrentLevel().getRewardChoices().containsKey(LevelRewardChoice.HEALING_POTION));
        rewardHelmetButton.setDisable(!currentGameState.getCurrentLevel().getRewardChoices().containsKey(LevelRewardChoice.DEFENSE_HELMET));
        updateMouseTransparencyState();
    }

    private void updateElementChoiceOverlay() {
        boolean visible = needsElementChoice();
        elementChoiceOverlay.setVisible(visible);
        elementChoiceOverlay.setManaged(visible);

        if (visible) {
            pressedKeys.clear();
            attackRequested = false;
            interactionRequested = false;
            specialPowerRequested = false;
            elementChoiceDescriptionLabel.setText(
                    "Scegli il potere della Pietra cliccando uno dei quattro cerchi elementali."
            );
        }

        boolean hasElementChoice = currentGameState.getPlayer().getElementType() == null;
        elementFireButton.setDisable(!hasElementChoice);
        elementWaterButton.setDisable(!hasElementChoice);
        elementWindButton.setDisable(!hasElementChoice);
        elementEarthButton.setDisable(!hasElementChoice);
        updateMouseTransparencyState();
    }

    private void updateMouseTransparencyState() {
        gameContentPane.setMouseTransparent(
                inventoryOverlayOpen
                        || elementChoiceOverlay.isVisible()
                        || rewardChoiceOverlay.isVisible()
        );
    }

    private String resolveStonePowerName() {
        return resolveStonePowerName(currentGameState.getPlayer());
    }

    private String resolveStonePowerName(Player player) {
        if (player.getElementType() == null) {
            return "non disponibile";
        }

        return switch (player.getElementType()) {
            case FIRE -> "FIRE - Palla di fuoco";
            case WATER -> "WATER - Onda di spinta";
            case WIND -> "WIND - Tornado";
            case EARTH -> "HEART - Cura totale";
        };
    }

    private Color resolvePlayerAuraColor() {
        ElementType elementType = currentGameState.getPlayer().getElementType();
        if (elementType == null) {
            return null;
        }

        return switch (elementType) {
            case FIRE -> PLAYER_FIRE_AURA_COLOR;
            case WATER -> PLAYER_WATER_AURA_COLOR;
            case WIND -> PLAYER_WIND_AURA_COLOR;
            case EARTH -> PLAYER_EARTH_AURA_COLOR;
        };
    }

    private DropShadow buildPlayerSpriteAuraEffect(Color auraColor) {
        if (auraColor == null) {
            return null;
        }

        DropShadow innerGlow = new DropShadow();
        innerGlow.setColor(auraColor.deriveColor(0, 1, 1, 0.90));
        innerGlow.setRadius(10);
        innerGlow.setSpread(0.55);
        innerGlow.setOffsetX(0);
        innerGlow.setOffsetY(0);

        DropShadow outerGlow = new DropShadow();
        outerGlow.setColor(auraColor.deriveColor(0, 1, 0.92, 0.96));
        outerGlow.setRadius(20);
        outerGlow.setSpread(0.68);
        outerGlow.setOffsetX(0);
        outerGlow.setOffsetY(0);
        outerGlow.setInput(innerGlow);
        return outerGlow;
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

    private Point2D moveInsideArena(Point2D currentPosition, Point2D movementDelta, double radius) {
        Point2D nextPosition = currentPosition;

        if (movementDelta.getX() != 0) {
            nextPosition = resolveArenaPosition(
                    new Point2D(nextPosition.getX() + movementDelta.getX(), nextPosition.getY()),
                    radius
            );
        }
        if (movementDelta.getY() != 0) {
            nextPosition = resolveArenaPosition(
                    new Point2D(nextPosition.getX(), nextPosition.getY() + movementDelta.getY()),
                    radius
            );
        }

        return resolveArenaPosition(nextPosition, radius);
    }

    private Point2D resolveArenaPosition(Point2D candidatePosition, double radius) {
        Point2D resolvedPosition = clampToBoard(candidatePosition, radius);

        for (int iteration = 0; iteration < 4; iteration++) {
            boolean adjusted = false;
            for (ArenaObstacle obstacle : getActiveMovementObstacles()) {
                Point2D collisionResolved = resolveCircleVsObstacle(resolvedPosition, radius, obstacle);
                if (collisionResolved.distance(resolvedPosition) > 0.001d) {
                    resolvedPosition = clampToBoard(collisionResolved, radius);
                    adjusted = true;
                }
            }
            if (!adjusted) {
                break;
            }
        }

        return resolvedPosition;
    }

    private List<ArenaObstacle> getActiveMovementObstacles() {
        List<ArenaObstacle> activeObstacles = new ArrayList<>(arenaObstacles);
        if (!canOpenDoor()) {
            activeObstacles.add(buildMainDoorObstacle());
        }
        return activeObstacles;
    }

    private ArenaObstacle buildMainDoorObstacle() {
        return new ArenaObstacle(
                DOOR_POSITION.getX() + 10,
                DOOR_POSITION.getY() + 6,
                DOOR_NODE_WIDTH - 20,
                DOOR_NODE_HEIGHT - 12,
                ArenaObstacleStyle.CLOSED_DOOR
        );
    }

    private Point2D resolveCircleVsObstacle(Point2D center, double radius, ArenaObstacle obstacle) {
        double closestX = Math.max(obstacle.left(), Math.min(center.getX(), obstacle.right()));
        double closestY = Math.max(obstacle.top(), Math.min(center.getY(), obstacle.bottom()));
        double deltaX = center.getX() - closestX;
        double deltaY = center.getY() - closestY;
        double distanceSquared = deltaX * deltaX + deltaY * deltaY;

        if (distanceSquared >= radius * radius) {
            return center;
        }

        if (distanceSquared > 0.0001d) {
            double distance = Math.sqrt(distanceSquared);
            double pushDistance = radius - distance;
            return new Point2D(
                    center.getX() + (deltaX / distance) * pushDistance,
                    center.getY() + (deltaY / distance) * pushDistance
            );
        }

        double distanceToLeft = Math.abs(center.getX() - obstacle.left());
        double distanceToRight = Math.abs(obstacle.right() - center.getX());
        double distanceToTop = Math.abs(center.getY() - obstacle.top());
        double distanceToBottom = Math.abs(obstacle.bottom() - center.getY());
        double minimumDistance = Math.min(Math.min(distanceToLeft, distanceToRight), Math.min(distanceToTop, distanceToBottom));

        if (minimumDistance == distanceToLeft) {
            return new Point2D(obstacle.left() - radius, center.getY());
        }
        if (minimumDistance == distanceToRight) {
            return new Point2D(obstacle.right() + radius, center.getY());
        }
        if (minimumDistance == distanceToTop) {
            return new Point2D(center.getX(), obstacle.top() - radius);
        }
        return new Point2D(center.getX(), obstacle.bottom() + radius);
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
            return 60;
        }
        if (enemy instanceof Slime) {
            return 34;
        }
        return 40;
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

    private Image loadOriginStoneDropSprite() {
        InputStream stream = GameViewController.class.getResourceAsStream(ORIGIN_STONE_DROP_SPRITE_PATH);
        if (stream == null) {
            throw new IllegalStateException("Sprite resource not found: " + ORIGIN_STONE_DROP_SPRITE_PATH);
        }

        return new Image(stream);
    }

    private Image loadPlayerSprite() {
        InputStream stream = GameViewController.class.getResourceAsStream(PLAYER_SPRITE_PATH);
        if (stream == null) {
            throw new IllegalStateException("Sprite resource not found: " + PLAYER_SPRITE_PATH);
        }

        Image sourceImage = new Image(stream);
        PixelReader pixelReader = sourceImage.getPixelReader();
        if (pixelReader == null) {
            return sourceImage;
        }

        int width = (int) Math.round(sourceImage.getWidth());
        int height = (int) Math.round(sourceImage.getHeight());
        WritableImage cleanedImage = new WritableImage(width, height);
        PixelWriter pixelWriter = cleanedImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
            }
        }

        boolean[] visited = new boolean[width * height];
        int[] queue = new int[width * height];
        int head = 0;
        int tail = 0;

        for (int x = 0; x < width; x++) {
            tail = enqueueTransparentBackgroundPixel(pixelReader, visited, queue, tail, width, height, x, 0);
            tail = enqueueTransparentBackgroundPixel(pixelReader, visited, queue, tail, width, height, x, height - 1);
        }
        for (int y = 0; y < height; y++) {
            tail = enqueueTransparentBackgroundPixel(pixelReader, visited, queue, tail, width, height, 0, y);
            tail = enqueueTransparentBackgroundPixel(pixelReader, visited, queue, tail, width, height, width - 1, y);
        }

        while (head < tail) {
            int index = queue[head++];
            int x = index % width;
            int y = index / width;
            pixelWriter.setColor(x, y, Color.TRANSPARENT);

            tail = enqueueTransparentBackgroundPixel(pixelReader, visited, queue, tail, width, height, x + 1, y);
            tail = enqueueTransparentBackgroundPixel(pixelReader, visited, queue, tail, width, height, x - 1, y);
            tail = enqueueTransparentBackgroundPixel(pixelReader, visited, queue, tail, width, height, x, y + 1);
            tail = enqueueTransparentBackgroundPixel(pixelReader, visited, queue, tail, width, height, x, y - 1);
        }

        return cleanedImage;
    }

    private int enqueueTransparentBackgroundPixel(
            PixelReader pixelReader,
            boolean[] visited,
            int[] queue,
            int tail,
            int width,
            int height,
            int x,
            int y
    ) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return tail;
        }

        int index = y * width + x;
        if (visited[index]) {
            return tail;
        }

        Color color = pixelReader.getColor(x, y);
        if (!isPlayerSpriteBackgroundColor(color)) {
            return tail;
        }

        visited[index] = true;
        queue[tail] = index;
        return tail + 1;
    }

    private boolean isPlayerSpriteBackgroundColor(Color color) {
        return color.getOpacity() > 0.98d
                && color.getRed() >= 0.92d
                && color.getGreen() >= 0.92d
                && color.getBlue() >= 0.92d;
    }

    private record BoardPalette(Color primaryTileColor, Color secondaryTileColor, Color borderColor) {
    }

    private enum ArenaObstacleStyle {
        TREE,
        FORTRESS_WALL,
        STONE_WALL,
        CLOSED_DOOR,
        RUBBLE,
        PILLAR
    }

    private record ArenaObstacle(
            double x,
            double y,
            double width,
            double height,
            ArenaObstacleStyle style
    ) {
        double left() {
            return x;
        }

        double right() {
            return x + width;
        }

        double top() {
            return y;
        }

        double bottom() {
            return y + height;
        }
    }

    private record EnemyVisual(
            StackPane node,
            Label hpChip,
            Circle rangeIndicator,
            double width,
            double height
    ) {
    }

    private record ProjectileEffect(
            Enemy targetEnemy,
            Point2D position,
            double speed,
            Color color,
            double radius
    ) {
        ProjectileEffect withPosition(Point2D nextPosition) {
            return new ProjectileEffect(targetEnemy, nextPosition, speed, color, radius);
        }
    }
}
