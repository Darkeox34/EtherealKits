package gg.ethereallabs.heavenkits.gui.models;

import gg.ethereallabs.heavenkits.HeavenKits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class BaseMenu implements Listener {

    private final String title;
    private final int size;
    protected Inventory inv;
    private final Set<UUID> viewers = new HashSet<>();

    public BaseMenu(String title, int size) {
        this.title = title;
        this.size = size;
    }

    public BaseMenu(String title) {
        this(title, 54);
    }

    public void open(Player p) {
        Component titleComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(title);
        inv = Bukkit.createInventory(null, size, titleComponent);
        Bukkit.getPluginManager().registerEvents(this, HeavenKits.instance);
        viewers.add(p.getUniqueId());
        draw(p);
        p.openInventory(inv);
    }

    public abstract void draw(Player p);

    public abstract void handleClick(Player p, int slot, InventoryClickEvent e);

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (inv == null || e.getClickedInventory() != inv) return;
        e.setCancelled(true);
        handleClick(p, e.getSlot(), e);
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

    protected ItemStack createItem(Component name, Material material, List<? extends Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.customName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
