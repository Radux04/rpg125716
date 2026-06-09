package it.unicam.cs.mpgc.rpg125716.model.item;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BossSword extends Weapon {
    public BossSword() {
        super("Spada della Vittoria", "Una spada ottenuta dal boss finale che aumenta molto l'attacco.", 12);
    }
}
