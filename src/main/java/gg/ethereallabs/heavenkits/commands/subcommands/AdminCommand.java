package gg.ethereallabs.heavenkits.commands.subcommands;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.commands.abs.BaseCommand;
import gg.ethereallabs.heavenkits.gui.admin.EditMenu;
import gg.ethereallabs.heavenkits.gui.admin.EditKitsMenu;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminCommand extends BaseCommand {
    public AdminCommand() {
        super("admin");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hk.commands.admin")) {
            sendMessage(sender, "<red>You don't have permission to execute this command!");
            return true;
        }

        if (args.length == 0) {
            helpMessage(sender);
            return true;
        }
        switch(args[0]){
            case "help":
                helpMessage(sender);
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
                sendMessage(sender, "<red>Unrecognized command! Use <yellow>/hk admin help</yellow> to see all available commands.");
                break;
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> commands = List.of("help", "list", "create", "delete", "rename", "edit", "gui");
            return commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "delete":
                case "rename":
                case "edit":
                    return HeavenKits.getInstance().getKitManager().getKits().keySet().stream()
                            .filter(kitName -> kitName.toLowerCase().startsWith(args[1].toLowerCase()))
                            .toList();
                case "create":
                    return List.of("<kit_name>");
            }
        }

        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if ("rename".equals(subCommand)) {
                return List.of("<new_name>");
            }
        }

        return List.of();
    }

    public void handleGui(CommandSender sender, String[] args){
        if(!(sender instanceof Player player)){
            sendMessage(sender, "<red>Only players can execute this command!");
            return;
        }
        new EditKitsMenu().open(player);
    }

    public void handleEdit(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)){
            sendMessage(sender, "<red>Only players can execute this command!");
            return;
        }

        if (args.length != 2) {
            sendMessage(sender, "<red>Correct usage: <yellow>/hk kits edit <kit_name>");
            return;
        }

        KitTemplate toEdit = HeavenKits.getInstance().getKitManager().getKit(args[1]);

        if (toEdit == null) {
            sendMessage(sender, "<red>Kit '<yellow>" + args[1] + "</yellow>' not found!");
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        new EditMenu(toEdit).open(player);
    }

    public void handleCreate(CommandSender sender, String[] args){
        if (args.length != 2) {
            sendMessage(sender, "<red>Correct usage: <yellow>/hk kits create <kit_name>");
            return;
        }

        String kitName = args[1];

        boolean succ = HeavenKits.getInstance().getKitManager().createKit(args[1], sender);
        if (!succ) {
            if(sender instanceof Player player){
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            sendMessage(sender, "<green>Kit '<yellow>" + kitName + "</yellow>' created successfully!");
        }
    }

    public void handleList(CommandSender sender, String[] args){
        if (HeavenKits.getInstance().getKitManager().getKits().isEmpty()) {
            sendMessage(sender, "<yellow>No kits available.");
            return;
        }

        HeavenKits.getInstance().getKitManager().listKits(sender);
    }
    public void handleDelete(CommandSender sender, String[] args){
        if (args.length < 1) {
            sendMessage(sender, "<red>Correct usage: <yellow>/hk kits delete <kit_name>");
            return;
        }

        String kitName = args[1];

        if (HeavenKits.getInstance().getKitManager().getKit(kitName) == null) {
            sendMessage(sender, "<red>Kit '<yellow>" + kitName + "</yellow>' not found!");
            return;
        }

        HeavenKits.getInstance().getKitManager().deleteKit(kitName);

        sendMessage(sender, "<green>Kit '<yellow>" + kitName + "</yellow>' deleted successfully!");
    }

    public void handleRename(CommandSender sender, String[] args){
        if (args.length < 2) {
            sendMessage(sender, "<red>Correct usage: <yellow>/hk kits rename <current_name> <new_name>");
            return;
        }

        String oldName = args[1];
        String newName = args[2];

        boolean succ = HeavenKits.getInstance().getKitManager().renameKit(oldName, newName, sender);

        if (!succ) {
            if(sender instanceof Player player){
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            sendMessage(sender, "<green>Kit '<yellow>" + oldName + "</yellow>' renamed to '<yellow>" + newName + "</yellow>'!");
        }
    }

    private void helpMessage(CommandSender sender) {
        sender.sendMessage(HeavenKits.mm.deserialize("<gradient:#8A2BE2:#FF00FF>=================== HeavenKits ===================</gradient>"));
        sender.sendMessage(HeavenKits.mm.deserialize( "<yellow>/hk admin list</yellow> <gray>- Show all available kits"));
        sender.sendMessage(HeavenKits.mm.deserialize( "<yellow>/hk admin create <name></yellow> <gray>- Create a new kit"));
        sender.sendMessage(HeavenKits.mm.deserialize( "<yellow>/hk admin delete <name></yellow> <gray>- Delete an existing kit"));
        sender.sendMessage(HeavenKits.mm.deserialize( "<yellow>/hk admin rename <name> <new_name></yellow> <gray>- Rename a kit"));
        sender.sendMessage(HeavenKits.mm.deserialize( "<yellow>/hk admin edit <name></yellow> <gray>- Edit a kit (players only)"));
        sender.sendMessage(HeavenKits.mm.deserialize( "<yellow>/hk admin gui</yellow> <gray>- Open the graphical interface (players only)"));
        sender.sendMessage(HeavenKits.mm.deserialize( "<gradient:#8A2BE2:#FF00FF>=================================================</gradient>"));
    }
}
