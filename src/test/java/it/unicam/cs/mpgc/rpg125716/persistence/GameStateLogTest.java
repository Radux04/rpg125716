package it.unicam.cs.mpgc.rpg125716.persistence;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateLogTest {

    @Test
    void gameStateLogStoresAnImmutableSnapshotOfTheSaveData() {
        Player player = new Player("Hero", 60, 10, 5, 8);
        player.collectItem(new Potion());
        List<String> completedLevels = new ArrayList<>(List.of("Livello 1"));
        LocalDateTime savedAt = LocalDateTime.of(2026, 6, 9, 12, 30);

        GameStateLog gameStateLog = new GameStateLog(
                1,
                player,
                2,
                completedLevels,
                player.getInventory(),
                LevelState.restartSnapshotForLevel(2),
                savedAt
        );

        player.setName("Changed Hero");
        player.collectItem(new Potion());
        completedLevels.add("Livello 2");

        assertEquals("Hero", gameStateLog.getPlayer().getName());
        assertEquals(1, gameStateLog.getInventory().getItems().size());
        assertEquals(List.of("Livello 1"), gameStateLog.getCompletedLevels());
        assertEquals(savedAt, gameStateLog.getLastSavedAt());
        assertEquals(1, gameStateLog.getSlotId());
        assertEquals(GameStateLog.CURRENT_SAVE_VERSION, gameStateLog.getSaveVersion());
        assertEquals(2, gameStateLog.getCurrentLevelState().getLevelNumber());
        assertNotSame(player, gameStateLog.getPlayer());
        assertNotSame(player.getInventory(), gameStateLog.getInventory());
    }

    @Test
    void fromCurrentGameRestartsTheCurrentLevelFromScratchOnLoad() {
        Player player = new Player("Hero", 60, 10, 5, 8);
        DemoCampaign campaign = new DemoCampaign();

        campaign.getCurrentLevel().getEnemies().forEach(enemy -> enemy.setHp(0));
        campaign.advanceToNextLevel();
        campaign.getCurrentLevel().getEnemies().get(0).setHp(7);

        GameStateLog gameStateLog = GameStateLog.fromCurrentGame(2, player, campaign, List.of("Livello 1"));

        assertEquals(2, gameStateLog.getSlotId());
        assertEquals(2, gameStateLog.getCurrentLevel());
        assertTrue(gameStateLog.getCurrentLevelState().isRestartFromBeginningOnLoad());
        assertEquals(35, gameStateLog.getCurrentLevelState().getEnemyStates().get(0).getStartingHp());
        assertEquals(45, gameStateLog.getCurrentLevelState().getEnemyStates().get(1).getStartingHp());
    }

    @Test
    void currentLevelMustBePositive() {
        Player player = new Player("Hero", 60, 10, 5, 8);

        assertThrows(
                IllegalArgumentException.class,
                () -> new GameStateLog(
                        1,
                        player,
                        0,
                        List.of(),
                        player.getInventory(),
                        LevelState.restartSnapshotForLevel(1),
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void slotIdMustBeBetweenOneAndThree() {
        Player player = new Player("Hero", 60, 10, 5, 8);

        assertThrows(
                IllegalArgumentException.class,
                () -> new GameStateLog(
                        0,
                        player,
                        1,
                        List.of(),
                        player.getInventory(),
                        LevelState.restartSnapshotForLevel(1),
                        LocalDateTime.now()
                )
        );
    }
}
