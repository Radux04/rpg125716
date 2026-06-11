package it.unicam.cs.mpgc.rpg125716.persistence;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
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
    private final XmlMapper xmlMapper;

    public AchievementRepository() {
        this(Path.of("achievements.xml"));
    }

    public AchievementRepository(Path achievementFile) {
        this(achievementFile, createDefaultMapper());
    }

    public AchievementRepository(Path achievementFile, XmlMapper xmlMapper) {
        this.achievementFile = Objects.requireNonNull(achievementFile, "achievementFile cannot be null");
        this.xmlMapper = Objects.requireNonNull(xmlMapper, "xmlMapper cannot be null");
    }

    public List<Achievement> loadAll() {
        if (!Files.exists(achievementFile)) {
            return List.of();
        }

        try {
            AchievementCatalog achievementCatalog = xmlMapper.readValue(
                    achievementFile.toFile(),
                    AchievementCatalog.class
            );
            return achievementCatalog == null
                    ? List.of()
                    : achievementCatalog.getAchievements().stream()
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

            xmlMapper.writeValue(
                    achievementFile.toFile(),
                    new AchievementCatalog(
                            achievements.stream()
                                    .map(Achievement::copyOf)
                                    .toList()
                    )
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

    private static XmlMapper createDefaultMapper() {
        XmlMapper xmlMapper = XmlMapper.builder()
                .defaultUseWrapper(false)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .build();
        xmlMapper.registerModule(new JavaTimeModule());
        return xmlMapper;
    }
}
