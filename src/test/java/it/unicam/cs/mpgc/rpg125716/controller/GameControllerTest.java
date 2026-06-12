package it.unicam.cs.mpgc.rpg125716.controller;

import it.unicam.cs.mpgc.rpg125716.model.character.ElementType;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.LevelRewardChoice;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import it.unicam.cs.mpgc.rpg125716.persistence.AchievementRepository;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.XmlSaveRepository;
import it.unicam.cs.mpgc.rpg125716.service.AchievementService;
import it.unicam.cs.mpgc.rpg125716.service.LoadService;
import it.unicam.cs.mpgc.rpg125716.service.SaveService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void gameControllerLoadsASlotAndSavesTheUpdatedRuntimeSession() {
        XmlSaveRepository repository = new XmlSaveRepository(tempDir);
        SaveService saveService = new SaveService(repository);
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.xml"))
        );
        LoadService loadService = new LoadService(saveService, achievementService);
        GameController gameController = new GameController(saveService, loadService, achievementService);

        Player player = new Player("Hero", 60, 10, 5, 8);
        player.collectItem(new Potion());

        DemoCampaign campaign = new DemoCampaign();
        campaign.getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));
        campaign.advanceToNextLevel();

        saveService.saveGame(
                GameStateLog.fromCurrentGame(1, player, campaign, List.of("Livello 1")),
                SaveSlot.SLOT_1
        );

        var loadedSession = gameController.loadGame(SaveSlot.SLOT_1).orElseThrow();
        loadedSession.getPlayer().collectItem(new Potion());
        loadedSession.getCampaign().getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));

        GameStateLog savedState = gameController.saveCurrentGame(SaveSlot.SLOT_2);

        assertEquals(2, savedState.getSlotId());
        assertEquals(SaveSlot.SLOT_2, gameController.requireCurrentSession().getSaveSlot());
        assertTrue(savedState.getCompletedLevels().contains("Livello 1 - Tutorial"));
        assertTrue(savedState.getCompletedLevels().contains("Livello 2 - Inseguimento"));
        assertTrue(gameController.loadGame(SaveSlot.SLOT_2).isPresent());
        assertTrue(gameController.requireCurrentSession().getPlayer().getInventory().containsItem(new Potion()));
        assertTrue(gameController.requireCurrentSession().getCampaign().getCurrentLevel().isCompleted());
    }

    @Test
    void attuningToTheOriginStoneUnlocksTheGlobalAchievement() {
        XmlSaveRepository repository = new XmlSaveRepository(tempDir.resolve("saves"));
        SaveService saveService = new SaveService(repository);
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.xml"))
        );
        LoadService loadService = new LoadService(saveService, achievementService);
        GameController gameController = new GameController(saveService, loadService, achievementService);

        Player player = new Player("Hero", 60, 10, 5, 8);
        player.collectItem(new OriginStone());
        saveService.saveGame(
                GameStateLog.fromCurrentGame(1, player, new DemoCampaign(), List.of()),
                SaveSlot.SLOT_1
        );

        gameController.loadGame(SaveSlot.SLOT_1).orElseThrow();
        gameController.attuneCurrentPlayerToOriginStone(ElementType.WIND);

        assertTrue(gameController.requireCurrentSession().getPlayer().hasAchievement(AchievementType.ORIGIN_STONE));
        assertTrue(achievementService.isUnlocked(AchievementType.ORIGIN_STONE));
    }

    @Test
    void collectingTenItemsThroughTheControllerUnlocksCollector() {
        XmlSaveRepository repository = new XmlSaveRepository(tempDir.resolve("saves"));
        SaveService saveService = new SaveService(repository);
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.xml"))
        );
        LoadService loadService = new LoadService(saveService, achievementService);
        GameController gameController = new GameController(saveService, loadService, achievementService);

        saveService.saveGame(
                GameStateLog.fromCurrentGame(1, new Player("Hero", 60, 10, 5, 8), new DemoCampaign(), List.of()),
                SaveSlot.SLOT_1
        );

        gameController.loadGame(SaveSlot.SLOT_1).orElseThrow();
        for (int i = 0; i < AchievementService.COLLECTOR_TARGET; i++) {
            gameController.collectItemForCurrentPlayer(new Potion());
        }

        assertTrue(gameController.requireCurrentSession().getPlayer().hasAchievement(AchievementType.COLLECTOR));
        assertTrue(achievementService.isUnlocked(AchievementType.COLLECTOR));
    }

    @Test
    void choosingLevelRewardThroughTheControllerCollectsTheItem() {
        XmlSaveRepository repository = new XmlSaveRepository(tempDir.resolve("saves"));
        SaveService saveService = new SaveService(repository);
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.xml"))
        );
        LoadService loadService = new LoadService(saveService, achievementService);
        GameController gameController = new GameController(saveService, loadService, achievementService);

        DemoCampaign campaign = new DemoCampaign();
        campaign.getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));
        campaign.advanceToNextLevel();
        campaign.getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));

        saveService.saveGame(
                GameStateLog.fromCurrentGame(1, new Player("Hero", 60, 10, 5, 8), campaign, List.of("Livello 1 - Tutorial")),
                SaveSlot.SLOT_1
        );

        gameController.loadGame(SaveSlot.SLOT_1).orElseThrow();
        var reward = gameController.chooseCurrentLevelReward(LevelRewardChoice.HEALING_POTION);

        assertTrue(gameController.requireCurrentSession().getPlayer().getInventory().containsItem(reward));
    }
}
