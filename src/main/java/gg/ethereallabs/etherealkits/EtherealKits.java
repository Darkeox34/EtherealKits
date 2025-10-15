package gg.ethereallabs.etherealkits;

import gg.ethereallabs.etherealkits.commands.CommandRegistry;
import gg.ethereallabs.etherealkits.data.*;
import gg.ethereallabs.etherealkits.data.storage.FileStorage;
import gg.ethereallabs.etherealkits.data.storage.MongoStorage;
import gg.ethereallabs.etherealkits.data.storage.Storage;
import gg.ethereallabs.etherealkits.events.PlayerEvents;
import gg.ethereallabs.etherealkits.gui.models.ChatPrompts;
import gg.ethereallabs.etherealkits.kits.KitManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EtherealKits extends JavaPlugin {
    private static EtherealKits instance;
    private KitManager kitManager;
    private ConfigManager configManager;
    private MongoDB mongo;
    private Storage storage;
    private static boolean mmoItemsEnabled = false;
    public static final MiniMessage mm = MiniMessage.miniMessage();


    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        FileConfiguration config = getConfig();

        if (getServer().getPluginManager().getPlugin("MMOItems") != null) {
            getLogger().info("MMOItems found! Enabling integration...");
            mmoItemsEnabled = true;
        } else {
            getLogger().warning("MMOItems not found, skipping integration.");
        }

        configManager = new ConfigManager(config);

        configManager.loadConfig();

        boolean mongoEnabled = config.getBoolean("mongodb.enabled", false);
        if (mongoEnabled) {
            mongo = new MongoDB(config);
            storage = new MongoStorage(mongo);
        } else {
            storage = new FileStorage(getDataFolder().toPath());
        }

        kitManager = new KitManager(storage);
        kitManager.loadAllKits();

        getLogger().info("Loaded " + kitManager.getKits().size() + " kits from storage.");

        Bukkit.getPluginManager().registerEvents(ChatPrompts.getInstance(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEvents(), this);
        CommandRegistry mainCommand = new CommandRegistry();
        Objects.requireNonNull(getCommand("kit")).setExecutor(mainCommand);
        Objects.requireNonNull(getCommand("kit")).setTabCompleter(mainCommand);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            kitManager.cleanExpiredCooldowns();
        }, 0L, 72000L);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            long now = System.currentTimeMillis();
            kitManager.getCooldowns().forEach((uuid, playerCooldowns) -> {
                playerCooldowns.entrySet().removeIf(e -> e.getValue() <= now);
            });
        }, 20L * 60, 20L * 60);
    }

    @Override
    public void onDisable() {
        if (mongo != null) {
            mongo.close();
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        Component component = mm.deserialize("<gradient:#499BF9:#CB37E8>EtherealKits ></gradient> <yellow>" + message);
        sender.sendMessage(component);
    }

    public static void sendMessage(CommandSender sender, Component message) {
        Component prefix = mm.deserialize("<gradient:#499BF9:#CB37E8>EtherealKits ></gradient> <yellow>");
        Component fullMessage = prefix.append(message);
        sender.sendMessage(fullMessage);
    }

    public static void sendPlayerMessage(CommandSender sender, String message) {
        Component component = mm.deserialize(Configs.getPrefix() + " " + message);
        sender.sendMessage(component);
    }

    public static long parseTime(String input) throws IllegalArgumentException {
        long totalMillis = 0;
        Pattern pattern = Pattern.compile("(\\d+)([smhd])");
        Matcher matcher = pattern.matcher(input.toLowerCase().replaceAll("\\s+", ""));

        boolean found = false;
        while (matcher.find()) {
            found = true;
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "s": totalMillis += value * 1000L; break;
                case "m": totalMillis += value * 60_000L; break;
                case "h": totalMillis += value * 3_600_000L; break;
                case "d": totalMillis += value * 86_400_000L; break;
                default: throw new IllegalArgumentException("Invalid time unit: " + unit);
            }
        }

        if (!found) throw new IllegalArgumentException("Invalid time format: " + input);
        return totalMillis;
    }

    public static String formatRemainingTime(long totalMillis) {
        long totalSeconds = totalMillis / 1000;
        long day = totalSeconds / 86400;
        long hour = (totalSeconds % 86400) / 3600;
        long minute = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (day > 0) {
            sb.append(day).append(day == 1 ? " day" : " days");
            if (hour > 0) sb.append(" ").append(hour).append(hour == 1 ? " hour" : " hours");
        } else if (hour > 0) {
            sb.append(hour).append(hour == 1 ? " hour" : " hours");
            if (minute > 0) sb.append(" ").append(minute).append(minute == 1 ? " minute" : " minutes");
        } else if (minute > 0) {
            sb.append(minute).append(minute == 1 ? " minute" : " minutes");
            if (seconds > 0) sb.append(" ").append(seconds).append(seconds == 1 ? " second" : " seconds");
        } else {
            sb.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }

        return sb.toString();
    }


    public static String formatTime(long totalMillis) {
        long totalSeconds = totalMillis / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        sb.append(days).append(days == 1 ? " day " : " days ");
        sb.append(hours).append(hours == 1 ? " hour " : " hours ");
        sb.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
        sb.append(seconds).append(seconds == 1 ? " second" : " seconds");

        return sb.toString().trim();
    }
    
    public static EtherealKits getInstance() {
        return instance;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MongoDB getMongo() {
        return mongo;
    }

    public Storage getStorage() {
        return storage;
    }

    public static boolean isMMOItemsEnabled() { return mmoItemsEnabled; }
}
