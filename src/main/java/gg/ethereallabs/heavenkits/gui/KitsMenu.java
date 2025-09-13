package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KitsMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    public KitsMenu() {
        super("Kits", 54);
    }

    @Override
    public void draw(Player p) {
        inv.clear();
        HashMap<String, KitTemplate> kits = HeavenKits.kitManager.getKits();

        int i = 0;
        for (Map.Entry<String, KitTemplate> entry : kits.entrySet()) {
            if (i >= slotsList.size()) {
                break;
            }
            String name = entry.getKey();
            List<Component> lore = getComponents(entry);

            inv.setItem(slotsList.get(i), createItem(name, Material.BOOK, lore));
            i++;
        }


    }

    private static @NotNull List<Component> getComponents(Map.Entry<String, KitTemplate> entry) {
        KitTemplate kit = entry.getValue();

        return List.of(
                Component.text("<gray>Left-Click: Edit"),
                Component.text("<gray>Right-Click: Delete"),
                Component.text(""),
                Component.text("<yellow>Description: " + kit.getDescription()),
                Component.text("<yellow>Cooldown: " + kit.getCooldown()),
                Component.text("<yellow>Permission: " + kit.getPermission())
        );
    }

    @Override
    public void handleClick(Player p, int slot, Triple<Boolean, Boolean, Boolean> clickFlags) {

    }
}
