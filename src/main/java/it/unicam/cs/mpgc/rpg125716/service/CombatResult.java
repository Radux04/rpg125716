package it.unicam.cs.mpgc.rpg125716.service;

import lombok.Value;

@Value
public class CombatResult {
    String message;
    int damage;
    boolean combatFinished;
    CombatWinner winner;
}
