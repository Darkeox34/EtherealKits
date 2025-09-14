package gg.ethereallabs.heavenkits.gui.admin;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static gg.ethereallabs.heavenkits.HeavenKits.*;

public class EditMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    KitTemplate kit;
    private final Map<Integer, ItemTemplate> slotToItem = new HashMap<>();

    public EditMenu(KitTemplate kitTemplate) {
        super(Component.text("Modificando ").color(NamedTextColor.AQUA)
                .append(Component.text(kitTemplate.getName())),54);
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
            Component name = entry.getName().decoration(TextDecoration.ITALIC, false);
            List<Component> lore = getComponents();
            int slot = slotsList.get(i);
            slotToItem.put(slot, entry);
            inv.setItem(slotsList.get(i), createItem(name, entry.getItem().getType(), lore, entry.getQty(), entry.getEnchantments()));
            i++;
        }

        inv.setItem(45, createItem(Component.text("Imposta il display material del Kit")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false), Material.PAINTING, Collections.emptyList(), 1));
        inv.setItem(49, createItem(Component.text("Aggiungi un nuovo item")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false), Material.GREEN_STAINED_GLASS_PANE, Collections.emptyList(), 1));
        inv.setItem(48, createItem(Component.text("Imposta Cooldown")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false), Material.CLOCK, Collections.emptyList(), 1));
        inv.setItem(50, createItem(Component.text("Imposta Permesso")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false), Material.SHIELD, Collections.emptyList(), 1));

        inv.setItem(53, createItem(Component.text("Torna Indietro")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false), Material.RED_STAINED_GLASS_PANE, Collections.emptyList(), 1));
    }

    private static @NotNull List<Component> getComponents() {
        return List.of(
                mm.deserialize(""),
                mm.deserialize("<gray>(Left-Click)<yellow> Modifica Enchantments").decoration(TextDecoration.ITALIC, false),
                mm.deserialize("<gray>(Shift+Left-Click)<yellow> Rinomina Item").decoration(TextDecoration.ITALIC, false),
                mm.deserialize("<gray>(Shift+Right-Click)<yellow> Modifica Quantità").decoration(TextDecoration.ITALIC, false),
                mm.deserialize("<gray>(Right-Click)<yellow> Rimuovi Item").decoration(TextDecoration.ITALIC, false)
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
                handleRenameItem(p, item);
            } else if (rightClick && shiftClick) {
                handleChangeQty(p, item);
            } else if (leftClick) {
                handleEditEnchant(p, item);
            } else if (rightClick) {
                handleRemoveItem(p, item);
            }
        } else if (slot == 49) {
            handleAddItem(p);
        }
        else if(slot == 53) {
            new EditKitsMenu().open(p);
        }
        else if (slot == 48) {
            handleSetCooldown(p);
        }
        else if (slot == 50) {
            handleSetPermission(p);
        }
        else if (slot == 45) {
            handleSetDisplayMaterial(p);
        }
    }

    void handleSetDisplayMaterial(Player p){
        ChatPrompts.getInstance().ask(p, "Inserire l'item da usare come display material: ", (player, message) -> {
            if (kit == null) return;

            Material mat = Material.getMaterial(message.toUpperCase());
            if (mat == null) {
                HeavenKits.sendMessage(player, "Item non valido!");
                new EditMenu(kit).open(player);
                return;
            }

            kit.setDisplayMaterial(mat);

            HeavenKits.sendMessage(player, mat.name() + " utilizzato come display material");

            new EditMenu(kit).open(player);
        });
    }

    void handleSetPermission(Player p) {
        ChatPrompts.getInstance().ask(p, "Inserire il permesso per il kit ", (player, message) -> {
            if (kit == null) return;

            kit.setPermission(message);

            sendMessage(p, "Permesso impostato su " + message);

            new EditMenu(kit).open(player);
        });
    }

    void handleSetCooldown(Player p) {
        ChatPrompts.getInstance().ask(p, "Inserire il cooldown per il kit (es. (7d12h))", (player, message) -> {
            if (kit == null) return;

            try {
                long cooldownSeconds = parseTime(message);
                kit.setCooldown(cooldownSeconds);
                sendMessage(p, "Cooldown impostato su " + formatTime(cooldownSeconds));
                new EditMenu(kit).open(player);
            } catch (IllegalArgumentException e) {
                player.sendMessage("Formato del tempo non valido! Esempio: (7d12h),(10m),(5h15m10s)");
            }
        });
    }

    void handleEditEnchant(Player p, ItemTemplate item) {
        new EnchantmentsMenu(item, kit).open(p);
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
        ChatPrompts.getInstance().ask(p, "Sei sicuro di voler rimuovere " + item.getName() + "? (sì | no)", (player, message) -> {
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
            if (kit == null || item == null) return;

            Component newName = mm.deserialize(message);

            item.setName(newName);

            HeavenKits.sendMessage(player,
                    Component.text("Hai modificato il nome dell'item in: ").color(NamedTextColor.GREEN)
                            .append(newName)
            );

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
            Component defaultName = newItem.displayName();
            kit.getItems().add(new ItemTemplate(newItem, defaultName));
            HeavenKits.sendMessage(player, "Item aggiunto al kit: " + mat.name());

            new EditMenu(kit).open(player);
        });
    }
}
