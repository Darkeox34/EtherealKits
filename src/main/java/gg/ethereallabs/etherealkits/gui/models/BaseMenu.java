package gg.ethereallabs.etherealkits.gui.models;

import gg.ethereallabs.etherealkits.EtherealKits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public abstract class BaseMenu implements Listener {

    private final Component title;
    private final int size;
    protected Inventory inv;
    private final Set<UUID> viewers = new HashSet<>();

    public BaseMenu(Component title, int size) {
        this.title = title;
        this.size = size;
    }

    public BaseMenu(String title, int size) {
        this(LegacyComponentSerializer.legacyAmpersand().deserialize(title), size);
    }

    public void open(Player p) {
        inv = Bukkit.createInventory(null, size, title);
        Bukkit.getPluginManager().registerEvents(this, EtherealKits.getInstance());
        viewers.add(p.getUniqueId());
        draw(p);
        p.openInventory(inv);
    }

    public abstract void draw(Player p);

    public abstract void handleClick(Player p, int slot, InventoryClickEvent e);

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (e.getInventory() == inv) {
            e.setCancelled(true);
            handleClick(p, e.getSlot(), e);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (inv == null || e.getInventory() != inv) return;
        if (!(e.getPlayer() instanceof Player p)) return;
        viewers.remove(p.getUniqueId());
        if (viewers.isEmpty()) {
            HandlerList.unregisterAll(this);
        }
    }

    protected ItemStack createItem(Component name, Material material, List<Component> lore, int qty) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        item.setAmount(qty);
        if (meta != null) {
            meta.customName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    protected ItemStack createItem(Component name, Material material, List<Component> lore, int qty, Map<Enchantment, Integer> enchants) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        item.setAmount(qty);
        if (meta != null) {
            meta.customName(name);
            meta.lore(lore);

            if (enchants != null) {
                enchants.forEach((enchant, level) -> meta.addEnchant(enchant, level, false));
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}
