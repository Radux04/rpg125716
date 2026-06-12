package it.unicam.cs.mpgc.rpg125716.model.progression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum AchievementType {
    FIRST_KILL("first-kill", "Primo Sangue", "Sconfiggi il primo nemico."),
    BOSS_SLAYER("boss-slayer", "Guardiano sconfitto", "Sconfiggi il boss."),
    COLLECTOR("collector", "Collezionista", "Ottieni 10 oggetti."),
    ORIGIN_STONE("origin-stone", "Portatore della Pietra", "Raccogli la Pietra Elementale."),
    DEMO_COMPLETED("demo-completed", "Demo completata", "Finisci la demo.");

    private final String id;
    private final String title;
    private final String description;

    public Achievement toAchievement() {
        return new Achievement(id, title, description, false, null);
    }

    public static AchievementType fromId(String id) {
        String safeId = Objects.requireNonNull(id, "id cannot be null");
        return Arrays.stream(values())
                .filter(achievementType -> achievementType.id.equals(safeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unsupported achievement id: " + safeId));
    }
}
