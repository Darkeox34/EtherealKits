package gg.ethereallabs.heavenkits;

import gg.ethereallabs.heavenkits.commands.CommandRegistry;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.KitManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HeavenKits extends JavaPlugin {
    public static HeavenKits instance;
    public static KitManager kitManager;
    public static final MiniMessage mm = MiniMessage.miniMessage();


    @Override
    public void onEnable() {
        instance = this;
        kitManager = new KitManager();

        Bukkit.getPluginManager().registerEvents(ChatPrompts.getInstance(), this);

        CommandRegistry mainCommand = new CommandRegistry();
        Objects.requireNonNull(getCommand("hk")).setExecutor(mainCommand);
        Objects.requireNonNull(getCommand("hk")).setTabCompleter(mainCommand);
    }

    @Override
    public void onDisable() {

    }

    public static void sendMessage(CommandSender sender, String message) {
        Component component = mm.deserialize("<gradient:#8A2BE2:#FF00FF:#00FF7F:#FFFF00:#FFA500>HeavenKits ></gradient> <yellow>" + message);
        sender.sendMessage(component);
    }

    public static void sendMessage(CommandSender sender, Component message) {
        Component prefix = mm.deserialize("<gradient:#8A2BE2:#FF00FF:#00FF7F:#FFFF00:#FFA500>HeavenKits ></gradient> <yellow>");
        Component fullMessage = prefix.append(message);
        sender.sendMessage(fullMessage);
    }

    public static long parseTime(String input) throws IllegalArgumentException {
        long totalSeconds = 0;
        Pattern pattern = Pattern.compile("(\\d+)([smhd])");
        Matcher matcher = pattern.matcher(input.toLowerCase().replaceAll("\\s+", ""));

        boolean found = false;
        while (matcher.find()) {
            found = true;
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "s": totalSeconds += value; break;
                case "m": totalSeconds += value * 60L; break;
                case "h": totalSeconds += value * 3600L; break;
                case "d": totalSeconds += value * 86400L; break;
                default: throw new IllegalArgumentException("Unità di tempo non valida: " + unit);
            }
        }

        if (!found) throw new IllegalArgumentException("Formato del tempo non valido: " + input);
        return totalSeconds;
    }

    public static String formatRemainingTime(long totalSeconds) {
        long day = totalSeconds / 86400;
        long hour = (totalSeconds % 86400) / 3600;
        long minute = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (day > 0) {
            sb.append(day).append(day == 1 ? " giorno" : " giorni");
            if (hour > 0) sb.append(" ").append(hour).append(hour == 1 ? " ora" : " ore");
        } else if (hour > 0) {
            sb.append(hour).append(hour == 1 ? " ora" : " ore");
            if (minute > 0) sb.append(" ").append(minute).append(minute == 1 ? " minuto" : " minuti");
        } else if (minute > 0) {
            sb.append(minute).append(minute == 1 ? " minuto" : " minuti");
            if (seconds > 0) sb.append(" ").append(seconds).append(seconds == 1 ? " secondo" : " secondi");
        } else {
            sb.append(seconds).append(seconds == 1 ? " secondo" : " secondi");
        }

        return sb.toString();
    }

    public static String formatTime(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        sb.append(days).append(days == 1 ? " giorno " : " giorni ");
        sb.append(hours).append(hours == 1 ? " ora " : " ore ");
        sb.append(minutes).append(minutes == 1 ? " minuto " : " minuti ");
        sb.append(seconds).append(seconds == 1 ? " secondo" : " secondi");

        return sb.toString().trim();
    }
}
