package it.unicam.cs.mpgc.rpg125716.persistence;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.BossSword;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlSaveRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void repositorySavesAndLoadsXmlRoundTrip() throws Exception {
        XmlSaveRepository saveRepository = new XmlSaveRepository(tempDir);
        Player player = new Player("Hero", 60, 10, 5, 8);
        player.collectItem(new Potion());
        player.collectItem(new BossSword());
        DemoCampaign campaign = new DemoCampaign();

        GameStateLog gameStateLog = GameStateLog.fromCurrentGame(1, player, campaign, List.of());

        saveRepository.save(gameStateLog, SaveSlot.SLOT_2);

        Path expectedFile = tempDir.resolve("slot-2.xml");
        assertTrue(Files.exists(expectedFile));

        GameStateLog loadedState = saveRepository.load(SaveSlot.SLOT_2).orElseThrow();

        assertEquals(2, loadedState.getSlotId());
        assertEquals("Hero", loadedState.getPlayer().getName());
        assertTrue(loadedState.getInventory().containsItem(new Potion()));
        assertTrue(loadedState.getInventory().containsItem(new BossSword()));
        assertTrue(loadedState.getPlayer().getInventory().containsItem(new BossSword()));
    }

    @Test
    void repositoryReturnsEmptyForMissingSlotAndDeletesExistingSave() {
        XmlSaveRepository saveRepository = new XmlSaveRepository(tempDir);

        assertTrue(saveRepository.load(SaveSlot.SLOT_1).isEmpty());

        Player player = new Player("Hero", 60, 10, 5, 8);
        GameStateLog gameStateLog = new GameStateLog(
                1,
                player,
                1,
                List.of(),
                player.getInventory(),
                LevelState.restartSnapshotForLevel(1),
                java.time.LocalDateTime.now()
        );

        saveRepository.save(gameStateLog, SaveSlot.SLOT_1);
        assertTrue(saveRepository.exists(SaveSlot.SLOT_1));
        assertTrue(saveRepository.delete(SaveSlot.SLOT_1));
        assertFalse(saveRepository.exists(SaveSlot.SLOT_1));
    }
}
