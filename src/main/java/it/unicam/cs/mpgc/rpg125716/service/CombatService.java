package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;

public class CombatService {
    private boolean combatFinished;
    private CombatWinner winner = CombatWinner.NONE;

    public CombatResult playerAttack(Player player, Enemy enemy) {
        return attack(player, enemy);
    }

    public CombatResult enemyAttack(Enemy enemy, Player player) {
        return attack(enemy, player);
    }

    public CombatResult attack(Player player, Enemy enemy) {
        int damage = calculateDamage(player.getAttack(), enemy.getDefense());
        enemy.setHp(Math.max(0, enemy.getHp() - damage));

        if (enemy.getHp() == 0) {
            player.gainExperience(enemy.getExperienceReward());
            player.setGold(player.getGold() + enemy.getGoldReward());
            finishCombat(CombatWinner.PLAYER);

            return new CombatResult(
                    player.getName() + " ha sconfitto " + enemy.getName(),
                    damage,
                    combatFinished,
                    winner
            );
        }

        return new CombatResult(
                player.getName() + " attacca " + enemy.getName(),
                damage,
                combatFinished,
                winner
        );
    }

    public CombatResult attack(Enemy enemy, Player player) {
        int previousHp = player.getCurrentHp();
        player.takeDamage(enemy.getAttack());
        player.setCurrentHp(Math.max(0, player.getCurrentHp()));
        int damage = previousHp - player.getCurrentHp();

        if (!player.isAlive()) {
            finishCombat(CombatWinner.ENEMY);

            return new CombatResult(
                    enemy.getName() + " ha sconfitto " + player.getName(),
                    damage,
                    combatFinished,
                    winner
            );
        }

        return new CombatResult(
                enemy.getName() + " attacca " + player.getName(),
                damage,
                combatFinished,
                winner
        );
    }

    public CombatResult useItem(Player player, Item item) {
        item.use(player);

        return new CombatResult(
                player.getName() + " usa " + item.getName(),
                0,
                combatFinished,
                winner
        );
    }

    public boolean isCombatFinished() {
        return combatFinished;
    }

    public CombatWinner getWinner() {
        return winner;
    }

    public void resetCombat() {
        combatFinished = false;
        winner = CombatWinner.NONE;
    }

    private int calculateDamage(int attack, int defense) {
        return Math.max(1, attack - defense);
    }

    private void finishCombat(CombatWinner winner) {
        this.combatFinished = true;
        this.winner = winner;
    }
}
