package gg.ethereallabs.etherealkits.gui;

import gg.ethereallabs.etherealkits.EtherealKits;
import gg.ethereallabs.etherealkits.gui.models.BaseMenu;
import gg.ethereallabs.etherealkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

import static gg.ethereallabs.etherealkits.EtherealKits.*;

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

        HashMap<String, KitTemplate> kits = EtherealKits.getInstance().getKitManager().getKits();

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
        UUID uuid = player.getUniqueId();

        Map<String, Long> playerCooldowns = EtherealKits.getInstance().getKitManager().getCooldowns().get(uuid);
        if (playerCooldowns == null) return 0;

        Long cooldownUntil = playerCooldowns.get(kit.getName());
        if (cooldownUntil == null) return 0;

        long currentTime = System.currentTimeMillis();
        return Math.max(0, cooldownUntil - currentTime);
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
        }.runTaskTimer(EtherealKits.getInstance(), 0L, 20L);
    }

    private static @NotNull List<Component> getComponents(Player player, KitTemplate kit, long timeLeftMs) {
        List<Component> components = new ArrayList<>(kit.getLore());
        components.add(Component.empty());
        components.add(mm.deserialize("<gray>(Left Click)<green> Redeem").decoration(TextDecoration.ITALIC, false));
        components.add(mm.deserialize("<gray>(Right Click)<yellow> View").decoration(TextDecoration.ITALIC, false));
        components.add(Component.empty());

        if (!player.hasPermission(kit.getPermission())) {
            components.add(mm.deserialize("<red>You don't have permission to redeem this Kit!")
                    .decoration(TextDecoration.ITALIC, false));
            components.add(mm.deserialize("Visit our store to discover our packages!").color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            if (timeLeftMs <= 0 || player.hasPermission("hk.cooldown.bypass")) {
                components.add(mm.deserialize("<green>Redeem Now!").decoration(TextDecoration.ITALIC, false));
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
                EtherealKits.getInstance().getKitManager().redeemKit(kit, p);
                p.closeInventory();
            } else if (rightClick) {
                new ViewKitMenu(kit).open(p);
            }
        }
    }
}
