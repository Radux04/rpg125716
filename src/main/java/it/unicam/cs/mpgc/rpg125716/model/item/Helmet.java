package it.unicam.cs.mpgc.rpg125716.model.item;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Helmet extends Armor {
    public Helmet() {
        super("Elmo del Guardiano", "Un elmo che aumenta la difesa del player di 2 punti.", 2);
    }
}
