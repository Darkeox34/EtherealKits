package gg.ethereallabs.heavenkits.kits;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

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

    public void createKit(String name){
        KitTemplate newTemplate = new KitTemplate(name);

        kits.put(name, newTemplate);
    }

    public void listKits(CommandSender sender){
        HeavenKits.sendMessage(sender, "Available Kits: ");
        kits.forEach((name, template) -> {
            sender.sendMessage("- " + name);
        });
    }

    public void deleteKit(String name){
        kits.remove(name);
    }

    public void renameKit(String name, String newName){
        KitTemplate temp = kits.remove(name);

        kits.put(newName, temp);
    }
}
