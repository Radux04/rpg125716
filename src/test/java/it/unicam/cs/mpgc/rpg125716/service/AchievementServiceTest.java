package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
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
                new AchievementRepository(tempDir.resolve("achievements.json"))
        );

        Achievement firstUnlock = achievementService.unlockAchievement(AchievementType.FIRST_KILL);
        LocalDateTime unlockedAt = firstUnlock.getUnlockedAt();
        Achievement secondUnlock = achievementService.unlockAchievement(AchievementType.FIRST_KILL);

        assertTrue(firstUnlock.isUnlocked());
        assertNotNull(unlockedAt);
        assertEquals(unlockedAt, secondUnlock.getUnlockedAt());
        assertTrue(achievementService.isUnlocked(AchievementType.FIRST_KILL));
        assertEquals(1, achievementService.getUnlockedAchievements().size());
    }

    @Test
    void synchronizePlayerAchievementsMergesLocalAndGlobalUnlocks() {
        AchievementService achievementService = new AchievementService(
                new AchievementRepository(tempDir.resolve("achievements.json"))
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
