package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
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

public class KitsMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    private final Map<Integer, KitTemplate> slotToKit = new HashMap<>();

    public KitsMenu() {
        super("Kits", 45);
    }

    @Override
    public void draw(Player p) {
        inv.clear();
        slotToKit.clear();

        HashMap<String, KitTemplate> kits = HeavenKits.kitManager.getKits();

        int i = 0;
        for (Map.Entry<String, KitTemplate> entry : kits.entrySet()) {
            if (i >= slotsList.size()) break;

            KitTemplate kit = entry.getValue();
            Component displayName = kit.getDisplayName().decoration(TextDecoration.ITALIC, false);
            List<Component> lore = getComponents(p, entry);

            int slot = slotsList.get(i);

            Material displayMaterial = p.hasPermission(kit.getPermission()) ? kit.getDisplayMaterial() : Material.BARRIER;

            inv.setItem(slotsList.get(i), createItem(displayName, displayMaterial, lore, 1));
            slotToKit.put(slot, kit);

            i++;
        }
    }

    private static @NotNull List<Component> getComponents(Player player, Map.Entry<String, KitTemplate> entry) {
        KitTemplate kit = entry.getValue();

        List<Component> components = new ArrayList<>();

        components.add(mm.deserialize("<gray>(Tasto Sinistro)<green> Riscuoti").decoration(TextDecoration.ITALIC, false));
        components.add(mm.deserialize("<gray>(Tasto Destro)<yellow> Vedi").decoration(TextDecoration.ITALIC, false));
        components.add(Component.empty());

        if (!player.hasPermission(kit.getPermission())) {
            components.add(mm.deserialize("<red>Non hai il permesso di riscuotere questo Kit!\n Visita il nostro store per scoprire i nostri pacchetti!")
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            if (kit.getCooldown() == 0) {
                components.add(mm.deserialize("<green>Riscuoti Ora!").decoration(TextDecoration.ITALIC, false));
            } else {
                components.add(mm.deserialize("<yellow>Cooldown: " + formatRemainingTime(kit.getCooldown()))
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        return components;
    }

    @Override
    public void handleClick(Player p, int slot, InventoryClickEvent e) {
        if (slotToKit.containsKey(slot)) {
            KitTemplate kit = slotToKit.get(slot);

            boolean leftClick = e.isLeftClick();
            boolean shiftClick = e.isShiftClick();
            boolean rightClick = e.isRightClick();

            if (leftClick) {
                kitManager.redeemKit(kit, p);
            } else if (rightClick) {
                new ViewKitMenu(kit).open(p);
            }
        }
    }
}
