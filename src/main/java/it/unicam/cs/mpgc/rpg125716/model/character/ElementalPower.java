package it.unicam.cs.mpgc.rpg125716.model.character;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@EqualsAndHashCode
@ToString
public class ElementalPower {
    private final ElementType elementType;
    private final String name;
    private final String description;

    public ElementalPower(ElementType elementType, String name, String description) {
        this.elementType = Objects.requireNonNull(elementType, "elementType cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
    }

    public static ElementalPower fromElementType(ElementType elementType) {
        return switch (Objects.requireNonNull(elementType, "elementType cannot be null")) {
            case FIRE -> new ElementalPower(
                    ElementType.FIRE,
                    "Fiamma Primordiale",
                    "Aumenta il danno degli attacchi."
            );
            case WATER -> new ElementalPower(
                    ElementType.WATER,
                    "Cura Fluente",
                    "Permette al giocatore di recuperare punti vita."
            );
            case WIND -> new ElementalPower(
                    ElementType.WIND,
                    "Passo del Vento",
                    "Aumenta la possibilita di evitare gli attacchi."
            );
            case EARTH -> new ElementalPower(
                    ElementType.EARTH,
                    "Pelle di Roccia",
                    "Aumenta la difesa del giocatore."
            );
        };
    }
}
