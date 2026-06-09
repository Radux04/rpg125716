package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlotInfo;
import it.unicam.cs.mpgc.rpg125716.persistence.XmlSaveRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SaveService {
    private final SaveRepository saveRepository;

    public SaveService() {
        this(new XmlSaveRepository());
    }

    public SaveService(SaveRepository saveRepository) {
        this.saveRepository = Objects.requireNonNull(saveRepository, "saveRepository cannot be null");
    }

    public void saveGame(GameStateLog gameStateLog, SaveSlot saveSlot) {
        saveRepository.save(Objects.requireNonNull(gameStateLog, "gameStateLog cannot be null"), saveSlot);
    }

    public Optional<GameStateLog> loadGame(SaveSlot saveSlot) {
        return saveRepository.load(saveSlot);
    }

    public boolean deleteSave(SaveSlot saveSlot) {
        return saveRepository.delete(saveSlot);
    }

    public List<SaveSlotInfo> getAvailableSlots() {
        return saveRepository.getAvailableSlots().stream()
                .map(this::toSlotInfo)
                .toList();
    }

    public List<SaveSlotInfo> listSlots() {
        return getAvailableSlots();
    }

    private SaveSlotInfo toSlotInfo(SaveSlot saveSlot) {
        Optional<GameStateLog> gameStateLog = saveRepository.load(saveSlot);
        return gameStateLog
                .map(state -> SaveSlotInfo.occupied(saveSlot, state))
                .orElseGet(() -> SaveSlotInfo.empty(saveSlot));
    }
}
