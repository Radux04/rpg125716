package it.unicam.cs.mpgc.rpg125716.model.item;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;

public interface Item {
    String getName();

    void use(Player player);
}
