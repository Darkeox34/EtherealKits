package gg.ethereallabs.heavenkits.kits;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

import static gg.ethereallabs.heavenkits.HeavenKits.formatRemainingTime;
import static gg.ethereallabs.heavenkits.HeavenKits.sendMessage;

public class KitManager {
    private final HashMap<String, KitTemplate> kits;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public KitManager() {
        kits = new HashMap<>();
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
        return true;
    }

    public void redeemKit(KitTemplate kit, Player p){
        if (!p.hasPermission(kit.getPermission())) {
            p.sendMessage(Component.text("Non hai il permesso per riscattare questo kit.").color(NamedTextColor.RED));
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(p.getName())) {
            long lastUsed = cooldowns.get(p.getName());
            if (currentTime - lastUsed < kit.getCooldown()) {
                long timeLeft = (kit.getCooldown() - (currentTime - lastUsed)) / 1000;
                p.sendMessage(Component.text("Devi aspettare " + formatRemainingTime(timeLeft) + " secondi prima di riscattare di nuovo questo kit."));
                return;
            }
        }

        for (ItemTemplate template : kit.getItems()) {
            ItemStack item = template.getItem().clone();
            item.setAmount(template.getQty());

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (template.getName() != null) {
                    meta.displayName(template.getName());
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
        sendMessage(p, Component.text("Hai riscattato il kit " + kit.getName() + "!"));
    }
}
