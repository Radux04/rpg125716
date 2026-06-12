package it.unicam.cs.mpgc.rpg125716.service;

import it.unicam.cs.mpgc.rpg125716.event.EnemyDefeatedEvent;
import it.unicam.cs.mpgc.rpg125716.event.GameEventDispatcher;
import it.unicam.cs.mpgc.rpg125716.event.PlayerDiedEvent;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.model.enemy.Enemy;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;

import java.util.Objects;
import java.util.function.DoubleSupplier;

public class CombatService {
    private final GameEventDispatcher gameEventDispatcher;
    private final DoubleSupplier dodgeRollSupplier;
    private boolean combatFinished;
    private CombatWinner winner = CombatWinner.NONE;

    public CombatService() {
        this(createDefaultDispatcher(), Math::random);
    }

    public CombatService(AchievementService achievementService) {
        this(
                new GameEventDispatcher().registerListener(Objects.requireNonNull(achievementService, "achievementService cannot be null")),
                Math::random
        );
    }

    public CombatService(GameEventDispatcher gameEventDispatcher) {
        this(gameEventDispatcher, Math::random);
    }

    CombatService(GameEventDispatcher gameEventDispatcher, DoubleSupplier dodgeRollSupplier) {
        this.gameEventDispatcher = Objects.requireNonNull(gameEventDispatcher, "gameEventDispatcher cannot be null");
        this.dodgeRollSupplier = Objects.requireNonNull(dodgeRollSupplier, "dodgeRollSupplier cannot be null");
    }

    public CombatResult playerAttack(Player player, Enemy enemy) {
        return attack(player, enemy, 0);
    }

    public CombatResult playerAttack(Player player, Enemy enemy, int bonusDamage) {
        return attack(player, enemy, bonusDamage);
    }

    public CombatResult enemyAttack(Enemy enemy, Player player) {
        return attack(enemy, player);
    }

    public CombatResult attack(Player player, Enemy enemy) {
        return attack(player, enemy, 0);
    }

    public CombatResult attack(Player player, Enemy enemy, int bonusDamage) {
        int damage = calculateDamage(player.getAttack() + Math.max(0, bonusDamage), enemy.getDefense());
        enemy.setHp(Math.max(0, enemy.getHp() - damage));

        if (enemy.getHp() == 0) {
            player.gainExperience(enemy.getExperienceReward());
            player.setGold(player.getGold() + enemy.getGoldReward());
            gameEventDispatcher.dispatch(new EnemyDefeatedEvent(player, enemy));
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
        if (playerDodgesAttack(player)) {
            return new CombatResult(
                    player.getName() + " schiva l'attacco di " + enemy.getName(),
                    0,
                    combatFinished,
                    winner
            );
        }

        int previousHp = player.getCurrentHp();
        player.takeDamage(enemy.getAttack());
        player.setCurrentHp(Math.max(0, player.getCurrentHp()));
        int damage = previousHp - player.getCurrentHp();

        if (!player.isAlive()) {
            gameEventDispatcher.dispatch(new PlayerDiedEvent(player));
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
        boolean used = player.useItem(item);

        if (!used) {
            return new CombatResult(
                    player.getName() + " non possiede " + item.getName(),
                    0,
                    combatFinished,
                    winner
            );
        }

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

    private boolean playerDodgesAttack(Player player) {
        return dodgeRollSupplier.getAsDouble() < player.getDodgeChance();
    }

    private void finishCombat(CombatWinner winner) {
        this.combatFinished = true;
        this.winner = winner;
    }

    private static GameEventDispatcher createDefaultDispatcher() {
        return new GameEventDispatcher().registerListener(new AchievementService());
    }
}
