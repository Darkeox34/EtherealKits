package gg.ethereallabs.heavenkits.kits;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

import static gg.ethereallabs.heavenkits.HeavenKits.sendMessage;

public class KitManager {
    private final HashMap<String, KitTemplate> kits;

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

        kits.put(newName, temp);
        return true;
    }
}
