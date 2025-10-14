package gg.ethereallabs.etherealkits.data;

import gg.ethereallabs.etherealkits.EtherealKits;
import gg.ethereallabs.etherealkits.kits.models.KitTemplate;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FileStorage implements Storage {
    private final Path baseDir;
    private final Path kitsDir;
    private final Path cooldownsFile;

    public FileStorage(Path dataFolder) {
        this.baseDir = dataFolder.resolve("data");
        this.kitsDir = baseDir.resolve("kits");
        this.cooldownsFile = baseDir.resolve("cooldowns.yml");

        try {
            Files.createDirectories(kitsDir);
            if (!Files.exists(cooldownsFile)) {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.set("_id", "cooldowns");
                yaml.set("players", new HashMap<>());
                yaml.save(cooldownsFile.toFile());
            }
        } catch (IOException e) {
            EtherealKits.getInstance().getLogger().severe("Failed to initialize local storage: " + e.getMessage());
        }
    }

    @Override
    public void loadAllKits(Map<String, KitTemplate> kitsOut) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(kitsDir, "*.yml")) {
            for (Path p : stream) {
                try {
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(p.toFile());
                    Map<String, Object> map = yaml.getValues(false);
                    if (map.containsKey("name") && map.get("name") != null) {
                        KitTemplate kit = KitSerializer.deserializeKitFromMap(map);
                        kitsOut.put(kit.getName(), kit);
                    }
                } catch (Exception ex) {
                    EtherealKits.getInstance().getLogger().warning("Failed to read kit file " + p.getFileName() + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            EtherealKits.getInstance().getLogger().severe("Failed to list kits directory: " + e.getMessage());
        }
    }

    @Override
    public void insertKit(KitTemplate kit) {
        writeKitFile(kit);
    }

    @Override
    public void replaceKit(KitTemplate kit) {
        writeKitFile(kit);
    }

    private void writeKitFile(KitTemplate kit) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> map = KitSerializer.serializeKitToMap(kit);
                YamlConfiguration yaml = new YamlConfiguration();
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    yaml.set(e.getKey(), e.getValue());
                }
                Path file = kitsDir.resolve(kit.getName() + ".yml");
                yaml.save(file.toFile());
            } catch (IOException e) {
                EtherealKits.getInstance().getLogger().severe("Failed to write kit '" + kit.getName() + "': " + e.getMessage());
            }
        });
    }

    @Override
    public void deleteKit(String name) {
        CompletableFuture.runAsync(() -> {
            try {
                Path file = kitsDir.resolve(name + ".yml");
                Files.deleteIfExists(file);
            } catch (IOException e) {
                EtherealKits.getInstance().getLogger().severe("Failed to delete kit '" + name + "': " + e.getMessage());
            }
        });
    }

    @Override
    public void updatePlayerCooldown(String playerUUID, String kitName, long redeemTime) {
        CompletableFuture.runAsync(() -> {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(cooldownsFile.toFile());
                yaml.set("players." + playerUUID + "." + kitName, redeemTime);
                yaml.save(cooldownsFile.toFile());
            } catch (IOException e) {
                EtherealKits.getInstance().getLogger().severe("Failed to update cooldowns: " + e.getMessage());
            }
        });
    }

    @Override
    public Map<String, Long> loadPlayerCooldowns(UUID playerUUID) {
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(cooldownsFile.toFile());
            Map<String, Object> playerDoc = yaml.getConfigurationSection("players." + playerUUID)
                    != null ? yaml.getConfigurationSection("players." + playerUUID).getValues(false) : null;
            if (playerDoc == null) return Collections.emptyMap();
            Map<String, Long> map = new HashMap<>();
            for (Map.Entry<String, Object> e : playerDoc.entrySet()) {
                if (e.getValue() instanceof Number n) {
                    map.put(e.getKey(), n.longValue());
                }
            }
            return map;
        } catch (Exception e) {
            EtherealKits.getInstance().getLogger().severe("Failed to load player cooldowns: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public void removeExpiredCooldown(String playerUUID, String kitName) {
        CompletableFuture.runAsync(() -> {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(cooldownsFile.toFile());
                yaml.set("players." + playerUUID + "." + kitName, null);
                yaml.save(cooldownsFile.toFile());
            } catch (IOException e) {
                EtherealKits.getInstance().getLogger().severe("Failed to remove expired cooldown: " + e.getMessage());
            }
        });
    }

    @Override
    public void cleanExpiredCooldowns() {
        CompletableFuture.runAsync(() -> {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(cooldownsFile.toFile());
                long now = System.currentTimeMillis();
                if (yaml.getConfigurationSection("players") == null) return;
                for (String uuid : yaml.getConfigurationSection("players").getKeys(false)) {
                    if (yaml.getConfigurationSection("players." + uuid) == null) continue;
                    List<String> toRemove = new ArrayList<>();
                    for (String kitName : yaml.getConfigurationSection("players." + uuid).getKeys(false)) {
                        Object v = yaml.get("players." + uuid + "." + kitName);
                        long ts = v instanceof Number ? ((Number) v).longValue() : 0L;
                        if (ts < now) toRemove.add(kitName);
                    }
                    for (String k : toRemove) {
                        yaml.set("players." + uuid + "." + k, null);
                    }
                }
                yaml.save(cooldownsFile.toFile());
            } catch (IOException e) {
                EtherealKits.getInstance().getLogger().severe("Failed to clean expired cooldowns: " + e.getMessage());
            }
        });
    }
}