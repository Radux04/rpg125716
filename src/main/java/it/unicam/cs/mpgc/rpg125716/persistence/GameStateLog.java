package it.unicam.cs.mpgc.rpg125716.persistence;

import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.Inventory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@EqualsAndHashCode
@ToString
public class GameStateLog {
    public static final int CURRENT_SAVE_VERSION = 1;
    public static final int MIN_SLOT_ID = 1;
    public static final int MAX_SLOT_ID = 3;

    private final int saveVersion;
    private final int slotId;
    private final Player player;
    private final int currentLevel;
    private final List<String> completedLevels;
    private final Inventory inventory;
    private final LevelState currentLevelState;
    private final LocalDateTime lastSavedAt;

    public GameStateLog(
            int slotId,
            Player player,
            int currentLevel,
            List<String> completedLevels,
            Inventory inventory,
            LevelState currentLevelState,
            LocalDateTime lastSavedAt
    ) {
        validateSlotId(slotId);
        this.saveVersion = CURRENT_SAVE_VERSION;
        this.slotId = slotId;
        this.player = new Player(Objects.requireNonNull(player, "player cannot be null"));

        if (currentLevel <= 0) {
            throw new IllegalArgumentException("currentLevel must be positive");
        }
        this.currentLevel = currentLevel;

        this.completedLevels = List.copyOf(Objects.requireNonNull(completedLevels, "completedLevels cannot be null"));
        this.inventory = new Inventory(Objects.requireNonNull(inventory, "inventory cannot be null"));
        this.currentLevelState = Objects.requireNonNull(currentLevelState, "currentLevelState cannot be null");
        if (this.currentLevelState.getLevelNumber() != currentLevel) {
            throw new IllegalArgumentException("currentLevel must match currentLevelState.levelNumber");
        }
        this.lastSavedAt = Objects.requireNonNull(lastSavedAt, "lastSavedAt cannot be null");
    }

    public static GameStateLog fromCurrentGame(
            int slotId,
            Player player,
            DemoCampaign campaign,
            List<String> completedLevels
    ) {
        Objects.requireNonNull(player, "player cannot be null");
        DemoCampaign safeCampaign = Objects.requireNonNull(campaign, "campaign cannot be null");
        int currentLevel = safeCampaign.getCurrentLevel().getNumber();

        // If the player saves during an unfinished level, loading restarts that level from scratch.
        LevelState levelState = LevelState.restartSnapshotForLevel(currentLevel);

        return new GameStateLog(
                slotId,
                player,
                currentLevel,
                completedLevels,
                player.getInventory(),
                levelState,
                LocalDateTime.now()
        );
    }

    private static void validateSlotId(int slotId) {
        if (slotId < MIN_SLOT_ID || slotId > MAX_SLOT_ID) {
            throw new IllegalArgumentException(
                    "slotId must be between " + MIN_SLOT_ID + " and " + MAX_SLOT_ID
            );
        }
    }
}
