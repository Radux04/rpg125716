package it.unicam.cs.mpgc.rpg125716.event;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;

import java.time.LocalDateTime;

public interface GameEvent {
    Player player();

    LocalDateTime occurredAt();
}
