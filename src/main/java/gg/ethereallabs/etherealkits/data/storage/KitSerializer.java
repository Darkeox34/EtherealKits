package gg.ethereallabs.etherealkits.data.storage;

import gg.ethereallabs.etherealkits.kits.models.ItemTemplate;
import gg.ethereallabs.etherealkits.kits.models.KitTemplate;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class KitSerializer {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Document serializeKit(KitTemplate kit) {
        List<Document> items = kit.getItems().stream()
                .map(KitSerializer::serializeItem)
                .toList();

        List<String> kitLore = kit.getLore() != null
                ? kit.getLore().stream().map(miniMessage::serialize).toList()
                : null;

        return new Document("name", kit.getName())
                .append("display_name", miniMessage.serialize(kit.getDisplayName()))
                .append("display_material", kit.getDisplayMaterial().toString())
                .append("cooldown", kit.getCooldown())
                .append("permission", kit.getPermission())
                .append("kit_lore", kitLore)
                .append("items", items);
    }

    public static Map<String, Object> serializeKitToMap(KitTemplate kit) {
        List<Map<String, Object>> items = kit.getItems().stream()
                .map(KitSerializer::serializeItemToMap)
                .toList();

        Map<String, Object> map = new HashMap<>();
        map.put("name", kit.getName());
        map.put("display_name", miniMessage.serialize(kit.getDisplayName()));
        map.put("display_material", kit.getDisplayMaterial().toString());
        map.put("cooldown", kit.getCooldown());
        map.put("permission", kit.getPermission());
        List<String> kitLore = kit.getLore() != null
                ? kit.getLore().stream().map(miniMessage::serialize).toList()
                : null;
        map.put("kit_lore", kitLore);
        map.put("items", items);
        return map;
    }

    private static Document serializeItem(ItemTemplate item) {
        Map<String, Integer> enchants = item.getEnchantments().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getKey().toString(), Map.Entry::getValue));

        List<String> lore = item.getLore() != null
                ? item.getLore().stream().map(miniMessage::serialize).toList()
                : null;

        return new Document("material", item.getItem().getType().toString())
                .append("name", miniMessage.serialize(item.getName()))
                .append("qty", item.getQty())
                .append("lore", lore)
                .append("enchantments", enchants);
    }

    private static Map<String, Object> serializeItemToMap(ItemTemplate item) {
        Map<String, Integer> enchants = item.getEnchantments().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getKey().toString(), Map.Entry::getValue));

        List<String> lore = item.getLore() != null
                ? item.getLore().stream().map(miniMessage::serialize).toList()
                : null;

        Map<String, Object> map = new HashMap<>();
        map.put("material", item.getItem().getType().toString());
        map.put("name", miniMessage.serialize(item.getName()));
        map.put("qty", item.getQty());
        map.put("lore", lore);
        map.put("enchantments", enchants);
        return map;
    }

    // ----------------- DESERIALIZE -----------------
    public static KitTemplate deserializeKit(Document doc) {
        String name = doc.getString("name");
        KitTemplate kit = new KitTemplate(name);

        if (doc.containsKey("display_name")) {
            kit.setDisplayName(miniMessage.deserialize(doc.getString("display_name")));
        }

        if (doc.containsKey("display_material")) {
            kit.setDisplayMaterial(Material.valueOf(doc.getString("display_material")));
        }

        kit.setCooldown(doc.getLong("cooldown"));
        kit.setPermission(doc.getString("permission"));

        Object kitLoreObj = doc.get("kit_lore");
        if (kitLoreObj instanceof List<?> rawLore) {
            List<Component> lore = rawLore.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(miniMessage::deserialize)
                    .toList();
            kit.setLore(lore);
        }

        Object itemsObj = doc.get("items");
        if (itemsObj instanceof List<?> rawItems) {
            for (Object o : rawItems) {
                if (o instanceof Document itemDoc) {
                    kit.addItem(deserializeItem(itemDoc));
                }
            }
        }

        return kit;
    }

    public static KitTemplate deserializeKitFromMap(Map<String, Object> map) {
        String name = (String) map.get("name");
        KitTemplate kit = new KitTemplate(name);

        Object dn = map.get("display_name");
        if (dn instanceof String s) {
            kit.setDisplayName(miniMessage.deserialize(s));
        }

        Object dm = map.get("display_material");
        if (dm instanceof String s) {
            kit.setDisplayMaterial(Material.valueOf(s));
        }

        Object cd = map.get("cooldown");
        if (cd instanceof Number n) {
            kit.setCooldown(n.longValue());
        }

        Object perm = map.get("permission");
        if (perm instanceof String s) {
            kit.setPermission(s);
        }

        Object kitLoreObj = map.get("kit_lore");
        if (kitLoreObj instanceof List<?> rawLore) {
            List<Component> lore = new ArrayList<>();
            for (Object el : rawLore) {
                if (el instanceof String s) {
                    lore.add(miniMessage.deserialize(s));
                }
            }
            kit.setLore(lore);
        }

        Object itemsObj = map.get("items");
        if (itemsObj instanceof List<?> rawList) {
            for (Object o : rawList) {
                if (o instanceof Map<?, ?> itemMapRaw) {
                    Map<String, Object> itemMap = new HashMap<>();
                    for (Map.Entry<?, ?> e : itemMapRaw.entrySet()) {
                        if (e.getKey() instanceof String key) {
                            itemMap.put(key, e.getValue());
                        }
                    }
                    kit.addItem(deserializeItemFromMap(itemMap));
                }
            }
        }

        return kit;
    }

    private static ItemTemplate deserializeItem(Document doc) {
        Material mat = Material.valueOf(doc.getString("material"));
        Component name = miniMessage.deserialize(doc.getString("name"));
        ItemTemplate item = new ItemTemplate(new ItemStack(mat), name);

        item.setQty(doc.getInteger("qty", 1));

        Object loreObj = doc.get("lore");
        if (loreObj instanceof List<?> loreRaw) {
            List<Component> lore = loreRaw.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(miniMessage::deserialize)
                    .toList();
            item.setLore(lore);
        }

        Object enchantsObj = doc.get("enchantments");
        if (enchantsObj instanceof Document enchantsDoc) {
            for (Map.Entry<String, Object> entry : enchantsDoc.entrySet()) {
                NamespacedKey key = NamespacedKey.fromString(entry.getKey());
                if (key != null) {
                    Enchantment enchant = RegistryAccess.registryAccess()
                            .getRegistry(RegistryKey.ENCHANTMENT)
                            .get(key);
                    if (enchant != null && entry.getValue() instanceof Number n) {
                        item.addEnchantment(enchant, n.intValue());
                    }
                }
            }
        }

        return item;
    }

    private static ItemTemplate deserializeItemFromMap(Map<String, Object> map) {
        Material mat = Material.valueOf((String) map.get("material"));
        Component name = miniMessage.deserialize((String) map.get("name"));
        ItemTemplate item = new ItemTemplate(new ItemStack(mat), name);

        Object qtyObj = map.get("qty");
        item.setQty(qtyObj instanceof Number n ? n.intValue() : 1);

        Object loreObj = map.get("lore");
        if (loreObj instanceof List<?> loreRaw) {
            List<Component> lore = new ArrayList<>();
            for (Object el : loreRaw) {
                if (el instanceof String s) {
                    lore.add(miniMessage.deserialize(s));
                }
            }
            item.setLore(lore);
        }

        Object enchantsObj = map.get("enchantments");
        if (enchantsObj instanceof Map<?, ?> enchMapRaw) {
            for (Map.Entry<?, ?> entry : enchMapRaw.entrySet()) {
                if (entry.getKey() instanceof String key && entry.getValue() instanceof Number n) {
                    NamespacedKey ns = NamespacedKey.fromString(key);
                    if (ns != null) {
                        Enchantment enchant = RegistryAccess.registryAccess()
                                .getRegistry(RegistryKey.ENCHANTMENT)
                                .get(ns);
                        if (enchant != null) {
                            item.addEnchantment(enchant, n.intValue());
                        }
                    }
                }
            }
        }

        return item;
    }
}
