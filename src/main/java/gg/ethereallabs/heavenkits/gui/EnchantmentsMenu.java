package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class EnchantmentsMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    ItemTemplate item;

    public EnchantmentsMenu(ItemTemplate item) {
        super("&bModificando " + item.getName() + " enchantments", 54);
        this.item = item;
    }

    @Override
    public void draw(Player p) {
        inv.clear();

        int i = 0;

        for (Enchantment enchant : Enchantment.values()) {
            if (i >= slotsList.size()) break;

            ItemStack book = new ItemStack(Material.BOOK);
            ItemMeta meta = book.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(enchant.getKey().getKey()));
                book.setItemMeta(meta);
            }

            inv.setItem(slotsList.get(i), book);
            i++;
        }
    }

    @Override
    public void handleClick(Player p, int slot, InventoryClickEvent e) {
    }
}
