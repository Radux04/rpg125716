package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.controller.GameController;
import it.unicam.cs.mpgc.rpg125716.model.character.ElementType;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
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
    private boolean currentLevelStarted;

    public GameService() {
        this(new GameController());
    }

    public GameService(GameController gameController) {
        this.gameController = Objects.requireNonNull(gameController, "gameController cannot be null");
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
            session.getCampaign().advanceToNextLevel();
        }

        currentLevelStarted = false;
        refreshCurrentSession();
        return getCurrentGameState();
    }

    public void attuneCurrentPlayerToOriginStone(ElementType elementType) {
        gameController.attuneCurrentPlayerToOriginStone(Objects.requireNonNull(elementType, "elementType cannot be null"));
        refreshCurrentSession();
    }

    public void chooseCurrentLevelReward(LevelRewardChoice rewardChoice) {
        gameController.chooseCurrentLevelReward(Objects.requireNonNull(rewardChoice, "rewardChoice cannot be null"));
        refreshCurrentSession();
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
