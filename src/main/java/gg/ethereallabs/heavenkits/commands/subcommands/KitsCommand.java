package gg.ethereallabs.heavenkits.commands.subcommands;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.commands.abs.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class KitsCommand extends BaseCommand {
    public KitsCommand() {
        super("kits");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendMessage(sender, "<red>");
        }

        switch(args[0]){
            case "help":
                sendMessage(sender, "<red>");
                break;
            case "list":
                handleList(sender, args);
                break;
            case "create":
                handleCreate(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            case "rename":
                handleRename(sender, args);
                break;
            case "edit":
                sendMessage(sender, "<red>");
                break;
            default:
                sendMessage(sender, "<red>");
                break;
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("help", "list", "create", "delete", "rename", "edit");
        }
        return List.of();
    }

    public void handleCreate(CommandSender sender, String[] args){
        if (args.length == 0) {
            sendMessage(sender, "<red>");
            return;
        }

        if(args.length != 1) {
            sendMessage(sender, "<red>");
            return;
        }

        HeavenKits.kitManager.createKit(args[0]);
    }

    public void handleList(CommandSender sender, String[] args){
        HeavenKits.kitManager.listKits(sender);
    }

    public void handleDelete(CommandSender sender, String[] args){
        if (args.length == 0) {
            sendMessage(sender, "<red>");
            return;
        }

        if(args.length != 1) {
            sendMessage(sender, "<red>");
            return;
        }

        HeavenKits.kitManager.deleteKit(args[0]);
    }

    public void handleRename(CommandSender sender, String[] args){
        if (args.length == 0) {
            sendMessage(sender, "<red>");
            return;
        }
        if(args.length != 2) {
            sendMessage(sender, "<red>");
            return;
        }

        HeavenKits.kitManager.renameKit(args[0], args[1]);
    }
}
