package gg.ethereallabs.heavenkits.data;

import gg.ethereallabs.heavenkits.kits.models.ItemTemplate;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitSerializer {
    private static final GsonComponentSerializer gson = GsonComponentSerializer.gson();

    public static Document serializeKit(KitTemplate kit) {
        List<Document> items = kit.getItems().stream()
                .map(KitSerializer::serializeItem)
                .toList();

        return new Document("name", kit.getName())
                .append("display_name", gson.serialize(kit.getDisplayName()))
                .append("display_material", kit.getDisplayMaterial().toString())
                .append("cooldown", kit.getCooldown())
                .append("permission", kit.getPermission())
                .append("items", items);
    }

    public static KitTemplate deserializeKit(Document doc) {
        String name = doc.getString("name");
        KitTemplate kit = new KitTemplate(name);

        // Display name
        if (doc.containsKey("display_name")) {
            kit.setDisplayName(gson.deserialize(doc.getString("display_name")));
        }

        // Display material
        if (doc.containsKey("display_material")) {
            kit.setDisplayMaterial(Material.valueOf(doc.getString("display_material")));
        }

        // Cooldown
        kit.setCooldown(doc.getLong("cooldown"));

        // Permission
        kit.setPermission(doc.getString("permission"));

        // Items
        List<?> itemsRaw = (List<?>) doc.get("items");
        if (itemsRaw != null) {
            for (Object o : itemsRaw) {
                if (o instanceof Document itemDoc) {
                    ItemTemplate item = deserializeItem(itemDoc);
                    kit.addItem(item);
                }
            }
        }
        return kit;
    }

    // Map-based serialization for non-BSON storage backends (e.g., local files)
    public static Map<String, Object> serializeKitToMap(KitTemplate kit) {
        List<Map<String, Object>> items = kit.getItems().stream()
                .map(KitSerializer::serializeItemToMap)
                .toList();

        Map<String, Object> map = new HashMap<>();
        map.put("name", kit.getName());
        map.put("display_name", gson.serialize(kit.getDisplayName()));
        map.put("display_material", kit.getDisplayMaterial().toString());
        map.put("cooldown", kit.getCooldown());
        map.put("permission", kit.getPermission());
        map.put("items", items);
        return map;
    }

    public static KitTemplate deserializeKitFromMap(Map<String, Object> map) {
        String name = (String) map.get("name");
        KitTemplate kit = new KitTemplate(name);

        Object dn = map.get("display_name");
        if (dn instanceof String s) {
            kit.setDisplayName(gson.deserialize(s));
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

        Object itemsObj = map.get("items");
        if (itemsObj instanceof List<?> rawList) {
            for (Object o : rawList) {
                if (o instanceof Map<?, ?> itemMapRaw) {
                    // Convert Map<?, ?> to Map<String, Object>
                    Map<String, Object> itemMap = new HashMap<>();
                    for (Map.Entry<?, ?> e : itemMapRaw.entrySet()) {
                        if (e.getKey() instanceof String key) {
                            itemMap.put(key, e.getValue());
                        }
                    }
                    ItemTemplate item = deserializeItemFromMap(itemMap);
                    kit.addItem(item);
                }
            }
        }
        return kit;
    }

    private static Document serializeItem(ItemTemplate item) {
        Map<String, Integer> enchants = item.getEnchantments().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getKey().toString(), Map.Entry::getValue));

        List<String> lore = item.getLore() != null
                ? item.getLore().stream().map(gson::serialize).toList()
                : null;

        return new Document("material", item.getItem().getType().toString())
                .append("name", gson.serialize(item.getName()))
                .append("qty", item.getQty())
                .append("lore", lore)
                .append("enchantments", enchants);
    }

    private static ItemTemplate deserializeItem(Document doc) {
        Material mat = Material.valueOf(doc.getString("material"));
        Component name = gson.deserialize(doc.getString("name"));
        ItemTemplate item = new ItemTemplate(new ItemStack(mat), name);

        item.setQty(doc.getInteger("qty", 1));

        Object loreObj = doc.get("lore");
        if (loreObj instanceof List<?> loreRaw) {
            List<Component> lore = loreRaw.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(gson::deserialize)
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
                    if (enchant != null) {
                        item.addEnchantment(enchant, (Integer) entry.getValue());
                    }
                }
            }
        }

        return item;
    }

    private static Map<String, Object> serializeItemToMap(ItemTemplate item) {
        Map<String, Integer> enchants = item.getEnchantments().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getKey().toString(), Map.Entry::getValue));

        List<String> lore = item.getLore() != null
                ? item.getLore().stream().map(gson::serialize).toList()
                : null;

        Map<String, Object> map = new HashMap<>();
        map.put("material", item.getItem().getType().toString());
        map.put("name", gson.serialize(item.getName()));
        map.put("qty", item.getQty());
        map.put("lore", lore);
        map.put("enchantments", enchants);
        return map;
    }

    private static ItemTemplate deserializeItemFromMap(Map<String, Object> map) {
        Material mat = Material.valueOf((String) map.get("material"));
        Component name = gson.deserialize((String) map.get("name"));
        ItemTemplate item = new ItemTemplate(new ItemStack(mat), name);

        Object qtyObj = map.get("qty");
        item.setQty(qtyObj instanceof Number n ? n.intValue() : 1);

        Object loreObj = map.get("lore");
        if (loreObj instanceof List<?> loreRaw) {
            List<Component> lore = new ArrayList<>();
            for (Object el : loreRaw) {
                if (el instanceof String s) {
                    lore.add(gson.deserialize(s));
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
