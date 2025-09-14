package gg.ethereallabs.heavenkits.gui;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

import static gg.ethereallabs.heavenkits.HeavenKits.mm;

public class EnchantmentsMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    ItemTemplate item;
    private final Map<Integer, Enchantment> slotToEnchant = new HashMap<>();

    public EnchantmentsMenu(ItemTemplate item) {
        super("&bModificando " + item.getName() + " enchantments", 54);
        this.item = item;
    }

    @Override
    public void draw(Player p) {
        inv.clear();
        slotToEnchant.clear();
        int i = 0;

        List<Enchantment> sortedEnchants = Arrays.stream(Enchantment.values())
                .sorted(Comparator.comparing(enchant -> enchant.getKey().getKey()))
                .toList();

        for (Enchantment enchant : sortedEnchants) {
            if (i >= slotsList.size()) break;

            ItemStack material;
            Component name;
            if(item.getEnchantments().containsKey(enchant)) {
                material = new ItemStack(Material.ENCHANTED_BOOK);
                name = mm.deserialize("<yellow>(Selezionato) <purple>" + enchant.getKey().getKey().toUpperCase() + " <orange>(" + item.getEnchantmentLevel(enchant) + ")");
            }
            else {
                material = new ItemStack(Material.BOOK);
                name = mm.deserialize("<yellow>" + enchant.getKey().getKey().toUpperCase());
            }

            ItemMeta meta = material.getItemMeta();
            if (meta != null) {
                meta.customName(name);
                material.setItemMeta(meta);
            }
            int slot = slotsList.get(i);
            slotToEnchant.put(slot, enchant);
            inv.setItem(slotsList.get(i), material);
            i++;
        }
    }

    @Override
    public void handleClick(Player p, int slot, InventoryClickEvent e) {
        if (slotToEnchant.containsKey(slot)) {
            Enchantment enchantment = slotToEnchant.get(slot);

            boolean leftClick = e.isLeftClick();
            boolean shiftClick = e.isShiftClick();
            boolean rightClick = e.isRightClick();

            if(leftClick){
                if(!item.getEnchantments().containsKey(enchantment))
                    handleAddEnchant(p, enchantment);
                else {
                    item.removeEnchantment(enchantment);
                    new EnchantmentsMenu(item).open(p);
                }
            }
        }
    }

    void handleAddEnchant(Player p, Enchantment enchantment) {
        ChatPrompts.getInstance().ask(p, "Inserire il livello di "
                + enchantment.getKey().getKey() +
                "(max. " + enchantment.getMaxLevel() + ") ", (player, message) -> {
            if (item == null) return;

            int level;
            try {
                level = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                HeavenKits.sendMessage(player, "Valore non valido! Inserisci un numero.");
                new EnchantmentsMenu(item).open(player);
                return;
            }

            if (level < 1) level = 1;
            if (level > enchantment.getMaxLevel()) level = enchantment.getMaxLevel();

            item.addEnchantment(enchantment, level);

            new EnchantmentsMenu(item).open(player);
        });
    }
}
