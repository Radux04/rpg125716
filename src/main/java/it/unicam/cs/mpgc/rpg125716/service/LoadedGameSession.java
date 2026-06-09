package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.level.DemoCampaign;
import it.unicam.cs.mpgc.rpg125716.persistence.GameStateLog;
import it.unicam.cs.mpgc.rpg125716.persistence.SaveSlot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class LoadedGameSession {
    private final SaveSlot saveSlot;
    private final Player player;
    private final DemoCampaign campaign;
    private final List<String> completedLevels;
    private final GameStateLog sourceSave;
    private final LocalDateTime loadedAt;

    public LoadedGameSession(
            SaveSlot saveSlot,
            Player player,
            DemoCampaign campaign,
            List<String> completedLevels,
            GameStateLog sourceSave,
            LocalDateTime loadedAt
    ) {
        this.saveSlot = saveSlot;
        this.player = player;
        this.campaign = campaign;
        this.completedLevels = List.copyOf(completedLevels);
        this.sourceSave = sourceSave;
        this.loadedAt = loadedAt;
    }
}
