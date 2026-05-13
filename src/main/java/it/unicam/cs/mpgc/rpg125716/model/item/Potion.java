package it.unicam.cs.mpgc.rpg125716.model.item;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Potion extends Item {
    private final int healAmount;

    public Potion() {
        this("Potion", "Ripristina punti vita.", 25);
    }

    public Potion(String name, String description, int healAmount) {
        super(name, ItemType.POTION, description, true);
        this.healAmount = healAmount;
    }

    @Override
    public void use(Player player) {
        player.heal(healAmount);
    }
}
