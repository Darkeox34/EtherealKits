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

import static gg.ethereallabs.heavenkits.HeavenKits.mm;

public class AdminCommand extends BaseCommand {
    public AdminCommand() {
        super("admin");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hk.commands.admin")) {
            sendMessage(sender, "<red>Non hai il permesso di eseguire questo comando!");
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
                sendMessage(sender, "<red>Comando non riconosciuto! Usa <yellow>/hk admin help</yellow> per vedere tutti i comandi disponibili.");
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
                    return HeavenKits.kitManager.getKits().keySet().stream()
                            .filter(kitName -> kitName.toLowerCase().startsWith(args[1].toLowerCase()))
                            .toList();
                case "create":
                    return List.of("<nome_kit>");
            }
        }

        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if ("rename".equals(subCommand)) {
                return List.of("<nuovo_nome>");
            }
        }

        return List.of();
    }

    public void handleGui(CommandSender sender, String[] args){
        if(!(sender instanceof Player player)){
            sendMessage(sender, "<red>Solo i giocatori possono eseguire questo comando!");
            return;
        }
        new EditKitsMenu().open(player);
    }

    public void handleEdit(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)){
            sendMessage(sender, "<red>Solo i giocatori possono eseguire questo comando!");
            return;
        }

        if (args.length != 2) {
            sendMessage(sender, "<red>Uso corretto: <yellow>/hk kits edit <nome_kit>");
            return;
        }

        KitTemplate toEdit = HeavenKits.kitManager.getKit(args[1]);

        if (toEdit == null) {
            sendMessage(sender, "<red>Kit '<yellow>" + args[1] + "</yellow>' non trovato!");
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        new EditMenu(toEdit).open(player);
    }

    public void handleCreate(CommandSender sender, String[] args){
        if (args.length != 2) {
            sendMessage(sender, "<red>Uso corretto: <yellow>/hk kits create <nome_kit>");
            return;
        }

        String kitName = args[1];

        boolean succ = HeavenKits.kitManager.createKit(args[1], sender);
        if (!succ) {
            if(sender instanceof Player player){
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            sendMessage(sender, "<green>Kit '<yellow>" + kitName + "</yellow>' creato con successo!");
        }
    }

    public void handleList(CommandSender sender, String[] args){
        if (HeavenKits.kitManager.getKits().isEmpty()) {
            sendMessage(sender, "<yellow>Nessun kit disponibile.");
            return;
        }

        HeavenKits.kitManager.listKits(sender);
    }
    public void handleDelete(CommandSender sender, String[] args){
        if (args.length < 1) {
            sendMessage(sender, "<red>Uso corretto: <yellow>/hk kits delete <nome_kit>");
            return;
        }

        String kitName = args[1];

        if (HeavenKits.kitManager.getKit(kitName) == null) {
            sendMessage(sender, "<red>Kit '<yellow>" + kitName + "</yellow>' non trovato!");
            return;
        }

        HeavenKits.kitManager.deleteKit(kitName);

        sendMessage(sender, "<green>Kit '<yellow>" + kitName + "</yellow>' eliminato con successo!");
    }

    public void handleRename(CommandSender sender, String[] args){
        if (args.length < 2) {
            sendMessage(sender, "<red>Uso corretto: <yellow>/hk kits rename <nome_attuale> <nuovo_nome>");
            return;
        }

        String oldName = args[1];
        String newName = args[2];

        boolean succ = HeavenKits.kitManager.renameKit(oldName, newName, sender);

        if (!succ) {
            if(sender instanceof Player player){
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            sendMessage(sender, "<green>Kit '<yellow>" + oldName + "</yellow>' rinominato in '<yellow>" + newName + "</yellow>'!");
        }
    }

    private void helpMessage(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<gradient:#8A2BE2:#FF00FF>=================== HeavenKits ===================</gradient>"));
        sender.sendMessage(mm.deserialize( "<yellow>/hk admin list</yellow> <gray>- Mostra tutti i kit disponibili"));
        sender.sendMessage(mm.deserialize( "<yellow>/hk admin create <nome></yellow> <gray>- Crea un nuovo kit"));
        sender.sendMessage(mm.deserialize( "<yellow>/hk admin delete <nome></yellow> <gray>- Elimina un kit esistente"));
        sender.sendMessage(mm.deserialize( "<yellow>/hk admin rename <nome> <nuovo_nome></yellow> <gray>- Rinomina un kit"));
        sender.sendMessage(mm.deserialize( "<yellow>/hk admin edit <nome></yellow> <gray>- Modifica un kit (solo giocatori)"));
        sender.sendMessage(mm.deserialize( "<yellow>/hk admin gui</yellow> <gray>- Apri l'interfaccia grafica (solo giocatori)"));
        sender.sendMessage(mm.deserialize( "<gradient:#8A2BE2:#FF00FF>=================================================</gradient>"));
    }
}
