package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.event.GameEventDispatcher;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.BossEnemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Slime;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import it.unicam.cs.mpgc.rpg125716.persistence.AchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void defeatingTheFirstEnemyUnlocksFirstKillAchievement() {
        Player player = new Player("Hero", 60, 30, 5, 8);
        Slime slime = new Slime();
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.xml"))
        );
        CombatService combatService = new CombatService(achievementService);

        CombatResult result = combatService.playerAttack(player, slime);

        assertTrue(result.isCombatFinished());
        assertEquals(CombatWinner.PLAYER, result.getWinner());
        assertTrue(player.hasAchievement(AchievementType.FIRST_KILL));
        assertTrue(achievementService.isUnlocked(AchievementType.FIRST_KILL));
    }

    @Test
    void defeatingAnEnemyAwardsTheUpdatedExperienceAmount() {
        Player player = new Player("Hero", 60, 30, 5, 8);
        Slime slime = new Slime();
        CombatService combatService = new CombatService(new GameEventDispatcher());

        combatService.playerAttack(player, slime);

        assertEquals(15, player.getExperience());
    }

    @Test
    void defeatingABossUnlocksBossSlayerAchievement() {
        Player player = new Player("Hero", 60, 30, 5, 8);
        BossEnemy bossEnemy = new BossEnemy();
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("boss-achievements.xml"))
        );
        CombatService combatService = new CombatService(achievementService);

        while (bossEnemy.isAlive()) {
            combatService.playerAttack(player, bossEnemy);
        }

        assertTrue(player.hasAchievement(AchievementType.BOSS_SLAYER));
        assertTrue(achievementService.isUnlocked(AchievementType.BOSS_SLAYER));
    }

    @Test
    void playerCanDodgeIncomingEnemyAttack() {
        Player player = new Player("Hero", 60, 10, 5, 8);
        Slime slime = new Slime();
        CombatService combatService = new CombatService(new GameEventDispatcher(), () -> 0.05d);

        CombatResult result = combatService.enemyAttack(slime, player);

        assertEquals(60, player.getCurrentHp());
        assertEquals(0, result.getDamage());
        assertFalse(result.isCombatFinished());
        assertEquals("Hero schiva l'attacco di Slime", result.getMessage());
    }

    @Test
    void playerTakesDamageWhenDodgeRollFails() {
        Player player = new Player("Hero", 60, 10, 5, 8);
        Slime slime = new Slime();
        CombatService combatService = new CombatService(new GameEventDispatcher(), () -> 0.99d);

        CombatResult result = combatService.enemyAttack(slime, player);

        assertEquals(59, player.getCurrentHp());
        assertEquals(1, result.getDamage());
        assertFalse(result.isCombatFinished());
        assertEquals("Slime attacca Hero", result.getMessage());
    }

    @Test
    void fireSpecialAttackDealsABitMoreDamageThanTheBaseAttack() {
        Player player = new Player("Hero", 60, 10, 5, 8);
        Slime slime = new Slime();
        CombatService combatService = new CombatService(new GameEventDispatcher());

        CombatResult result = combatService.playerAttack(player, slime, 4);

        assertEquals(13, result.getDamage());
        assertEquals(7, slime.getHp());
    }
}
