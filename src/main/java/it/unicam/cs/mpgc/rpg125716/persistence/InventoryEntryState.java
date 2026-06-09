package it.unicam.cs.mpgc.rpg125716.persistence;

import it.unicam.cs.mpgc.rpg125716.model.item.Armor;
import it.unicam.cs.mpgc.rpg125716.model.item.BossSword;
import it.unicam.cs.mpgc.rpg125716.model.item.Helmet;
import it.unicam.cs.mpgc.rpg125716.model.item.Item;
import it.unicam.cs.mpgc.rpg125716.model.item.ItemType;
import it.unicam.cs.mpgc.rpg125716.model.item.KeyItem;
import it.unicam.cs.mpgc.rpg125716.model.item.OriginStone;
import it.unicam.cs.mpgc.rpg125716.model.item.Potion;
import it.unicam.cs.mpgc.rpg125716.model.item.Weapon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEntryState {
    private String itemClassName;
    private ItemType itemType;
    private String name;
    private String description;
    private boolean consumable;
    private int quantity;
    private Integer healAmount;
    private Integer attackBonus;
    private Integer defenseBonus;

    public static InventoryEntryState fromItemStack(Item item, int quantity) {
        InventoryEntryState inventoryEntryState = new InventoryEntryState();
        inventoryEntryState.itemClassName = item.getClass().getSimpleName();
        inventoryEntryState.itemType = item.getType();
        inventoryEntryState.name = item.getName();
        inventoryEntryState.description = item.getDescription();
        inventoryEntryState.consumable = item.isConsumable();
        inventoryEntryState.quantity = quantity;

        if (item instanceof Potion potion) {
            inventoryEntryState.healAmount = potion.getHealAmount();
        }

        if (item instanceof Weapon weapon) {
            inventoryEntryState.attackBonus = weapon.getAttackBonus();
        }

        if (item instanceof Armor armor) {
            inventoryEntryState.defenseBonus = armor.getDefenseBonus();
        }

        return inventoryEntryState;
    }

    public Item toItem() {
        return switch (itemType) {
            case POTION -> new Potion(name, description, healAmount == null ? 25 : healAmount);
            case WEAPON -> createWeapon();
            case ARMOR -> createArmor();
            case KEY_ITEM -> createKeyItem();
        };
    }

    private Item createWeapon() {
        if (BossSword.class.getSimpleName().equals(itemClassName)) {
            return new BossSword();
        }

        return new Weapon(name, description, attackBonus == null ? 0 : attackBonus);
    }

    private Item createArmor() {
        if (Helmet.class.getSimpleName().equals(itemClassName)) {
            return new Helmet();
        }

        return new Armor(name, description, defenseBonus == null ? 0 : defenseBonus);
    }

    private Item createKeyItem() {
        if (OriginStone.class.getSimpleName().equals(itemClassName)) {
            return new OriginStone();
        }

        return new KeyItem(name, description);
    }
}
