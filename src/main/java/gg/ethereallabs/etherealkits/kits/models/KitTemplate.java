package gg.ethereallabs.etherealkits.kits.models;

import gg.ethereallabs.etherealkits.EtherealKits;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class KitTemplate {
    private String name;
    private Component displayName;
    private Material displayMaterial = Material.BOOK;
    private final List<ItemTemplate> items;
    private long cooldown;
    private String permission = "None";
    private List<Component> lore = null;


    public KitTemplate(String name) {
        this.name = name;
        this.displayName = Component.text(name);
        this.items = new ArrayList<>();
    }

    public Component getDisplayName() {
        return displayName;
    }
    public String getName() { return name; }
    public Material getDisplayMaterial() { return displayMaterial; }
    public List<ItemTemplate> getItems() { return items; }
    public long getCooldown() { return cooldown; }
    public String getPermission() { return permission; }
    public List<Component> getLore() { return lore; }

    public void setName(String name) {
        this.name = name;
        EtherealKits.getInstance().getKitManager().updateKit(this);
    }

    public void setDisplayMaterial(Material displayMaterial) {
        this.displayMaterial = displayMaterial;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
    }
    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public void setLore(List<Component> lore) {
        this.lore = lore != null ? lore : new ArrayList<>();
    }

    public void addItem(ItemTemplate item) {
        item.setKit(this);
        items.add(item);
    }
}
