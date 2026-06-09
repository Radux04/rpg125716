package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Slime;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import it.unicam.cs.mpgc.rpg125716.persistence.AchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void defeatingTheFirstEnemyUnlocksFirstKillAchievement() {
        Player player = new Player("Hero", 60, 30, 5, 8);
        Slime slime = new Slime();
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.json"))
        );
        CombatService combatService = new CombatService(achievementService);

        CombatResult result = combatService.playerAttack(player, slime);

        assertTrue(result.isCombatFinished());
        assertEquals(CombatWinner.PLAYER, result.getWinner());
        assertTrue(player.hasAchievement(AchievementType.FIRST_KILL));
        assertTrue(achievementService.isUnlocked(AchievementType.FIRST_KILL));
    }
}
