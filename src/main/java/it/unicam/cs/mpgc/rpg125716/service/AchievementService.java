package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.progression.Achievement;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import it.unicam.cs.mpgc.rpg125716.persistence.AchievementRepository;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AchievementService {
    private final AchievementRepository achievementRepository;

    public AchievementService() {
        this(new AchievementRepository());
    }

    public AchievementService(AchievementRepository achievementRepository) {
        this.achievementRepository = Objects.requireNonNull(achievementRepository, "achievementRepository cannot be null");
    }

    public Achievement unlockAchievement(String achievementId) {
        return unlockAchievement(AchievementType.fromId(achievementId));
    }

    public Achievement unlockAchievement(AchievementType achievementType) {
        AchievementType safeAchievementType = Objects.requireNonNull(achievementType, "achievementType cannot be null");
        List<Achievement> achievements = loadMergedAchievements();
        Achievement achievement = findAchievement(achievements, safeAchievementType);

        if (achievement.unlock()) {
            achievementRepository.saveAll(achievements);
        }

        return Achievement.copyOf(achievement);
    }

    public Achievement unlockAchievement(Player player, String achievementId) {
        return unlockAchievement(player, AchievementType.fromId(achievementId));
    }

    public Achievement unlockAchievement(Player player, AchievementType achievementType) {
        Objects.requireNonNull(player, "player cannot be null");
        Achievement achievement = unlockAchievement(achievementType);
        player.unlockAchievement(achievementType);
        return achievement;
    }

    public boolean isUnlocked(String achievementId) {
        return isUnlocked(AchievementType.fromId(achievementId));
    }

    public boolean isUnlocked(AchievementType achievementType) {
        return findAchievement(loadMergedAchievements(), achievementType).isUnlocked();
    }

    public List<Achievement> getUnlockedAchievements() {
        return loadMergedAchievements().stream()
                .filter(Achievement::isUnlocked)
                .map(Achievement::copyOf)
                .toList();
    }

    public List<Achievement> getAllAchievements() {
        return loadMergedAchievements().stream()
                .map(Achievement::copyOf)
                .toList();
    }

    public void synchronizePlayerAchievements(Player player) {
        Objects.requireNonNull(player, "player cannot be null");

        for (AchievementType achievementType : player.getUnlockedAchievements()) {
            unlockAchievement(achievementType);
        }

        Set<AchievementType> mergedAchievements = new LinkedHashSet<>(player.getUnlockedAchievements());
        getUnlockedAchievements().stream()
                .map(Achievement::getId)
                .map(AchievementType::fromId)
                .forEach(mergedAchievements::add);

        player.setUnlockedAchievements(mergedAchievements);
    }

    private List<Achievement> loadMergedAchievements() {
        Map<String, Achievement> storedAchievementsById = new LinkedHashMap<>();
        achievementRepository.loadAll().forEach(achievement -> storedAchievementsById.put(achievement.getId(), achievement));

        return Arrays.stream(AchievementType.values())
                .map(achievementType -> mergeAchievement(achievementType, storedAchievementsById.get(achievementType.getId())))
                .toList();
    }

    private Achievement findAchievement(List<Achievement> achievements, AchievementType achievementType) {
        AchievementType safeAchievementType = Objects.requireNonNull(achievementType, "achievementType cannot be null");
        return achievements.stream()
                .filter(achievement -> safeAchievementType.getId().equals(achievement.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("missing achievement definition for " + safeAchievementType.name()));
    }

    private Achievement mergeAchievement(AchievementType achievementType, Achievement storedAchievement) {
        Achievement achievement = Achievement.fromType(achievementType);

        if (storedAchievement != null && storedAchievement.isUnlocked()) {
            achievement.setUnlocked(true);
            achievement.setUnlockedAt(storedAchievement.getUnlockedAt());
        }

        return achievement;
    }
}
