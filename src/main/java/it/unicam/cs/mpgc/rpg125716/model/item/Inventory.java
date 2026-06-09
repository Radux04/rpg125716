package it.unicam.cs.mpgc.rpg125716.model.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import it.unicam.cs.mpgc.rpg125716.model.character.Player;
import it.unicam.cs.mpgc.rpg125716.persistence.InventoryEntryState;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
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

    @JsonIgnore
    public int getTotalItemCount() {
        return items.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    @JsonIgnore
    public Map<Item, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    @JacksonXmlElementWrapper(localName = "entries")
    @JacksonXmlProperty(localName = "entry")
    public List<InventoryEntryState> getEntries() {
        return items.entrySet().stream()
                .map(entry -> InventoryEntryState.fromItemStack(entry.getKey(), entry.getValue()))
                .toList();
    }

    public void setEntries(List<InventoryEntryState> entries) {
        items.clear();

        if (entries == null) {
            return;
        }

        for (InventoryEntryState entry : entries) {
            if (entry == null) {
                continue;
            }

            Item item = entry.toItem();
            int quantity = Math.max(1, entry.getQuantity());
            items.put(item, quantity);
        }
    }
}
