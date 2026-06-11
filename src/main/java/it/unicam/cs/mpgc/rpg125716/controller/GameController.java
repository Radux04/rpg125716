package it.unicam.cs.mpgc.rpg125716.controller;

import it.unicam.cs.mpgc.rpg125716.event.GameEventDispatcher;
import it.unicam.cs.mpgc.rpg125716.event.ItemCollectedEvent;
import it.unicam.cs.mpgc.rpg125716.event.LevelCompletedEvent;
import it.unicam.cs.mpgc.rpg125716.event.OriginStoneAttunedEvent;
import it.unicam.cs.mpgc.rpg125716.event.PlayerDiedEvent;
import it.unicam.cs.mpgc.rpg125716.model.character.ElementType;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoLevel;
import it.unicam.cs.mpgc.rpg125716.model.level.LevelRewardChoice;
import it.unicam.cs.mpgc.rpg125716.model.progression.Achievement;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlotInfo;
import it.unicam.cs.mpgc.rpg125716.service.AchievementService;
import it.unicam.cs.mpgc.rpg125716.service.CombatService;
import it.unicam.cs.mpgc.rpg125716.service.LoadService;
import it.unicam.cs.mpgc.rpg125716.service.LoadedGameSession;
import it.unicam.cs.mpgc.rpg125716.service.SaveService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GameController {
    private final SaveService saveService;
    private final LoadService loadService;
    private final AchievementService achievementService;
    private final GameEventDispatcher gameEventDispatcher;
    private LoadedGameSession currentSession;

    public GameController() {
        this(new SaveService(), new AchievementService());
    }

    public GameController(SaveService saveService, AchievementService achievementService) {
        this(
                saveService,
                new LoadService(saveService, achievementService),
                achievementService,
                new GameEventDispatcher().registerListener(achievementService)
        );
    }

    public GameController(SaveService saveService, LoadService loadService) {
        this(saveService, loadService, new AchievementService());
    }

    public GameController(SaveService saveService, LoadService loadService, AchievementService achievementService) {
        this(
                saveService,
                loadService,
                achievementService,
                new GameEventDispatcher().registerListener(achievementService)
        );
    }

    public GameController(
            SaveService saveService,
            LoadService loadService,
            AchievementService achievementService,
            GameEventDispatcher gameEventDispatcher
    ) {
        this.saveService = Objects.requireNonNull(saveService, "saveService cannot be null");
        this.loadService = Objects.requireNonNull(loadService, "loadService cannot be null");
        this.achievementService = Objects.requireNonNull(achievementService, "achievementService cannot be null");
        this.gameEventDispatcher = Objects.requireNonNull(gameEventDispatcher, "gameEventDispatcher cannot be null");
    }

    public List<SaveSlotInfo> listSaveSlots() {
        return saveService.listSlots();
    }

    public Optional<LoadedGameSession> loadGame(SaveSlot saveSlot) {
        Optional<LoadedGameSession> loadedGameSession = loadService.loadFromSlot(saveSlot);
        currentSession = loadedGameSession.orElse(null);
        return loadedGameSession;
    }

    public LoadedGameSession openSession(LoadedGameSession loadedGameSession) {
        currentSession = Objects.requireNonNull(loadedGameSession, "loadedGameSession cannot be null");
        return currentSession;
    }

    public GameStateLog saveCurrentGame(SaveSlot saveSlot) {
        Objects.requireNonNull(saveSlot, "saveSlot cannot be null");

        LoadedGameSession session = requireCurrentSession();
        List<String> completedLevels = deriveCompletedLevels(session.getCampaign());
        GameStateLog gameStateLog = GameStateLog.fromCurrentGame(
                saveSlot.getSlotId(),
                session.getPlayer(),
                session.getCampaign(),
                completedLevels
        );

        saveService.saveGame(gameStateLog, saveSlot);

        currentSession = new LoadedGameSession(
                saveSlot,
                session.getPlayer(),
                session.getCampaign(),
                completedLevels,
                GameStateLog.copyOf(gameStateLog),
                LocalDateTime.now()
        );

        return GameStateLog.copyOf(gameStateLog);
    }

    public void attuneCurrentPlayerToOriginStone(ElementType elementType) {
        Objects.requireNonNull(elementType, "elementType cannot be null");

        LoadedGameSession session = requireCurrentSession();
        session.getPlayer().attuneToOriginStone(elementType);
        gameEventDispatcher.dispatch(new OriginStoneAttunedEvent(session.getPlayer(), elementType));
    }

    public Item collectItemForCurrentPlayer(Item item) {
        Objects.requireNonNull(item, "item cannot be null");

        LoadedGameSession session = requireCurrentSession();
        Player player = session.getPlayer();
        player.collectItem(item);
        gameEventDispatcher.dispatch(new ItemCollectedEvent(player, item, player.getInventory().getTotalItemCount()));
        return item;
    }

    public Item claimCurrentLevelCompletionDrop() {
        LoadedGameSession session = requireCurrentSession();
        DemoLevel currentLevel = session.getCampaign().getCurrentLevel();
        Item item = currentLevel.grantCompletionDrop(session.getPlayer());
        gameEventDispatcher.dispatch(new ItemCollectedEvent(session.getPlayer(), item, session.getPlayer().getInventory().getTotalItemCount()));
        return item;
    }

    public Item chooseCurrentLevelReward(LevelRewardChoice rewardChoice) {
        Objects.requireNonNull(rewardChoice, "rewardChoice cannot be null");

        LoadedGameSession session = requireCurrentSession();
        DemoLevel currentLevel = session.getCampaign().getCurrentLevel();
        Item item = currentLevel.chooseReward(session.getPlayer(), rewardChoice);
        gameEventDispatcher.dispatch(new ItemCollectedEvent(session.getPlayer(), item, session.getPlayer().getInventory().getTotalItemCount()));
        return item;
    }

    public void publishCurrentLevelCompleted() {
        LoadedGameSession session = requireCurrentSession();
        gameEventDispatcher.dispatch(new LevelCompletedEvent(session.getPlayer(), session.getCampaign().getCurrentLevel()));
    }

    public void publishCurrentPlayerDied() {
        gameEventDispatcher.dispatch(new PlayerDiedEvent(requireCurrentSession().getPlayer()));
    }

    public boolean deleteSave(SaveSlot saveSlot) {
        return saveService.deleteSave(saveSlot);
    }

    public CombatService createCombatService() {
        return new CombatService(gameEventDispatcher);
    }

    public List<Achievement> getAllAchievements() {
        return achievementService.getAllAchievements();
    }

    public Optional<LoadedGameSession> getCurrentSession() {
        return Optional.ofNullable(currentSession);
    }

    public LoadedGameSession requireCurrentSession() {
        if (currentSession == null) {
            throw new IllegalStateException("no game session is currently loaded");
        }

        return currentSession;
    }

    public void clearCurrentSession() {
        currentSession = null;
    }

    private List<String> deriveCompletedLevels(DemoCampaign campaign) {
        return campaign.getLevels().stream()
                .filter(DemoLevel::isCompleted)
                .map(DemoLevel::getName)
                .toList();
    }
}
