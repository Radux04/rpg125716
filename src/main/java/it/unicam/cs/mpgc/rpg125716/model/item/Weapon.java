package it.unicam.cs.mpgc.rpg125716.model.item;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Weapon extends Item {
    private final int attackBonus;

    public Weapon() {
        this("Sword", "Aumenta l'attacco del player.", 5);
    }

    public Weapon(String name, String description, int attackBonus) {
        super(name, ItemType.WEAPON, description, false);
        this.attackBonus = attackBonus;
    }

    @Override
    public void use(Player player) {
        player.setAttack(player.getAttack() + attackBonus);
    }
}
