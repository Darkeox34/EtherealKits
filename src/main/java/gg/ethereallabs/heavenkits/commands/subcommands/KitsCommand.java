package gg.ethereallabs.heavenkits.commands.subcommands;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.commands.abs.BaseCommand;
import gg.ethereallabs.heavenkits.gui.EditMenu;
import gg.ethereallabs.heavenkits.gui.KitsMenu;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KitsCommand extends BaseCommand {
    public KitsCommand() {
        super("kits");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendMessage(sender, "<red>");
            return true;
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
                handleEdit(sender, args);
                break;
            case "gui":
                handleGui(sender, args);
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
            return List.of("help", "list", "create", "delete", "rename", "edit", "gui");
        }
        return List.of();
    }

    public void handleGui(CommandSender sender, String[] args){
        if(!(sender instanceof Player player)){
            sendMessage(sender, "<red>Solo i giocatori possono eseguire questo comando!");
            return;
        }

        KitsMenu kitsMenu = new KitsMenu();
        kitsMenu.open(player);
    }

    public void handleEdit(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)){
            sendMessage(sender, "<red>Solo i giocatori possono eseguire questo comando!");
            return;
        }

        if (args.length == 0) {
            sendMessage(sender, "<red>");
            return;
        }

        KitTemplate toEdit = HeavenKits.kitManager.getKit(args[0]);

        EditMenu menu = new EditMenu(toEdit);
        menu.open(player);
    }

    public void handleCreate(CommandSender sender, String[] args){
        if (args.length == 0) {
            sendMessage(sender, "<red>");
            return;
        }

        if(args.length != 2) {
            sendMessage(sender, "<red>");
            return;
        }

        HeavenKits.kitManager.createKit(args[1]);
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
