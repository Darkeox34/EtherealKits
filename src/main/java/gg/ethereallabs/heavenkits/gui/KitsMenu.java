package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gg.ethereallabs.heavenkits.HeavenKits.*;

public class KitsMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    private final Map<Integer, KitTemplate> slotToKit = new HashMap<>();

    public KitsMenu() {
        super("Kits", 54);
    }

    @Override
    public void draw(Player p) {
        inv.clear();
        slotToKit.clear();

        HashMap<String, KitTemplate> kits = HeavenKits.kitManager.getKits();

        int i = 0;
        for (Map.Entry<String, KitTemplate> entry : kits.entrySet()) {
            if (i >= slotsList.size()) break;

            String name = entry.getKey();
            List<Component> lore = getComponents(entry);
            KitTemplate kit = entry.getValue();

            int slot = slotsList.get(i);
            inv.setItem(slotsList.get(i), createItem(Component.text(name), Material.BOOK, lore, 1));
            slotToKit.put(slot, kit);

            i++;
        }

        inv.setItem(49, createItem(Component.text("Crea un nuovo Kit"), Material.EMERALD_BLOCK, Collections.emptyList(), 1));
    }

    private static @NotNull List<Component> getComponents(Map.Entry<String, KitTemplate> entry) {
        KitTemplate kit = entry.getValue();

        return List.of(
                mm.deserialize("<gray>Left-Click: Modifica"),
                mm.deserialize("<gray>Shift-Left-Click: Rinomina"),
                mm.deserialize("<gray>Right-Click: Rimuovi"),
                Component.empty(),
                mm.deserialize("<yellow>Cooldown: " + kit.getCooldown()),
                mm.deserialize("<yellow>Permesso: " + kit.getPermission())
        );
    }

    @Override
    public void handleClick(Player p, int slot, InventoryClickEvent e) {
        if (slotToKit.containsKey(slot)) {
            KitTemplate kit = slotToKit.get(slot);

            boolean leftClick = e.isLeftClick();
            boolean shiftClick = e.isShiftClick();
            boolean rightClick = e.isRightClick();

            if (leftClick && shiftClick) {
                sendMessage(p, "Hai iniziato a rinominare il kit: " + kit.getName());
            } else if (leftClick) {
                new EditMenu(kit).open(p);
            } else if (rightClick) {
                sendMessage(p,"Hai cliccato per eliminare il kit: " + kit.getName());
            }
        } else if (slot == 49) {
            handleKitCreation(p);
        }
    }

    void handleKitCreation(Player p){
        ChatPrompts.getInstance().ask(p, "Inserire il nome del Kit", (player, message) -> {
            kitManager.createKit(message, player);
            sendMessage(player, "<green>Kit '<yellow>" + message + "</yellow>' creato con successo!");
        });
    }
}
