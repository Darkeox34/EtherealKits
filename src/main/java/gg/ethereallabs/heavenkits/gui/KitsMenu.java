package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.KitManager;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;
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
            long timeLeft = getTimeLeft(p, kit);
            List<Component> lore = getComponents(p, kit, timeLeft);

            int slot = slotsList.get(i);

            Material displayMaterial = p.hasPermission(kit.getPermission()) ? kit.getDisplayMaterial() : Material.BARRIER;

            inv.setItem(slotsList.get(i), createItem(displayName, displayMaterial, lore, 1));
            slotToKit.put(slot, kit);

            i++;
        }

        startCooldownUpdater(p);
    }

    private long getTimeLeft(Player player, KitTemplate kit) {
        if (kitManager.getCooldowns().containsKey(player.getName())) {
            long lastUsed = kitManager.getCooldowns().get(player.getName());
            long currentTime = System.currentTimeMillis();
            return Math.max(0, kit.getCooldown() - (currentTime - lastUsed));
        }
        return 0;
    }

    private void startCooldownUpdater(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Component expectedTitle = Component.text("Kits");
                if (!player.getOpenInventory().title().equals(expectedTitle)) {
                    cancel();
                    return;
                }

                for (Map.Entry<Integer, KitTemplate> entry : slotToKit.entrySet()) {
                    int slot = entry.getKey();
                    KitTemplate kit = entry.getValue();

                    long timeLeft = getTimeLeft(player, kit);
                    List<Component> lore = getComponents(player, kit, timeLeft);

                    inv.setItem(slot, createItem(
                            kit.getDisplayName().decoration(TextDecoration.ITALIC, false),
                            player.hasPermission(kit.getPermission()) ? kit.getDisplayMaterial() : Material.BARRIER,
                            lore,
                            1
                    ));
                }

                player.updateInventory();
            }
        }.runTaskTimer(HeavenKits.instance, 0L, 20L);
    }

    private static @NotNull List<Component> getComponents(Player player, KitTemplate kit, long timeLeftMs) {
        List<Component> components = new ArrayList<>();
        components.add(mm.deserialize("<gray>(Tasto Sinistro)<green> Riscuoti").decoration(TextDecoration.ITALIC, false));
        components.add(mm.deserialize("<gray>(Tasto Destro)<yellow> Vedi").decoration(TextDecoration.ITALIC, false));
        components.add(Component.empty());

        if (!player.hasPermission(kit.getPermission())) {
            components.add(mm.deserialize("<red>Non hai il permesso di riscuotere questo Kit!")
                    .decoration(TextDecoration.ITALIC, false));
            components.add(mm.deserialize("Visita il nostro store per scoprire i nostri pacchetti!")
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            if (timeLeftMs <= 0) {
                components.add(mm.deserialize("<green>Riscuoti Ora!").decoration(TextDecoration.ITALIC, false));
            } else {
                components.add(mm.deserialize("<yellow>Cooldown: " + formatRemainingTime(timeLeftMs))
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
                p.closeInventory();
            } else if (rightClick) {
                new ViewKitMenu(kit).open(p);
            }
        }
    }
}
