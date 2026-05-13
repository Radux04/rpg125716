package it.unicam.cs.mpgc.rpg125716.model.item;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public abstract class Item {
    private final String name;
    private final ItemType type;
    private final String description;
    private final boolean consumable;

    protected Item(String name, ItemType type, String description, boolean consumable) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.consumable = consumable;
    }

    public abstract void use(Player player);
}
