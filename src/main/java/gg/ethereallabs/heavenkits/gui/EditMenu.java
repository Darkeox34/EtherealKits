package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static gg.ethereallabs.heavenkits.HeavenKits.mm;

public class EditMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    KitTemplate kit;
    private final Map<Integer, ItemTemplate> slotToItem = new HashMap<>();

    public EditMenu(KitTemplate kitTemplate) {
        super("&bEditing " + kitTemplate.getName(), 54);
        kit = kitTemplate;
    }

    @Override
    public void draw(Player p) {
        inv.clear();
        slotToItem.clear();
        List<ItemTemplate> items = kit.getItems();

        int i = 0;
        for (ItemTemplate entry : items) {
            if (i >= slotsList.size()) {
                break;
            }
            Component name = entry.getName();
            List<Component> lore = getComponents();
            int slot = slotsList.get(i);
            slotToItem.put(slot, entry);
            inv.setItem(slotsList.get(i), createItem(name, entry.getItem().getType(), lore, entry.getQty(), entry.getEnchantments()));
            i++;
        }

        inv.setItem(49, createItem(Component.text("Aggiungi un nuovo item"), Material.EMERALD_BLOCK, Collections.emptyList(), 1));
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
                handleRenameItem(p, item);
            } else if (rightClick && shiftClick) {
                HeavenKits.sendMessage(p, "Hai iniziato a modificare la quantità di: " + item.getName());
                handleChangeQty(p, item);
            } else if (leftClick) {
                HeavenKits.sendMessage(p, "Hai iniziato a modificare gli enchantment di: " + item.getName());
                handleEditEnchant(p, item);
            } else if (rightClick) {
                HeavenKits.sendMessage(p,"Hai cliccato per eliminare l'item: " + item.getName());
                handleRemoveItem(p, item);
            }
        } else if (slot == 49) {
            handleAddItem(p);
        }
    }

    void handleEditEnchant(Player p, ItemTemplate item) {
        new EnchantmentsMenu(item).open(p);
    }

    void handleChangeQty(Player p, ItemTemplate item) {
        ChatPrompts.getInstance().ask(p, "Inserire la quantità dell'item (max. 64) ", (player, message) -> {
            if (kit == null || item == null) return;

            int qty;
            try {
                qty = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                HeavenKits.sendMessage(player, "Valore non valido! Inserisci un numero.");
                new EditMenu(kit).open(player);
                return;
            }

            if (qty < 1) qty = 1;
            if (qty > 64) qty = 64;

            item.setQty(qty);
            HeavenKits.sendMessage(player, "Quantità aggiornata a " + qty);

            new EditMenu(kit).open(player);
        });
    }

    void handleRemoveItem(Player p, ItemTemplate item) {
        ChatPrompts.getInstance().ask(p, "Sei sicuro di voler rimuovere l'item? (sì|no)", (player, message) -> {
            if (kit == null) return;

            if (item == null) return;

            if (message.equalsIgnoreCase("si") || message.equalsIgnoreCase("sì")) {
                kit.getItems().remove(item);
                HeavenKits.sendMessage(p, "Hai rimosso l'item: " + item.getName());
            }

            new EditMenu(kit).open(player);
        });
    }

    void handleRenameItem(Player p, ItemTemplate item) {
        ChatPrompts.getInstance().ask(p, "Inserire il nuovo nome dell'item: ", (player, message) -> {
            if (kit == null) return;

            if (item == null) return;

            Component newName = mm.deserialize(message);

            item.setName(newName);

            HeavenKits.sendMessage(p, "Hai modificato il nome dell'item in: " + newName);

            new EditMenu(kit).open(player);
        });
    }

    void handleAddItem(Player p){
        ChatPrompts.getInstance().ask(p, "Inserire l'item da aggiungere: ", (player, message) -> {
            if (kit == null) return;

            Material mat = Material.getMaterial(message.toUpperCase());
            if (mat == null) {
                HeavenKits.sendMessage(player, "Item non valido!");
                new EditMenu(kit).open(player);
                return;
            }

            ItemStack newItem = new ItemStack(mat);
            Component defaultName = mm.deserialize("<white>" + newItem.displayName() + "</white>");
            kit.getItems().add(new ItemTemplate(newItem, defaultName));
            HeavenKits.sendMessage(player, "Item aggiunto al kit: " + mat.name());

            new EditMenu(kit).open(player);
        });
    }
}
