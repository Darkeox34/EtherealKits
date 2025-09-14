package gg.ethereallabs.heavenkits.kits.models;

import gg.ethereallabs.heavenkits.HeavenKits;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ItemTemplate {
    private ItemStack item;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private Component name;
    private int qty = 1;
    private List<Component> lore;

    private KitTemplate kit;

    public ItemTemplate(ItemStack item, Component name){
        this.item = item;
        this.name = name;
    }

    public Component getName(){
        return name;
    }

    public List<Component> getLore(){
        return lore;
    }

    public int getQty(){
        return qty;
    }

    public ItemStack getItem(){
        return item;
    }

    public Map<Enchantment, Integer> getEnchantments(){
        return enchantments;
    }

    public int getEnchantmentLevel(Enchantment enchantment){
        return this.enchantments.getOrDefault(enchantment, 0);
    }

    public KitTemplate getKit(){
        return kit;
    }

    public void setItem(ItemStack item){
        this.item = item;
    }

    public void addEnchantment(Enchantment enchantment, int level){
        this.enchantments.put(enchantment, level);
    }

    public void removeEnchantment(Enchantment enchantment){
        this.enchantments.remove(enchantment);
    }

    public void setName(Component name){
        this.name = name;
    }

    public void setQty(int qty){
        this.qty = qty;
    }

    public void setLore(List<Component> lore){
        this.lore = lore;
    }

    public void setKit(KitTemplate kit){
        this.kit = kit;
    }
}
