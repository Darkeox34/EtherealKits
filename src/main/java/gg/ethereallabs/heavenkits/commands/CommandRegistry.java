package gg.ethereallabs.heavenkits.commands;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.commands.abs.BaseCommand;
import gg.ethereallabs.heavenkits.commands.abs.CommandHandler;
import gg.ethereallabs.heavenkits.commands.subcommands.AdminCommand;
import gg.ethereallabs.heavenkits.commands.subcommands.KitsCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;

import static gg.ethereallabs.heavenkits.HeavenKits.mm;

public class CommandRegistry implements CommandExecutor, TabCompleter {
    private final Map<String, CommandHandler> commands = new HashMap<>();

    public CommandRegistry() {
        registerCommand(new AdminCommand());
        registerCommand(new KitsCommand());
    }

    private void registerCommand(BaseCommand handler) {
        commands.put(handler.getName(), handler);
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("heavenkits.hk")) {
            sendMessage(sender, "<red>You don't have permission to execute this command!");
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
            sendMessage(sender, "<red>Command not found! Type /hk admin help to get a list of all commands.");
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

    public static void sendDefaultMessage(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<gradient:#8A2BE2:#FF00FF>=================== HeavenKits ===================</gradient>"));
        sender.sendMessage(mm.deserialize("<yellow>/hk admin help</yellow> <gray>- Show all available commands"));
        sender.sendMessage(mm.deserialize("<gradient:#8A2BE2:#FF00FF>================================================</gradient>"));
    }

    public static void sendMessage(CommandSender sender, String message) {
        HeavenKits.sendMessage(sender, message);
    }
}
