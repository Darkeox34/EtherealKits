package gg.ethereallabs.heavenkits.commands;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.commands.abs.BaseCommand;
import gg.ethereallabs.heavenkits.commands.abs.CommandHandler;
import gg.ethereallabs.heavenkits.commands.subcommands.KitsCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;

public class CommandRegistry implements CommandExecutor, TabCompleter {
    private final Map<String, CommandHandler> commands = new HashMap<>();

    public CommandRegistry() {
        registerCommand(new KitsCommand());
    }

    private void registerCommand(BaseCommand handler) {
        commands.put(handler.getName(), handler);
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("heavenkits.hk")) {
            return true;
        }

        if (args.length == 0) {
            sendDefaultMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        CommandHandler handler = commands.get(subCommand);

        if (handler != null) {
            return handler.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sendMessage(sender, "<red>Comando non trovato! Scrivi /hk help per avere una lista di tutti i comandi.");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            for (String key : commands.keySet()) {
                if (key.startsWith(args[0].toLowerCase())) {
                    result.add(key);
                }
            }
            return result;
        }

        String subCommand = args[0].toLowerCase();
        CommandHandler handler = commands.get(subCommand);
        if (handler == null) {
            return Collections.emptyList();
        }

        return handler.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    private void sendDefaultMessage(CommandSender sender) {
        sender.sendMessage("§3§m----------------====§8§b SLDungeons§3§m====-------------");
        sender.sendMessage("§3MCLeveling dungeon plugin.");
        sender.sendMessage("§3/sld§7 help ➛ Have a list of plugin commands");
        sender.sendMessage("§3§m-----------------§8 Author@Darkeox34 §3§m-----------------");
    }

    public static void sendMessage(CommandSender sender, String message) {
        HeavenKits.sendMessage(sender, message);
    }
}
