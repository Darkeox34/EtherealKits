package gg.ethereallabs.etherealkits.kits;

import gg.ethereallabs.etherealkits.EtherealKits;
import gg.ethereallabs.etherealkits.data.Storage;
import gg.ethereallabs.etherealkits.kits.models.ItemTemplate;
import gg.ethereallabs.etherealkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static gg.ethereallabs.etherealkits.EtherealKits.*;

public class KitManager {
    private final HashMap<String, KitTemplate> kits;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final Storage storage;

    public KitManager(Storage storage) {
        kits = new HashMap<>();
        this.storage = storage;
    }

    public void loadAllKits() {
        storage.loadAllKits(kits);
    }

    public void updatePlayerCooldown(String playerUUID, String kitName, long redeemTime) {
        CompletableFuture.runAsync(() -> storage.updatePlayerCooldown(playerUUID, kitName, redeemTime));
    }

    public void loadCooldowns(Player p) {
        CompletableFuture.runAsync(() -> {
            Map<String, Long> playerCooldowns = storage.loadPlayerCooldowns(p.getUniqueId());
            if (playerCooldowns == null || playerCooldowns.isEmpty()) return;

            Map<String, Long> cooldownMap = new HashMap<>();
            long currentTime = System.currentTimeMillis();

            for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
                Long redeemTime = entry.getValue();
                String kitName = entry.getKey();
                if (redeemTime != null) {
                    if (redeemTime > currentTime) {
                        cooldownMap.put(kitName, redeemTime);
                    } else {
                        removeExpiredCooldown(p.getUniqueId().toString(), kitName);
                    }
                }
            }

            if (!cooldownMap.isEmpty()) {
                cooldowns.put(p.getUniqueId(), cooldownMap);
            }
        });
    }

    private void removeExpiredCooldown(String playerUUID, String kitName) {
        CompletableFuture.runAsync(() -> storage.removeExpiredCooldown(playerUUID, kitName));
    }

    public void cleanExpiredCooldowns() {
        CompletableFuture.runAsync(storage::cleanExpiredCooldowns);
    }

    public HashMap<String, KitTemplate> getKits() {
        return kits;
    }

    public KitTemplate getKit(String kitName) {
        return kits.get(kitName);
    }

    public boolean createKit(String name, CommandSender sender) {
        if (EtherealKits.getInstance().getKitManager().getKit(name) != null) {
            sendMessage(sender, "<red>A kit with the name '<yellow>" + name + "</yellow>' already exists!");
            return false;
        }

        KitTemplate newTemplate = new KitTemplate(name);

        kits.put(name, newTemplate);
        CompletableFuture.runAsync(() -> storage.insertKit(newTemplate));
        return true;
    }
    public void listKits(CommandSender sender) {
        sendMessage(sender, "Available Kits: ");
        kits.forEach((name, template) -> {
            Component kitComponent = Component.text("- ")
                    .append(template.getDisplayName()
                            .hoverEvent(HoverEvent.showText(Component.text("Click to edit this kit")))
                            .clickEvent(ClickEvent.runCommand("/hk admin edit " + template.getName())));

            sender.sendMessage(kitComponent);
        });
    }

    public void deleteKit(String name){
        kits.remove(name);
        CompletableFuture.runAsync(() -> storage.deleteKit(name));
    }

    public void updateKit(KitTemplate kit) {
        CompletableFuture.runAsync(() -> storage.replaceKit(kit));
    }

    public boolean renameKit(String name, String newName, CommandSender sender){
        if (EtherealKits.getInstance().getKitManager().getKit(name) == null) {
            sendMessage(sender, "<red>Kit '<yellow>" + name + "</yellow>' not found!");
            return false;
        }

        if (EtherealKits.getInstance().getKitManager().getKit(newName) != null) {
            sendMessage(sender, "<red>A kit with the name '<yellow>" + newName + "</yellow>' already exists!");
            return false;
        }

        KitTemplate temp = kits.remove(name);
        temp.setName(newName);
        kits.put(newName, temp);
        // Persist rename: remove old record, then insert the new one
        CompletableFuture.runAsync(() -> {
            storage.deleteKit(name);
            storage.insertKit(temp);
        });
        return true;
    }

    public void redeemKit(KitTemplate kit, Player p) {
        if (!p.hasPermission(kit.getPermission())) {
            p.sendMessage(Component.text("You don't have permission to redeem this kit.").color(NamedTextColor.RED));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        UUID uuid = p.getUniqueId();
        long currentTime = System.currentTimeMillis();

        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());

        Long cooldownUntil = playerCooldowns.get(kit.getName());
        if (cooldownUntil != null && currentTime < cooldownUntil && !p.hasPermission("hk.cooldown.bypass")) {
            long timeLeft = cooldownUntil - currentTime;
            p.sendMessage(mm.deserialize(
                    "<red>You must wait <yellow>" + formatRemainingTime(timeLeft) + "<red> before redeeming this kit again."
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        int neededSlots = kit.getItems().size();
        int emptySlots = 0;

        ItemStack[] mainInventory = p.getInventory().getStorageContents();
        for (ItemStack slot : mainInventory) {
            if (slot == null || slot.getType().isAir()) {
                emptySlots++;
            }
        }

        if (emptySlots < neededSlots) {
            sendMessage(p, mm.deserialize("Your inventory is full, free at least " + (neededSlots - emptySlots) + " slots!").color(NamedTextColor.RED));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        for (ItemTemplate template : kit.getItems()) {
            ItemStack item = template.getItem().clone();
            item.setAmount(template.getQty());

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (template.getName() != null) {
                    meta.displayName(template.getName().decoration(TextDecoration.ITALIC, false));
                }
                if (template.getLore() != null && !template.getLore().isEmpty()) {
                    meta.lore(template.getLore());
                }
                item.setItemMeta(meta);
            }

            for (Map.Entry<Enchantment, Integer> entry : template.getEnchantments().entrySet()) {
                item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }

            p.getInventory().addItem(item);
        }

        if (!p.hasPermission("hk.cooldown.bypass")) {
            long newCooldownUntil = currentTime + kit.getCooldown();
            playerCooldowns.put(kit.getName(), newCooldownUntil);
            EtherealKits.getInstance().getKitManager().updatePlayerCooldown(uuid.toString(), kit.getName(), newCooldownUntil);
        }

        sendMessage(p, mm.deserialize("<yellow>You redeemed the kit</yellow> ")
                .append(kit.getDisplayName())
                .append(Component.text("!")));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }



    public Map<UUID, Map<String, Long>> getCooldowns() {
        return cooldowns;
    }
}
