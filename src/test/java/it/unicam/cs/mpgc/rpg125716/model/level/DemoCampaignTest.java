package it.unicam.cs.mpgc.rpg125716.model.level;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.BossEnemy;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import it.unicam.cs.mpgc.rpg125716.model.item.BossSword;
import it.unicam.cs.mpgc.rpg125716.model.item.Helmet;
import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoCampaignTest {

    @Test
    void campaignContainsTheThreeRequestedLevels() {
        DemoCampaign campaign = new DemoCampaign();
        List<DemoLevel> levels = campaign.getLevels();

        assertEquals(3, levels.size());

        DemoLevel level1 = levels.get(0);
        assertTrue(level1.isTutorial());
        assertEquals(1, level1.getEnemies().size());
        assertInstanceOf(OriginStone.class, level1.getCompletionDrop());
        assertTrue(level1.isUnlocksElementChoice());

        DemoLevel level2 = levels.get(1);
        assertTrue(level2.getEnemies().size() >= 2);
        assertInstanceOf(Potion.class, level2.getRewardChoices().get(LevelRewardChoice.HEALING_POTION));
        assertInstanceOf(Helmet.class, level2.getRewardChoices().get(LevelRewardChoice.DEFENSE_HELMET));
        for (Enemy enemy : level2.getEnemies()) {
            assertTrue(enemy.getDetectionRange() > 0);
            assertTrue(enemy.shouldChasePlayer(enemy.getDetectionRange()));
        }

        DemoLevel level3 = levels.get(2);
        assertTrue(level3.isBossFight());
        assertInstanceOf(BossEnemy.class, level3.getEnemies().get(0));
        assertInstanceOf(BossSword.class, level3.getCompletionDrop());
        assertTrue(level3.isEndsDemoWithVictory());
    }

    @Test
    void completedLevelTwoAllowsOnlyOneRewardChoice() {
        DemoLevel level2 = new DemoCampaign().getLevels().get(1);
        Player player = new Player("Hero", 60, 10, 5, 8);

        level2.getEnemies().forEach(enemy -> enemy.setHp(0));

        var reward = level2.chooseReward(player, LevelRewardChoice.DEFENSE_HELMET);

        assertTrue(player.getInventory().containsItem(reward));
        assertThrows(IllegalStateException.class, () -> level2.chooseReward(player, LevelRewardChoice.HEALING_POTION));
    }
}
