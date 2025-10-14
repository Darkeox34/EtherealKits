package gg.ethereallabs.etherealkits.commands.abs;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandHandler {
    boolean execute(CommandSender sender, String[] args);

    List<String> tabComplete(CommandSender sender, String[] args);
}
