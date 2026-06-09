package it.unicam.cs.mpgc.rpg125716.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveSlotInfo {
    private SaveSlot slot;
    private boolean occupied;
    private String playerName;
    private Integer currentLevel;
    private LocalDateTime lastSavedAt;

    public static SaveSlotInfo empty(SaveSlot slot) {
        return new SaveSlotInfo(slot, false, null, null, null);
    }

    public static SaveSlotInfo occupied(SaveSlot slot, GameStateLog gameStateLog) {
        return new SaveSlotInfo(
                slot,
                true,
                gameStateLog.getPlayer() == null ? null : gameStateLog.getPlayer().getName(),
                gameStateLog.getCurrentLevel(),
                gameStateLog.getLastSavedAt()
        );
    }
}
