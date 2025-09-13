package gg.ethereallabs.heavenkits;

import gg.ethereallabs.heavenkits.commands.CommandRegistry;
import gg.ethereallabs.heavenkits.kits.KitManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HeavenKits extends JavaPlugin {
    public static HeavenKits instance;
    public static KitManager kitManager;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();


    @Override
    public void onEnable() {
        instance = this;
        kitManager = new KitManager();

        CommandRegistry mainCommand = new CommandRegistry();
        Objects.requireNonNull(getCommand("hk")).setExecutor(mainCommand);
        Objects.requireNonNull(getCommand("hk")).setTabCompleter(mainCommand);
    }

    @Override
    public void onDisable() {

    }

    public static void sendMessage(CommandSender sender, String message) {
        Component component = MINI_MESSAGE.deserialize("<gradient:#8A2BE2:#FF00FF:#00FF7F:#FFFF00:#FFA500>HeavenKits ></gradient> <dark_aqua>" + message);
        sender.sendMessage(component);

        sender.sendMessage("§dHeavenKits > §3" + message);
    }
}
