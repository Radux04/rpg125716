package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.controller.GameController;
import it.unicam.cs.mpgc.rpg125716.model.character.ElementType;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;
import it.unicam.cs.mpgc.rpg125716.model.level.LevelRewardChoice;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlotInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class GameService {
    private static final String DEFAULT_PLAYER_NAME = "Hero";
    private static final int DEFAULT_PLAYER_MAX_HP = 60;
    private static final int DEFAULT_PLAYER_ATTACK = 10;
    private static final int DEFAULT_PLAYER_DEFENSE = 5;
    private static final int DEFAULT_PLAYER_SPEED = 8;

    private final GameController gameController;
    private final CombatService combatService;
    private boolean currentLevelStarted;

    public GameService() {
        this(new GameController());
    }

    public GameService(GameController gameController) {
        this(gameController, gameController.createCombatService());
    }

    public GameService(GameController gameController, CombatService combatService) {
        this.gameController = Objects.requireNonNull(gameController, "gameController cannot be null");
        this.combatService = Objects.requireNonNull(combatService, "combatService cannot be null");
    }

    public CurrentGameState newGame() {
        return newGame(DEFAULT_PLAYER_NAME);
    }

    public CurrentGameState newGame(String playerName) {
        LoadedGameSession session = new LoadedGameSession(
                null,
                createPlayer(playerName),
                new DemoCampaign(),
                List.of(),
                null,
                LocalDateTime.now()
        );

        gameController.openSession(session);
        currentLevelStarted = false;
        return getCurrentGameState();
    }

    public CurrentGameState loadGame(SaveSlot saveSlot) {
        gameController.loadGame(Objects.requireNonNull(saveSlot, "saveSlot cannot be null"))
                .orElseThrow(() -> new IllegalStateException("no save present in slot " + saveSlot));
        currentLevelStarted = false;
        return getCurrentGameState();
    }

    public CurrentGameState saveCurrentGame(SaveSlot saveSlot) {
        gameController.saveCurrentGame(Objects.requireNonNull(saveSlot, "saveSlot cannot be null"));
        return getCurrentGameState();
    }

    public CurrentGameState getCurrentGameState() {
        return CurrentGameState.fromSession(gameController.requireCurrentSession(), currentLevelStarted);
    }

    public CurrentGameState startLevel() {
        gameController.requireCurrentSession();
        currentLevelStarted = true;
        return getCurrentGameState();
    }

    public CurrentGameState completeCurrentLevel() {
        LoadedGameSession session = gameController.requireCurrentSession();
        DemoLevel currentLevel = session.getCampaign().getCurrentLevel();

        if (!currentLevelStarted) {
            throw new IllegalStateException("the current level has not been started yet");
        }

        if (!currentLevel.isCompleted()) {
            throw new IllegalStateException("the current level is not completed yet");
        }

        resolvePendingLevelCompletionActions(session, currentLevel);
        gameController.publishCurrentLevelCompleted();

        if (session.getCampaign().hasNextLevel()) {
            rewardPlayerForLevelAdvance(session.getPlayer());
            session.getCampaign().advanceToNextLevel();
        }

        currentLevelStarted = false;
        refreshCurrentSession();
        return getCurrentGameState();
    }

    public CurrentGameState attuneCurrentPlayerToOriginStone(ElementType elementType) {
        gameController.attuneCurrentPlayerToOriginStone(Objects.requireNonNull(elementType, "elementType cannot be null"));
        refreshCurrentSession();
        return getCurrentGameState();
    }

    public CurrentGameState chooseCurrentLevelReward(LevelRewardChoice rewardChoice) {
        gameController.chooseCurrentLevelReward(Objects.requireNonNull(rewardChoice, "rewardChoice cannot be null"));
        refreshCurrentSession();
        return getCurrentGameState();
    }

    public CurrentGameState claimCurrentLevelCompletionDrop() {
        ensureLevelStarted();
        gameController.claimCurrentLevelCompletionDrop();
        refreshCurrentSession();
        return getCurrentGameState();
    }

    public CombatTurnResult attackCurrentLevelEnemy(Enemy enemy) {
        return resolveCurrentLevelAttack(enemy, 0, true);
    }

    public CombatTurnResult attackCurrentLevelEnemyWithoutCounterAttack(Enemy enemy) {
        return resolveCurrentLevelAttack(enemy, 0, false);
    }

    public CombatTurnResult attackCurrentLevelEnemyWithBonusDamageWithoutCounterAttack(Enemy enemy, int bonusDamage) {
        return resolveCurrentLevelAttack(enemy, bonusDamage, false);
    }

    private CombatTurnResult resolveCurrentLevelAttack(Enemy enemy, int bonusDamage, boolean enemyCanCounterAttack) {
        ensureLevelStarted();
        Enemy enemyToAttack = requireAttackableEnemy(enemy);
        Player player = gameController.requireCurrentSession().getPlayer();

        combatService.resetCombat();
        CombatResult playerActionResult = combatService.playerAttack(player, enemyToAttack, bonusDamage);
        CombatResult enemyActionResult = null;

        if (enemyCanCounterAttack && enemyToAttack.isAlive() && player.isAlive()) {
            enemyActionResult = combatService.enemyAttack(enemyToAttack, player);
        }

        return new CombatTurnResult(getCurrentGameState(), playerActionResult, enemyActionResult);
    }

    public CombatResult useItem(Item item) {
        ensureLevelStarted();
        Player player = gameController.requireCurrentSession().getPlayer();

        combatService.resetCombat();
        return combatService.useItem(player, Objects.requireNonNull(item, "item cannot be null"));
    }

    public List<SaveSlotInfo> listSaveSlots() {
        return gameController.listSaveSlots();
    }

    public boolean deleteSave(SaveSlot saveSlot) {
        return gameController.deleteSave(Objects.requireNonNull(saveSlot, "saveSlot cannot be null"));
    }

    private void resolvePendingLevelCompletionActions(LoadedGameSession session, DemoLevel currentLevel) {
        if (currentLevel.hasCompletionDrop() && !currentLevel.isCompletionDropClaimed()) {
            gameController.claimCurrentLevelCompletionDrop();
        }

        if (currentLevel.isUnlocksElementChoice() && session.getPlayer().getElementType() == null) {
            refreshCurrentSession();
            throw new IllegalStateException("the player must choose an element before completing this level");
        }

        if (currentLevel.hasRewardChoices() && !currentLevel.isRewardClaimed()) {
            refreshCurrentSession();
            throw new IllegalStateException("the player must choose a reward before completing this level");
        }
    }

    private void refreshCurrentSession() {
        LoadedGameSession session = gameController.requireCurrentSession();
        gameController.openSession(
                new LoadedGameSession(
                        session.getSaveSlot(),
                        session.getPlayer(),
                        session.getCampaign(),
                        session.getCampaign().getLevels().stream()
                                .filter(DemoLevel::isCompleted)
                                .map(DemoLevel::getName)
                                .toList(),
                        session.getSourceSave(),
                        session.getLoadedAt()
                )
        );
    }

    private void rewardPlayerForLevelAdvance(Player player) {
        Objects.requireNonNull(player, "player cannot be null");
        player.levelUp();
    }

    private void ensureLevelStarted() {
        gameController.requireCurrentSession();
        if (!currentLevelStarted) {
            throw new IllegalStateException("the current level has not been started yet");
        }
    }

    private Enemy requireAttackableEnemy(Enemy enemy) {
        Enemy enemyToAttack = Objects.requireNonNull(enemy, "enemy cannot be null");
        DemoLevel currentLevel = gameController.requireCurrentSession().getCampaign().getCurrentLevel();

        if (!currentLevel.getEnemies().contains(enemyToAttack)) {
            throw new IllegalArgumentException("enemy does not belong to the current level");
        }

        if (!enemyToAttack.isAlive()) {
            throw new IllegalStateException("enemy is already defeated");
        }

        return enemyToAttack;
    }

    private static Player createPlayer(String playerName) {
        return new Player(
                normalizePlayerName(playerName),
                DEFAULT_PLAYER_MAX_HP,
                DEFAULT_PLAYER_ATTACK,
                DEFAULT_PLAYER_DEFENSE,
                DEFAULT_PLAYER_SPEED
        );
    }

    private static String normalizePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return DEFAULT_PLAYER_NAME;
        }

        return playerName.trim();
    }
}
