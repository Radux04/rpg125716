package it.unicam.cs.mpgc.rpg125716.persistence;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class XmlSaveRepository implements SaveRepository {
    private final Path saveDirectory;
    private final XmlMapper xmlMapper;

    public XmlSaveRepository() {
        this(Path.of("saves"));
    }

    public XmlSaveRepository(Path saveDirectory) {
        this(saveDirectory, createDefaultMapper());
    }

    public XmlSaveRepository(Path saveDirectory, XmlMapper xmlMapper) {
        this.saveDirectory = Objects.requireNonNull(saveDirectory, "saveDirectory cannot be null");
        this.xmlMapper = Objects.requireNonNull(xmlMapper, "xmlMapper cannot be null");
    }

    @Override
    public void save(GameStateLog gameStateLog, SaveSlot saveSlot) {
        Objects.requireNonNull(gameStateLog, "gameStateLog cannot be null");
        Objects.requireNonNull(saveSlot, "saveSlot cannot be null");

        GameStateLog snapshot = gameStateLog.snapshotForSlot(saveSlot);

        try {
            Files.createDirectories(saveDirectory);
            xmlMapper.writeValue(saveSlot.resolvePath(saveDirectory).toFile(), snapshot);
        } catch (IOException e) {
            throw new UncheckedIOException("unable to save game to " + saveSlot, e);
        }
    }

    @Override
    public Optional<GameStateLog> load(SaveSlot saveSlot) {
        Objects.requireNonNull(saveSlot, "saveSlot cannot be null");
        Path savePath = saveSlot.resolvePath(saveDirectory);

        if (!Files.exists(savePath)) {
            return Optional.empty();
        }

        try {
            GameStateLog gameStateLog = xmlMapper.readValue(savePath.toFile(), GameStateLog.class);
            gameStateLog.synchronizePlayerInventory();
            gameStateLog.validate();
            return Optional.of(gameStateLog);
        } catch (IOException e) {
            throw new UncheckedIOException("unable to load game from " + saveSlot, e);
        }
    }

    @Override
    public boolean delete(SaveSlot saveSlot) {
        Objects.requireNonNull(saveSlot, "saveSlot cannot be null");

        try {
            return Files.deleteIfExists(saveSlot.resolvePath(saveDirectory));
        } catch (IOException e) {
            throw new UncheckedIOException("unable to delete save " + saveSlot, e);
        }
    }

    @Override
    public boolean exists(SaveSlot saveSlot) {
        Objects.requireNonNull(saveSlot, "saveSlot cannot be null");
        return Files.exists(saveSlot.resolvePath(saveDirectory));
    }

    @Override
    public List<SaveSlot> getAvailableSlots() {
        return List.of(SaveSlot.values());
    }

    public Path getSaveDirectory() {
        return saveDirectory;
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
