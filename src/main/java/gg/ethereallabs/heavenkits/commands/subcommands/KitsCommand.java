package gg.ethereallabs.heavenkits.commands.subcommands;

import gg.ethereallabs.heavenkits.commands.abs.BaseCommand;
import gg.ethereallabs.heavenkits.gui.KitsMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KitsCommand extends BaseCommand {
    public KitsCommand() {
        super("kits");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hk.commands.kits")) {
            sendMessage(sender, "<red>You don't have permission to execute this command!");
            return true;
        }

        if(!(sender instanceof Player player)){
            sendMessage(sender, "<red>Only players can execute this command!");
            return true;
        }

        new KitsMenu().open(player);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
