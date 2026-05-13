package it.unicam.cs.mpgc.rpg125716.model.item;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Armor extends Item {
    private final int defenseBonus;

    public Armor() {
        this("Armor", "Aumenta la difesa del player.", 3);
    }

    public Armor(String name, String description, int defenseBonus) {
        super(name, ItemType.ARMOR, description, false);
        this.defenseBonus = defenseBonus;
    }

    @Override
    public void use(Player player) {
        player.setDefense(player.getDefense() + defenseBonus);
    }
}
