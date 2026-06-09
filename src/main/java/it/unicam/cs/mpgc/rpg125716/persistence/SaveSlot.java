package it.unicam.cs.mpgc.rpg125716.persistence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@Getter
@RequiredArgsConstructor
public enum SaveSlot {
    SLOT_1(1, "slot-1.xml"),
    SLOT_2(2, "slot-2.xml"),
    SLOT_3(3, "slot-3.xml");

    private final int slotId;
    private final String fileName;

    public Path resolvePath(Path saveDirectory) {
        return saveDirectory.resolve(fileName);
    }

    public static SaveSlot fromSlotId(int slotId) {
        for (SaveSlot saveSlot : values()) {
            if (saveSlot.slotId == slotId) {
                return saveSlot;
            }
        }

        throw new IllegalArgumentException("unsupported slotId: " + slotId);
    }
}
