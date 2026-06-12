package it.unicam.cs.mpgc.rpg125716.model.character;

import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerProgressionTest {

    @Test
    void attuningToOriginStoneUnlocksElement() {
        Player player = new Player("Hero", 60, 10, 5, 8);
        player.collectItem(new OriginStone());

        player.attuneToOriginStone(ElementType.FIRE);

        assertEquals(ElementType.FIRE, player.getElementType());
        assertEquals("Fiamma Primordiale", player.getElementalPower().getName());
    }

    @Test
    void attuningWithoutOriginStoneFails() {
        Player player = new Player("Hero", 60, 10, 5, 8);

        assertThrows(IllegalStateException.class, () -> player.attuneToOriginStone(ElementType.WATER));
    }

    @Test
    void windElementBoostsDodgeChance() {
        Player player = new Player("Hero", 60, 10, 5, 8);
        player.collectItem(new OriginStone());
        int baseDodgeChance = player.getDodgeChancePercentage();

        player.attuneToOriginStone(ElementType.WIND);

        assertEquals(ElementType.WIND, player.getElementType());
        assertEquals(28, player.getDodgeChancePercentage());
        assertTrue(player.getDodgeChancePercentage() > baseDodgeChance);
    }
}
