package it.unicam.cs.mpgc.rpg125716.model.level;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LevelRewardChoice {
    HEALING_POTION("Pozione curativa"),
    DEFENSE_HELMET("Elmo con +2 difesa");

    private final String label;
}
