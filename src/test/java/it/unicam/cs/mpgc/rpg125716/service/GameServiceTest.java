package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.controller.GameController;
import it.unicam.cs.mpgc.rpg125716.model.character.ElementType;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.level.LevelRewardChoice;
import it.unicam.cs.mpgc.rpg125716.persistence.AchievementRepository;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.XmlSaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void newGameCreatesADefaultRuntimeSessionThatCanBeSavedAndLoaded() {
        GameService gameService = createGameService();

        CurrentGameState newGameState = gameService.newGame();
        assertEquals("Hero", newGameState.getPlayer().getName());
        assertEquals(1, newGameState.getCurrentLevel().getNumber());
        assertFalse(newGameState.isCurrentLevelStarted());

        CurrentGameState savedState = gameService.saveCurrentGame(SaveSlot.SLOT_1);
        assertEquals(SaveSlot.SLOT_1, savedState.getSaveSlot());

        CurrentGameState loadedState = gameService.loadGame(SaveSlot.SLOT_1);
        assertEquals(SaveSlot.SLOT_1, loadedState.getSaveSlot());
        assertEquals(1, loadedState.getCurrentLevel().getNumber());
    }

    @Test
    void tutorialLevelRequiresElementSelectionBeforeAdvancing() {
        GameService gameService = createGameService();
        gameService.newGame();
        gameService.startLevel();
        gameService.getCurrentGameState().getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));

        IllegalStateException exception = assertThrows(IllegalStateException.class, gameService::completeCurrentLevel);

        assertEquals("the player must choose an element before completing this level", exception.getMessage());
        assertTrue(gameService.getCurrentGameState().getPlayer().getInventory().containsItem(new OriginStone()));

        gameService.attuneCurrentPlayerToOriginStone(ElementType.FIRE);
        CurrentGameState advancedState = gameService.completeCurrentLevel();

        assertEquals(2, advancedState.getCurrentLevel().getNumber());
        assertTrue(advancedState.getCompletedLevels().contains("Livello 1 - Tutorial"));
        assertFalse(advancedState.isCurrentLevelStarted());
    }

    @Test
    void levelTwoRequiresRewardSelectionBeforeAdvancing() {
        GameService gameService = createGameService();
        gameService.newGame();
        gameService.startLevel();
        gameService.getCurrentGameState().getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));
        assertThrows(IllegalStateException.class, gameService::completeCurrentLevel);
        gameService.attuneCurrentPlayerToOriginStone(ElementType.WIND);
        gameService.completeCurrentLevel();

        gameService.startLevel();
        gameService.getCurrentGameState().getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));

        IllegalStateException exception = assertThrows(IllegalStateException.class, gameService::completeCurrentLevel);
        assertEquals("the player must choose a reward before completing this level", exception.getMessage());

        gameService.chooseCurrentLevelReward(LevelRewardChoice.HEALING_POTION);
        CurrentGameState advancedState = gameService.completeCurrentLevel();

        assertEquals(3, advancedState.getCurrentLevel().getNumber());
        assertTrue(advancedState.getCompletedLevels().contains("Livello 2 - Inseguimento"));
        assertTrue(advancedState.getPlayer().getInventory().containsItem(new Potion("Pozione curativa", "Permette al player di recuperare punti vita.", 25)));
    }

    @Test
    void loadGameRestoresAnExistingSaveThroughTheFacade() {
        GameService gameService = createGameService();
        Player player = new Player("Loaded Hero", 60, 10, 5, 8);
        DemoCampaign campaign = new DemoCampaign();

        createSaveService().saveGame(
                GameStateLog.fromCurrentGame(2, player, campaign, List.of()),
                SaveSlot.SLOT_2
        );

        CurrentGameState loadedState = gameService.loadGame(SaveSlot.SLOT_2);

        assertEquals("Loaded Hero", loadedState.getPlayer().getName());
        assertEquals(SaveSlot.SLOT_2, loadedState.getSaveSlot());
    }

    private GameService createGameService() {
        SaveService saveService = createSaveService();
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.json"))
        );
        LoadService loadService = new LoadService(saveService, achievementService);
        GameController gameController = new GameController(saveService, loadService, achievementService);
        return new GameService(gameController);
    }

    private SaveService createSaveService() {
        return new SaveService(new XmlSaveRepository(tempDir.resolve("saves")));
    }
}
