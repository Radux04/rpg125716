package it.unicam.cs.mpgc.rpg125716.model.progression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    private String id;
    private String title;
    private String description;
    private boolean unlocked;
    private LocalDateTime unlockedAt;

    public static Achievement fromType(AchievementType achievementType) {
        return Objects.requireNonNull(achievementType, "achievementType cannot be null").toAchievement();
    }

    public static Achievement copyOf(Achievement other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Achievement(
                other.id,
                other.title,
                other.description,
                other.unlocked,
                other.unlockedAt
        );
    }

    public boolean unlock() {
        if (unlocked) {
            return false;
        }

        unlocked = true;
        unlockedAt = LocalDateTime.now();
        return true;
    }
}
