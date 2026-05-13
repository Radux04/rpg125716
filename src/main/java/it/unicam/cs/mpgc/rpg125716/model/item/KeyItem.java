package it.unicam.cs.mpgc.rpg125716.model.item;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;

public class KeyItem extends Item {
    public KeyItem() {
        this("Key", "Oggetto chiave.");
    }

    public KeyItem(String name, String description) {
        super(name, ItemType.KEY_ITEM, description, false);
    }

    @Override
    public void use(Player player) {
        // Gli oggetti chiave non modificano direttamente le statistiche del player.
    }
}
