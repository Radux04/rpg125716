package it.unicam.cs.mpgc.rpg125716.frontend.controller.menu;

import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlotInfo;
import it.unicam.cs.mpgc.rpg125716.service.CurrentGameState;
import it.unicam.cs.mpgc.rpg125716.service.GameService;

import java.util.List;
import java.util.Objects;

public class LoadSlotsController {
    private final GameService gameService;

    public LoadSlotsController(GameService gameService) {
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
    }

    public List<SaveSlotInfo> listSaveSlots() {
        return gameService.listSaveSlots();
    }

    public CurrentGameState loadGame(SaveSlot saveSlot) {
        return gameService.loadGame(Objects.requireNonNull(saveSlot, "saveSlot cannot be null"));
    }
}
