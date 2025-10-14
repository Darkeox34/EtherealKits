package gg.ethereallabs.etherealkits.gui.admin;

import gg.ethereallabs.etherealkits.EtherealKits;
import gg.ethereallabs.etherealkits.gui.models.BaseMenu;
import gg.ethereallabs.etherealkits.gui.models.ChatPrompts;
import gg.ethereallabs.etherealkits.kits.models.ItemTemplate;
import gg.ethereallabs.etherealkits.kits.models.KitTemplate;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.IntStream;

import static gg.ethereallabs.etherealkits.EtherealKits.mm;

public class EnchantmentsMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    ItemTemplate item;
    KitTemplate kit;
    private final Map<Integer, Enchantment> slotToEnchant = new HashMap<>();

    public EnchantmentsMenu(ItemTemplate item, KitTemplate kit) {
        super(Component.text("Editing ").color(NamedTextColor.AQUA)
                .append(item.getName().color(NamedTextColor.YELLOW)),54);
        this.item = item;
        this.kit = kit;
    }

    @Override
    public void draw(Player p) {
        inv.clear();
        slotToEnchant.clear();
        int i = 0;

        List<Enchantment> sortedEnchants = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .stream()
                .sorted(Comparator.comparing(enchant -> enchant.getKey().getKey()))
                .toList();

        for (Enchantment enchant : sortedEnchants) {
            if (i >= slotsList.size()) break;

            ItemStack material;
            Component name;
            if(item.getEnchantments().containsKey(enchant)) {
                material = new ItemStack(Material.ENCHANTED_BOOK);
                name = mm.deserialize(
                        "<yellow>(Selected)</yellow> " +
                                "<red>" + enchant.getKey().getKey().toUpperCase() + "</red> " +
                                "<red>(" + item.getEnchantmentLevel(enchant) + ")</red>"
                ).decoration(TextDecoration.ITALIC, false);

            }
            else {
                material = new ItemStack(Material.BOOK);
                name = mm.deserialize("<yellow>" + enchant.getKey().getKey().toUpperCase()).decoration(TextDecoration.ITALIC, false);
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

        inv.setItem(53, createItem(Component.text("Go back").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED), Material.RED_STAINED_GLASS_PANE, Collections.emptyList(), 1));
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
                    new EnchantmentsMenu(item, kit).open(p);
                }
            }
        }
        else if(slot == 53){
            new EditMenu(kit).open(p);
        }
    }

    void handleAddEnchant(Player p, Enchantment enchantment) {
        ChatPrompts.getInstance().ask(p, "Enter the level for "
                + enchantment.getKey().getKey() +
                " (max. " + enchantment.getMaxLevel() + "): ", (player, message) -> {
            if (item == null) return;

            int level;
            try {
                level = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                EtherealKits.sendMessage(player, "Invalid value! Enter a number.");
                new EnchantmentsMenu(item, kit).open(player);
                return;
            }

            if (level < 1) level = 1;
            if (level > enchantment.getMaxLevel()) level = enchantment.getMaxLevel();

            item.addEnchantment(enchantment, level);
            EtherealKits.getInstance().getKitManager().updateKit(kit);
            new EnchantmentsMenu(item, kit).open(player);
        });
    }
}
