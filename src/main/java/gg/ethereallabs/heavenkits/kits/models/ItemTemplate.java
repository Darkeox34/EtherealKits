package gg.ethereallabs.heavenkits.kits.models;

import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;

public class ItemTemplate {
    ItemStack item;
    HashSet<Enchantment> enchantments;
    String name;
    int qty;
    List<Component> lore;

    public String getName(){
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
    public HashSet<Enchantment> getEnchantments(){
        return enchantments;
    }

    public void setItem(ItemStack item){
        this.item = item;
    }

    public void setEnchantments(Enchantment enchantments){
        this.enchantments.add(enchantments);
    }

    public void setName(String name){
        this.name = name;
    }

    public void setQty(int qty){
        this.qty = qty;
    }

    public void setLore(List<Component> lore){
        this.lore = lore;
    }
}
