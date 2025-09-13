package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class EditMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    KitTemplate kit;
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, ItemTemplate> slotToItem = new HashMap<>();

    public EditMenu(KitTemplate kitTemplate) {
        super("&bEditing " + kitTemplate.getName(), 54);
        kit = kitTemplate;
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
            String name = entry.getName();
            List<Component> lore = getComponents();
            int slot = slotsList.get(i);
            slotToItem.put(slot, entry);
            inv.setItem(slotsList.get(i), createItem(name, entry.getItem().getType(), lore));
            i++;
        }

        inv.setItem(49, createItem("Aggiungi un nuovo item", Material.EMERALD_BLOCK, Collections.emptyList()));
    }

    private static @NotNull List<Component> getComponents() {
        return List.of(
                mm.deserialize("<gray>Left-Click: Modifica Enchantments"),
                mm.deserialize("<gray>Shift-Left-Click: Rinomina Item"),
                mm.deserialize("<gray>Shift-Right-Click: Modifica Quantity"),
                mm.deserialize("<gray>Right-Click: Rimuovi Item")
        );
    }

    @Override
    public void handleClick(Player p, int slot, InventoryClickEvent e) {
        if (slotToItem.containsKey(slot)) {
            ItemTemplate item = slotToItem.get(slot);

            boolean leftClick = e.isLeftClick();
            boolean shiftClick = e.isShiftClick();
            boolean rightClick = e.isRightClick();

            if (leftClick && shiftClick) {
                HeavenKits.sendMessage(p, "Hai iniziato a rinominare l'item: " + item.getName());
            } else if (rightClick && shiftClick) {
                HeavenKits.sendMessage(p, "Hai iniziato a modificare la quantità di: " + item.getName());
            } else if (leftClick) {
                HeavenKits.sendMessage(p, "Hai iniziato a modificare gli enchantment di: " + item.getName());
            } else if (rightClick) {
                HeavenKits.sendMessage(p,"Hai cliccato per eliminare l'item: " + item.getName());
            }
        } else if (slot == 49) {
            HeavenKits.sendMessage(p,"Hai cliccato su Aggiungi un nuovo item");
        }
    }
}
