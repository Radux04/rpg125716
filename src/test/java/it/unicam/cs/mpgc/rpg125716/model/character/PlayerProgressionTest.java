package it.unicam.cs.mpgc.rpg125716.model.character;

import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import it.unicam.cs.mpgc.rpg125716.model.progression.AchievementType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerProgressionTest {

    @Test
    void attuningToOriginStoneUnlocksElementAndAchievement() {
        Player player = new Player("Hero", 60, 10, 5, 8);
        player.collectItem(new OriginStone());

        player.attuneToOriginStone(ElementType.FIRE);

        assertEquals(ElementType.FIRE, player.getElementType());
        assertEquals("Fiamma Primordiale", player.getElementalPower().getName());
        assertTrue(player.hasAchievement(AchievementType.ORIGIN_STONE));
    }

    @Test
    void attuningWithoutOriginStoneFails() {
        Player player = new Player("Hero", 60, 10, 5, 8);

        assertThrows(IllegalStateException.class, () -> player.attuneToOriginStone(ElementType.WATER));
    }
}
