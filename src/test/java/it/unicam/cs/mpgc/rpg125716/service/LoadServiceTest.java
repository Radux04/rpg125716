package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import it.unicam.cs.mpgc.rpg125716.persistence.AchievementRepository;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.XmlSaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void loadServiceRebuildsPlayerAndCampaignFromSavedSlot() {
        XmlSaveRepository repository = new XmlSaveRepository(tempDir);
        SaveService saveService = new SaveService(repository);
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.json"))
        );
        LoadService loadService = new LoadService(saveService, achievementService);

        Player player = new Player("Hero", 60, 10, 5, 8);
        player.collectItem(new Potion());

        DemoCampaign campaign = new DemoCampaign();
        campaign.getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));
        campaign.advanceToNextLevel();
        campaign.getCurrentLevel().getEnemies().get(0).setHp(7);

        GameStateLog gameStateLog = GameStateLog.fromCurrentGame(2, player, campaign, List.of("Livello 1"));
        saveService.saveGame(gameStateLog, SaveSlot.SLOT_2);

        LoadedGameSession loadedGameSession = loadService.requireLoadedSession(SaveSlot.SLOT_2);

        assertEquals(SaveSlot.SLOT_2, loadedGameSession.getSaveSlot());
        assertEquals("Hero", loadedGameSession.getPlayer().getName());
        assertTrue(loadedGameSession.getPlayer().getInventory().containsItem(new Potion()));
        assertEquals(2, loadedGameSession.getCampaign().getCurrentLevel().getNumber());
        assertEquals(35, loadedGameSession.getCampaign().getCurrentLevel().getEnemies().get(0).getHp());
        assertEquals(45, loadedGameSession.getCampaign().getCurrentLevel().getEnemies().get(1).getHp());
        assertTrue(loadedGameSession.getCampaign().getLevel(1).isCompleted());
        assertEquals(List.of("Livello 1"), loadedGameSession.getCompletedLevels());
    }

    @Test
    void loadServiceKeepsCompletedCurrentLevelWhenSaveWasCreatedAfterCompletion() {
        XmlSaveRepository repository = new XmlSaveRepository(tempDir);
        SaveService saveService = new SaveService(repository);
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.json"))
        );
        LoadService loadService = new LoadService(saveService, achievementService);

        Player player = new Player("Hero", 60, 10, 5, 8);
        DemoCampaign campaign = new DemoCampaign();
        campaign.getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));

        saveService.saveGame(
                GameStateLog.fromCurrentGame(1, player, campaign, List.of("Livello 1")),
                SaveSlot.SLOT_1
        );

        LoadedGameSession loadedGameSession = loadService.requireLoadedSession(SaveSlot.SLOT_1);

        assertEquals(1, loadedGameSession.getCampaign().getCurrentLevel().getNumber());
        assertTrue(loadedGameSession.getCampaign().getCurrentLevel().isCompleted());
    }

    @Test
    void loadServiceSynchronizesGlobalAchievementsIntoTheLoadedPlayer() {
        XmlSaveRepository repository = new XmlSaveRepository(tempDir.resolve("saves"));
        SaveService saveService = new SaveService(repository);
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.json"))
        );
        LoadService loadService = new LoadService(saveService, achievementService);

        achievementService.unlockAchievement(AchievementType.FIRST_KILL);
        saveService.saveGame(
                GameStateLog.fromCurrentGame(1, new Player("Hero", 60, 10, 5, 8), new DemoCampaign(), List.of()),
                SaveSlot.SLOT_1
        );

        LoadedGameSession loadedGameSession = loadService.requireLoadedSession(SaveSlot.SLOT_1);

        assertTrue(loadedGameSession.getPlayer().hasAchievement(AchievementType.FIRST_KILL));
    }
}
