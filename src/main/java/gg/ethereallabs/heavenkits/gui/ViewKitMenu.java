package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

import static gg.ethereallabs.heavenkits.HeavenKits.*;

public class ViewKitMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    KitTemplate kit;

    public ViewKitMenu(KitTemplate kit) {
        super(kit.getDisplayName(), 54);
        this.kit = kit;
    }

    @Override
    public void draw(Player p) {
        inv.clear();
        List<ItemTemplate> items = kit.getItems();

        int i = 0;
        for (ItemTemplate entry : items) {
            if (i >= slotsList.size()) {
                break;
            }
            Component name = entry.getName().decoration(TextDecoration.ITALIC, false);
            List<Component> lore = List.of();
            inv.setItem(slotsList.get(i), createItem(name, entry.getItem().getType(), lore, entry.getQty(), entry.getEnchantments()));
            i++;
        }

        inv.setItem(49, createItem(Component.text("Go back")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false), Material.RED_STAINED_GLASS_PANE, Collections.emptyList(), 1));
    }

    @Override
    public void handleClick(Player p, int slot, InventoryClickEvent e) {
        if(slot == 49){
            new KitsMenu().open(p);
        }
    }
}
