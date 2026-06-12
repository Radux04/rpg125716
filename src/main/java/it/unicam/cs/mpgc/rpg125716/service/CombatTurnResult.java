package it.unicam.cs.mpgc.rpg125716.service;

import lombok.Getter;

import java.util.Objects;

@Getter
public class CombatTurnResult {
    private final CurrentGameState currentGameState;
    private final CombatResult playerActionResult;
    private final CombatResult enemyActionResult;

    public CombatTurnResult(
            CurrentGameState currentGameState,
            CombatResult playerActionResult,
            CombatResult enemyActionResult
    ) {
        this.currentGameState = Objects.requireNonNull(currentGameState, "currentGameState cannot be null");
        this.playerActionResult = Objects.requireNonNull(playerActionResult, "playerActionResult cannot be null");
        this.enemyActionResult = enemyActionResult;
    }

    public boolean isLevelCleared() {
        return currentGameState.getCurrentLevel().isCompleted();
    }

    public boolean isPlayerDefeated() {
        return !currentGameState.getPlayer().isAlive();
    }

    public String buildBattleLog() {
        if (enemyActionResult == null) {
            return playerActionResult.getMessage();
        }

        return playerActionResult.getMessage() + System.lineSeparator() + enemyActionResult.getMessage();
    }
}
