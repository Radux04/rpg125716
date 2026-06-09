package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlotInfo;
import it.unicam.cs.mpgc.rpg125716.persistence.XmlSaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void serviceListsAllThreeSlotsWithOccupancyMetadata() {
        SaveService saveService = new SaveService(new XmlSaveRepository(tempDir));
        Player player = new Player("Hero", 60, 10, 5, 8);
        GameStateLog gameStateLog = GameStateLog.fromCurrentGame(1, player, new DemoCampaign(), List.of());

        saveService.saveGame(gameStateLog, SaveSlot.SLOT_1);

        List<SaveSlotInfo> slots = saveService.getAvailableSlots();

        assertEquals(3, slots.size());
        assertTrue(slots.get(0).isOccupied());
        assertEquals("Hero", slots.get(0).getPlayerName());
        assertFalse(slots.get(1).isOccupied());
        assertFalse(slots.get(2).isOccupied());
    }

    @Test
    void serviceDeletesSaveAndMakesSlotEmptyAgain() {
        SaveService saveService = new SaveService(new XmlSaveRepository(tempDir));
        Player player = new Player("Hero", 60, 10, 5, 8);
        GameStateLog gameStateLog = GameStateLog.fromCurrentGame(1, player, new DemoCampaign(), List.of());

        saveService.saveGame(gameStateLog, SaveSlot.SLOT_3);
        assertTrue(saveService.loadGame(SaveSlot.SLOT_3).isPresent());

        assertTrue(saveService.deleteSave(SaveSlot.SLOT_3));
        assertTrue(saveService.loadGame(SaveSlot.SLOT_3).isEmpty());
    }
}
