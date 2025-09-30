package gg.ethereallabs.heavenkits.commands.abs;

import gg.ethereallabs.heavenkits.HeavenKits;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class BaseCommand implements CommandHandler {
    private final String name;
    public BaseCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    public static void sendMessage(CommandSender sender, String message) {
        HeavenKits.sendMessage(sender, message);
    }
}