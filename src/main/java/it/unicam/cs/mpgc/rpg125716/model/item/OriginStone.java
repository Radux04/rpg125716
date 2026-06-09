package it.unicam.cs.mpgc.rpg125716.model.item;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OriginStone extends KeyItem {
    public OriginStone() {
        super("Pietra dell'Origine", "Una pietra antica che permette di scegliere il proprio elemento.");
    }
}
