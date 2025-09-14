package gg.ethereallabs.heavenkits.kits;

import com.mongodb.client.MongoCollection;
import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.data.KitSerializer;
import gg.ethereallabs.heavenkits.data.MongoDB;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bson.Document;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

import static gg.ethereallabs.heavenkits.HeavenKits.*;

public class KitManager {
    private final HashMap<String, KitTemplate> kits;
    private final Map<String, Long> cooldowns = new HashMap<>();

    private final MongoCollection<Document> collection;

    public KitManager() {
        kits = new HashMap<>();
        this.collection = mongo.getKitCollection();
    }

    public void loadAllKits() {
        for (Document doc : collection.find()) {
            KitTemplate kit = KitSerializer.deserializeKit(doc);
            kits.put(kit.getName(), kit);
        }
    }

    public void updatePlayerCooldown(String playerUUID, String kitName, long cooldownUntil) {
        Document query = new Document("player_uuid", playerUUID)
                .append("kits.name", kitName);

        Document update = new Document("$set",
                new Document("kits.$.cooldown_until", cooldownUntil));

        collection.updateOne(query, update);
    }

    public HashMap<String, KitTemplate> getKits() {
        return kits;
    }

    public KitTemplate getKit(String kitName) {
        return kits.get(kitName);
    }

    public boolean createKit(String name, CommandSender sender) {
        if (HeavenKits.kitManager.getKit(name) != null) {
            sendMessage(sender, "<red>Un kit con il nome '<yellow>" + name + "</yellow>' esiste già!");
            return false;
        }

        KitTemplate newTemplate = new KitTemplate(name);

        kits.put(name, newTemplate);
        Document doc = KitSerializer.serializeKit(newTemplate);
        collection.insertOne(doc);
        return true;
    }

    public void listKits(CommandSender sender){
        sendMessage(sender, "Available Kits: ");
        kits.forEach((name, template) -> {
            sender.sendMessage("- " + name);
        });
    }

    public void deleteKit(String name){
        kits.remove(name);
        collection.deleteOne(new Document("name", name));
    }

    public void updateKit(KitTemplate kit) {
        Document query = new Document("name", kit.getName());
        Document updatedDoc = KitSerializer.serializeKit(kit);

        collection.replaceOne(query, updatedDoc);
    }

    public boolean renameKit(String name, String newName, CommandSender sender){
        if (HeavenKits.kitManager.getKit(name) == null) {
            sendMessage(sender, "<red>Kit '<yellow>" + name + "</yellow>' non trovato!");
            return false;
        }

        if (HeavenKits.kitManager.getKit(newName) != null) {
            sendMessage(sender, "<red>Un kit con il nome '<yellow>" + newName + "</yellow>' esiste già!");
            return false;
        }

        KitTemplate temp = kits.remove(name);
        temp.setName(newName);
        kits.put(newName, temp);
        updateKit(temp);
        return true;
    }

    public void redeemKit(KitTemplate kit, Player p){
        if (!p.hasPermission(kit.getPermission())) {
            p.sendMessage(Component.text("Non hai il permesso per riscattare questo kit.").color(NamedTextColor.RED));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(p.getName())) {
            long lastUsed = cooldowns.get(p.getName());
            if (currentTime - lastUsed < kit.getCooldown()) {
                long timeLeft = kit.getCooldown() - (currentTime - lastUsed);
                p.sendMessage(mm.deserialize(
                        "<red>Devi aspettare <yellow>" + formatRemainingTime(timeLeft) + "<red> prima di riscattare di nuovo questo kit."
                ));
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
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

        cooldowns.put(p.getName(), currentTime);
        sendMessage(p, mm.deserialize("<yellow>Hai riscattato il kit</yellow> " + kit.getDisplayName() + "!"));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }
}
