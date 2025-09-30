package gg.ethereallabs.heavenkits.kits;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.data.KitSerializer;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bson.Document;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static gg.ethereallabs.heavenkits.HeavenKits.*;

public class KitManager {
    private final HashMap<String, KitTemplate> kits;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    private final MongoCollection<Document> collection;

    public KitManager() {
        kits = new HashMap<>();
        this.collection = HeavenKits.getInstance().getMongo().getKitCollection();
    }

    public void loadAllKits() {
        Document filter = new Document("_id", new Document("$ne", "cooldowns"));

        for (Document doc : collection.find(filter)) {
            if (doc.containsKey("name") && doc.getString("name") != null) {
                KitTemplate kit = KitSerializer.deserializeKit(doc);
                kits.put(kit.getName(), kit);
            }
        }
    }

    public void updatePlayerCooldown(String playerUUID, String kitName, long redeemTime) {
        CompletableFuture.runAsync(() -> {
            Document query = new Document("_id", "cooldowns");
            Document update = new Document("$set",
                    new Document("players." + playerUUID + "." + kitName, redeemTime));

            UpdateOptions options = new UpdateOptions().upsert(true);
            collection.updateOne(query, update, options);
        });
    }

    public void loadCooldowns(Player p) {
        CompletableFuture.runAsync(() -> {
            Document cooldownsDoc = collection.find(new Document("_id", "cooldowns")).first();
            if (cooldownsDoc == null) return;

            Document players = cooldownsDoc.get("players", Document.class);
            if (players == null) return;

            String uuidString = p.getUniqueId().toString();
            Document playerCooldowns = players.get(uuidString, Document.class);
            if (playerCooldowns == null) return;

            Map<String, Long> cooldownMap = new HashMap<>();
            long currentTime = System.currentTimeMillis();

            for (String kitName : playerCooldowns.keySet()) {
                Long redeemTime = playerCooldowns.getLong(kitName);
                if (redeemTime != null) {
                    if (redeemTime > currentTime) {
                        cooldownMap.put(kitName, redeemTime);
                    } else {
                        removeExpiredCooldown(uuidString, kitName);
                    }
                }
            }

            if (!cooldownMap.isEmpty()) {
                cooldowns.put(p.getUniqueId(), cooldownMap);
            }
        });
    }

    private void removeExpiredCooldown(String playerUUID, String kitName) {
        CompletableFuture.runAsync(() -> {
            Document query = new Document("_id", "cooldowns");
            Document update = new Document("$unset",
                    new Document("players." + playerUUID + "." + kitName, ""));

            collection.updateOne(query, update);
        });
    }

    public void cleanExpiredCooldowns() {
        CompletableFuture.runAsync(() -> {
            Document cooldownsDoc = collection.find(new Document("_id", "cooldowns")).first();
            if (cooldownsDoc == null) return;

            Document players = cooldownsDoc.get("players", Document.class);
            if (players == null) return;

            for (String uuidString : players.keySet()) {
                Document playerCooldowns = players.get(uuidString, Document.class);
                List<String> toRemove = new ArrayList<>();

                for (String kitName : playerCooldowns.keySet()) {
                    long redeemTime = playerCooldowns.getLong(kitName);
                    if (redeemTime < System.currentTimeMillis()) {
                        toRemove.add(kitName);
                    }
                }

                if (!toRemove.isEmpty()) {
                    Document update = new Document();
                    for (String kitName : toRemove) {
                        update.put("players." + uuidString + "." + kitName, "");
                    }
                    collection.updateOne(
                            new Document("_id", "cooldowns"),
                            new Document("$unset", update)
                    );
                }
            }
        });
    }

    public HashMap<String, KitTemplate> getKits() {
        return kits;
    }

    public KitTemplate getKit(String kitName) {
        return kits.get(kitName);
    }

    public boolean createKit(String name, CommandSender sender) {
        if (HeavenKits.getInstance().getKitManager().getKit(name) != null) {
            sendMessage(sender, "<red>A kit with the name '<yellow>" + name + "</yellow>' already exists!");
            return false;
        }

        KitTemplate newTemplate = new KitTemplate(name);

        kits.put(name, newTemplate);
        CompletableFuture.runAsync(() -> {
            Document doc = KitSerializer.serializeKit(newTemplate);
            collection.insertOne(doc);
        });
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
        CompletableFuture.runAsync(() -> {
            collection.deleteOne(new Document("name", name));
        });
    }

    public void updateKit(KitTemplate kit) {
        CompletableFuture.runAsync(() -> {
            Document query = new Document("name", kit.getName());
            Document updatedDoc = KitSerializer.serializeKit(kit);

            collection.replaceOne(query, updatedDoc);
        });
    }

    public boolean renameKit(String name, String newName, CommandSender sender){
        if (HeavenKits.getInstance().getKitManager().getKit(name) == null) {
            sendMessage(sender, "<red>Kit '<yellow>" + name + "</yellow>' not found!");
            return false;
        }

        if (HeavenKits.getInstance().getKitManager().getKit(newName) != null) {
            sendMessage(sender, "<red>A kit with the name '<yellow>" + newName + "</yellow>' already exists!");
            return false;
        }

        KitTemplate temp = kits.remove(name);
        temp.setName(newName);
        kits.put(newName, temp);
        updateKit(temp);
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
            HeavenKits.getInstance().getKitManager().updatePlayerCooldown(uuid.toString(), kit.getName(), newCooldownUntil);
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
