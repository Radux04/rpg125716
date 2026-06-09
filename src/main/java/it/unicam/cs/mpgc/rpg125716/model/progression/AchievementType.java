package it.unicam.cs.mpgc.rpg125716.model.progression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AchievementType {
    FIRST_KILL("first kill", "Sconfiggi il tuo primo nemico."),
    ORIGIN_STONE("Pietra dell'Origine", "Ottieni il potere della Pietra dell'Origine.");

    private final String displayName;
    private final String description;
}
