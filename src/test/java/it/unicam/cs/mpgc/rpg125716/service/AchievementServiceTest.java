package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.event.EnemyDefeatedEvent;
import it.unicam.cs.mpgc.rpg125716.event.ItemCollectedEvent;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.BossEnemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Slime;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import it.unicam.cs.mpgc.rpg125716.model.progression.Achievement;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import it.unicam.cs.mpgc.rpg125716.persistence.AchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void achievementServiceUnlocksAchievementsOnlyOnceAndPersistsThemGlobally() {
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.xml"))
        );
        Player player = new Player("Hero", 60, 10, 5, 8);

        achievementService.onGameEvent(new EnemyDefeatedEvent(player, new Slime()));
        Achievement firstUnlock = achievementService.getUnlockedAchievements().getFirst();
        LocalDateTime unlockedAt = firstUnlock.getUnlockedAt();
        achievementService.onGameEvent(new EnemyDefeatedEvent(player, new Slime()));

        assertTrue(firstUnlock.isUnlocked());
        assertNotNull(unlockedAt);
        assertEquals(unlockedAt, achievementService.getUnlockedAchievements().getFirst().getUnlockedAt());
        assertTrue(player.hasAchievement(AchievementType.FIRST_KILL));
        assertTrue(achievementService.isUnlocked(AchievementType.FIRST_KILL));
        assertEquals(1, achievementService.getUnlockedAchievements().size());
    }

    @Test
    void achievementServiceUnlocksBossSlayerAndCollectorFromEvents() {
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("event-achievements.xml"))
        );
        Player player = new Player("Hero", 60, 10, 5, 8);

        achievementService.onGameEvent(new EnemyDefeatedEvent(player, new BossEnemy()));

        for (int i = 0; i < AchievementService.COLLECTOR_TARGET; i++) {
            player.collectItem(new Potion());
            achievementService.onGameEvent(
                    new ItemCollectedEvent(player, new Potion(), player.getInventory().getTotalItemCount())
            );
        }

        assertTrue(player.hasAchievement(AchievementType.FIRST_KILL));
        assertTrue(player.hasAchievement(AchievementType.BOSS_SLAYER));
        assertTrue(player.hasAchievement(AchievementType.COLLECTOR));
        assertEquals(3, achievementService.getUnlockedAchievements().size());
    }

    @Test
    void synchronizePlayerAchievementsMergesLocalAndGlobalUnlocks() {
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.xml"))
        );
        Player player = new Player("Hero", 60, 10, 5, 8);

        player.unlockAchievement(AchievementType.ORIGIN_STONE);
        achievementService.unlockAchievement(AchievementType.FIRST_KILL);
        achievementService.synchronizePlayerAchievements(player);

        assertTrue(player.hasAchievement(AchievementType.FIRST_KILL));
        assertTrue(player.hasAchievement(AchievementType.ORIGIN_STONE));

        List<Achievement> unlockedAchievements = achievementService.getUnlockedAchievements();
        assertEquals(2, unlockedAchievements.size());
        assertFalse(achievementService.getAllAchievements().isEmpty());
    }
}
