package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Slime;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatServiceTest {

    @Test
    void defeatingTheFirstEnemyUnlocksFirstKillAchievement() {
        Player player = new Player("Hero", 60, 30, 5, 8);
        Slime slime = new Slime();
        CombatService combatService = new CombatService();

        CombatResult result = combatService.playerAttack(player, slime);

        assertTrue(result.isCombatFinished());
        assertEquals(CombatWinner.PLAYER, result.getWinner());
        assertTrue(player.hasAchievement(AchievementType.FIRST_KILL));
    }
}
