package it.unicam.cs.mpgc.rpg125716.persistence;

import java.util.List;
import java.util.Optional;

public interface SaveRepository {
    void save(GameStateLog gameStateLog, SaveSlot saveSlot);

    Optional<GameStateLog> load(SaveSlot saveSlot);

    boolean delete(SaveSlot saveSlot);

    boolean exists(SaveSlot saveSlot);

    List<SaveSlot> getAvailableSlots();
}
