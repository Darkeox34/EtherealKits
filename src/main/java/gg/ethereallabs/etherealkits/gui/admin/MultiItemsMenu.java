package gg.ethereallabs.etherealkits.gui.admin;

import gg.ethereallabs.etherealkits.EtherealKits;
import gg.ethereallabs.etherealkits.gui.models.BaseMenu;
import gg.ethereallabs.etherealkits.kits.models.ItemTemplate;
import gg.ethereallabs.etherealkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.IntStream;

public class MultiItemsMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 35).boxed().toList();
    private final KitTemplate kit;

    public MultiItemsMenu(KitTemplate kit) {
        super(Component.text("Edit items: ").color(NamedTextColor.AQUA)
                .append(kit.getDisplayName()), 54);
        this.kit = kit;
    }

    @Override
    public void draw(Player p) {
        inv.clear();

        int i = 0;
        for (ItemTemplate entry : kit.getItems()) {
            if (i >= slotsList.size()) break;

            ItemStack stack = entry.getItem().clone();
            stack.setAmount(entry.getQty());

            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                if (entry.getName() != null) {
                    meta.displayName(entry.getName().decoration(TextDecoration.ITALIC, false));
                }
                if (entry.getLore() != null && !entry.getLore().isEmpty()) {
                    meta.lore(entry.getLore());
                }
                stack.setItemMeta(meta);
            }

            for (Map.Entry<Enchantment, Integer> en : entry.getEnchantments().entrySet()) {
                stack.addUnsafeEnchantment(en.getKey(), en.getValue());
            }

            inv.setItem(slotsList.get(i), stack);
            i++;
        }

        for (int j = 36; j < 45; j++) {
            inv.setItem(j, createItem(Component.text("").color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false), Material.GRAY_STAINED_GLASS_PANE, Collections.emptyList(), 1));
        }

        inv.setItem(53, createItem(Component.text("Confirm and save")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false), Material.GREEN_STAINED_GLASS_PANE, Collections.emptyList(), 1));
    }

    @Override
    public void handleClick(Player p, int slot, InventoryClickEvent e) {
        if (slot >= 0 && slot <= 35) {
            e.setCancelled(false);
            return;
        }

        if (slot == 53) {
            saveAndReturn(p);
        }
    }

    private void saveAndReturn(Player p) {
        List<ItemTemplate> newItems = new ArrayList<>();

        for (int slot : slotsList) {
            ItemStack stack = inv.getItem(slot);
            if (stack == null || stack.getType() == Material.AIR) continue;

            ItemStack clone = stack.clone();
            ItemMeta meta = clone.getItemMeta();

            Component name = null;
            List<Component> lore = null;
            if (meta != null) {
                name = meta.displayName();
                lore = meta.lore();
            }
            if (name == null) {
                name = Component.translatable(clone.translationKey());
            }

            ItemTemplate tpl = new ItemTemplate(clone, name);
            tpl.setQty(clone.getAmount());
            tpl.setLore(lore);

            Map<Enchantment, Integer> enchants = clone.getEnchantments();
            if (enchants != null) {
                enchants.forEach(tpl::addEnchantment);
            }

            newItems.add(tpl);
        }

        kit.getItems().clear();
        for (ItemTemplate it : newItems) kit.addItem(it);

        EtherealKits.getInstance().getKitManager().updateKit(kit);
        EtherealKits.sendMessage(p, "Kit Updated: " + newItems.size() + " item(s)");
        new EditMenu(kit).open(p);
    }
}