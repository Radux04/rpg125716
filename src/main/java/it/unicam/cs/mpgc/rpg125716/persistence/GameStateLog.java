package it.unicam.cs.mpgc.rpg125716.persistence;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.item.Inventory;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@JacksonXmlRootElement(localName = "game-state-log")
public class GameStateLog {
    public static final int CURRENT_SAVE_VERSION = 1;
    public static final int MIN_SLOT_ID = 1;
    public static final int MAX_SLOT_ID = 3;

    private int saveVersion = CURRENT_SAVE_VERSION;
    private int slotId;
    private Player player = new Player();
    private int currentLevel;

    @JacksonXmlElementWrapper(localName = "completed-levels")
    @JacksonXmlProperty(localName = "level")
    private List<String> completedLevels = new ArrayList<>();

    private Inventory inventory = new Inventory();
    private LevelState currentLevelState = new LevelState();
    private LocalDateTime lastSavedAt;

    public GameStateLog(
            int slotId,
            Player player,
            int currentLevel,
            List<String> completedLevels,
            Inventory inventory,
            LevelState currentLevelState,
            LocalDateTime lastSavedAt
    ) {
        this.slotId = slotId;
        this.player = player == null ? new Player() : new Player(player);
        this.currentLevel = currentLevel;
        this.completedLevels = completedLevels == null ? new ArrayList<>() : new ArrayList<>(completedLevels);
        this.inventory = inventory == null ? new Inventory() : new Inventory(inventory);
        this.currentLevelState = currentLevelState == null ? new LevelState() : LevelState.copyOf(currentLevelState);
        this.lastSavedAt = lastSavedAt;
        synchronizePlayerInventory();
        validate();
    }

    public static GameStateLog fromCurrentGame(
            int slotId,
            Player player,
            DemoCampaign campaign,
            List<String> completedLevels
    ) {
        Objects.requireNonNull(player, "player cannot be null");
        DemoCampaign safeCampaign = Objects.requireNonNull(campaign, "campaign cannot be null");

        LevelState currentLevelState = safeCampaign.getCurrentLevel().isCompleted()
                ? LevelState.fromLevel(safeCampaign.getCurrentLevel(), false)
                : LevelState.restartSnapshotForLevel(safeCampaign.getCurrentLevel().getNumber());

        return new GameStateLog(
                slotId,
                player,
                safeCampaign.getCurrentLevel().getNumber(),
                completedLevels,
                player.getInventory(),
                currentLevelState,
                LocalDateTime.now()
        );
    }

    public static GameStateLog copyOf(GameStateLog other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new GameStateLog(
                other.slotId,
                other.player,
                other.currentLevel,
                other.completedLevels,
                other.inventory,
                other.currentLevelState,
                other.lastSavedAt
        );
    }

    public GameStateLog snapshotForSlot(SaveSlot saveSlot) {
        Objects.requireNonNull(saveSlot, "saveSlot cannot be null");
        Inventory sourceInventory = player != null ? player.getInventory() : inventory;

        return new GameStateLog(
                saveSlot.getSlotId(),
                player,
                currentLevel,
                completedLevels,
                sourceInventory,
                currentLevelState,
                LocalDateTime.now()
        );
    }

    public void validate() {
        validateSlotId(slotId);

        if (saveVersion <= 0) {
            throw new IllegalArgumentException("saveVersion must be positive");
        }

        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }

        if (currentLevel <= 0) {
            throw new IllegalArgumentException("currentLevel must be positive");
        }

        if (completedLevels == null) {
            completedLevels = new ArrayList<>();
        }

        if (inventory == null) {
            throw new IllegalArgumentException("inventory cannot be null");
        }

        if (currentLevelState == null) {
            throw new IllegalArgumentException("currentLevelState cannot be null");
        }

        if (currentLevelState.getLevelNumber() != currentLevel) {
            throw new IllegalArgumentException("currentLevel must match currentLevelState.levelNumber");
        }

        if (lastSavedAt == null) {
            throw new IllegalArgumentException("lastSavedAt cannot be null");
        }
    }

    public void synchronizePlayerInventory() {
        if (player == null) {
            return;
        }

        if (inventory == null) {
            inventory = new Inventory();
        }

        player.setInventory(new Inventory(inventory));
    }

    private static void validateSlotId(int slotId) {
        if (slotId < MIN_SLOT_ID || slotId > MAX_SLOT_ID) {
            throw new IllegalArgumentException(
                    "slotId must be between " + MIN_SLOT_ID + " and " + MAX_SLOT_ID
            );
        }
    }
}
