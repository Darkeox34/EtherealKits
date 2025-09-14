package gg.ethereallabs.heavenkits.kits.models;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitTemplate {
    private String name;
    private final List<ItemTemplate> items;
    private long cooldown;
    private String permission;


    public KitTemplate(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<ItemTemplate> getItems() { return items; }
    public long getCooldown() { return cooldown; }
    public String getPermission() { return permission; }

    public void setName(String name) {
        this.name = name;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public void addItem(ItemTemplate item) {
        items.add(item);
    }
}
