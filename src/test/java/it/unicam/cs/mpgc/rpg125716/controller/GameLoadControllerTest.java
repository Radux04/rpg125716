package it.unicam.cs.mpgc.rpg125716.controller;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.XmlSaveRepository;
import it.unicam.cs.mpgc.rpg125716.service.LoadService;
import it.unicam.cs.mpgc.rpg125716.service.SaveService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameLoadControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void controllerKeepsTrackOfTheCurrentLoadedSession() {
        XmlSaveRepository repository = new XmlSaveRepository(tempDir);
        SaveService saveService = new SaveService(repository);
        LoadService loadService = new LoadService(saveService);
        GameLoadController gameLoadController = new GameLoadController(loadService);

        Player player = new Player("Hero", 60, 10, 5, 8);
        saveService.saveGame(
                GameStateLog.fromCurrentGame(3, player, new DemoCampaign(), List.of()),
                SaveSlot.SLOT_3
        );

        var loaded = gameLoadController.loadSelectedSlot(SaveSlot.SLOT_3);

        assertTrue(loaded.isPresent());
        assertEquals(SaveSlot.SLOT_3, gameLoadController.requireCurrentSession().getSaveSlot());
        assertEquals("Hero", gameLoadController.requireCurrentSession().getPlayer().getName());
    }
}
