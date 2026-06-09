package it.unicam.cs.mpgc.rpg125716.model.item;

import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import lombok.ToString;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@ToString
public class Inventory {
    private final Map<Item, Integer> items = new LinkedHashMap<>();

    public Inventory() {
    }

    public Inventory(Inventory other) {
        Objects.requireNonNull(other, "other cannot be null");
        this.items.putAll(other.items);
    }

    public void addItem(Item item) {
        items.merge(item, 1, Integer::sum);
    }

    public boolean removeItem(Item item) {
        Integer quantity = items.get(item);

        if (quantity == null) {
            return false;
        }

        if (quantity == 1) {
            items.remove(item);
        } else {
            items.put(item, quantity - 1);
        }

        return true;
    }

    public boolean useItem(Player player, Item item) {
        if (!items.containsKey(item)) {
            return false;
        }

        item.use(player);

        if (item.isConsumable()) {
            removeItem(item);
        }

        return true;
    }

    public boolean containsItem(Item item) {
        return items.containsKey(item);
    }

    public Map<Item, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }
}
