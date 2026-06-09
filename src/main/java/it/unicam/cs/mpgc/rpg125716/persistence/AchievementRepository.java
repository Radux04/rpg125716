package it.unicam.cs.mpgc.rpg125716.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.unicam.cs.mpgc.rpg125716.model.progression.Achievement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class AchievementRepository {
    private final Path achievementFile;
    private final ObjectMapper objectMapper;

    public AchievementRepository() {
        this(Path.of("achievements.json"));
    }

    public AchievementRepository(Path achievementFile) {
        this(achievementFile, createDefaultMapper());
    }

    public AchievementRepository(Path achievementFile, ObjectMapper objectMapper) {
        this.achievementFile = Objects.requireNonNull(achievementFile, "achievementFile cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
    }

    public List<Achievement> loadAll() {
        if (!Files.exists(achievementFile)) {
            return List.of();
        }

        try {
            List<Achievement> achievements = objectMapper.readValue(
                    achievementFile.toFile(),
                    new TypeReference<List<Achievement>>() {
                    }
            );
            return achievements == null
                    ? List.of()
                    : achievements.stream()
                    .filter(Objects::nonNull)
                    .map(Achievement::copyOf)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("unable to load achievements from " + achievementFile, e);
        }
    }

    public void saveAll(List<Achievement> achievements) {
        Objects.requireNonNull(achievements, "achievements cannot be null");

        try {
            Path parent = achievementFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                    achievementFile.toFile(),
                    achievements.stream()
                            .map(Achievement::copyOf)
                            .toList()
            );
        } catch (IOException e) {
            throw new UncheckedIOException("unable to save achievements to " + achievementFile, e);
        }
    }

    public boolean exists() {
        return Files.exists(achievementFile);
    }

    public Path getAchievementFile() {
        return achievementFile;
    }

    private static ObjectMapper createDefaultMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
