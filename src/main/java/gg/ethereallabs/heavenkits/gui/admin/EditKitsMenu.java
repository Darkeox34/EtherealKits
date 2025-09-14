package gg.ethereallabs.heavenkits.gui.admin;

import gg.ethereallabs.heavenkits.HeavenKits;
import gg.ethereallabs.heavenkits.gui.models.BaseMenu;
import gg.ethereallabs.heavenkits.gui.models.ChatPrompts;
import gg.ethereallabs.heavenkits.kits.models.KitTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

import static gg.ethereallabs.heavenkits.HeavenKits.*;

public class EditKitsMenu extends BaseMenu {
    private final List<Integer> slotsList = IntStream.rangeClosed(0, 44)
            .boxed()
            .toList();

    private final Map<Integer, KitTemplate> slotToKit = new HashMap<>();

    public EditKitsMenu() {
        super("Kits", 54);
    }

    @Override
    public void draw(Player p) {
        inv.clear();
        slotToKit.clear();

        HashMap<String, KitTemplate> kits = HeavenKits.kitManager.getKits();

        int i = 0;
        for (Map.Entry<String, KitTemplate> entry : kits.entrySet()) {
            if (i >= slotsList.size()) break;

            KitTemplate kit = entry.getValue();
            Component displayName = kit.getDisplayName().append(Component.text(" (" + kit.getName() + ")", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false);
            List<Component> lore = getComponents(entry);

            int slot = slotsList.get(i);
            inv.setItem(slotsList.get(i), createItem(displayName, kit.getDisplayMaterial(), lore, 1));
            slotToKit.put(slot, kit);

            i++;
        }

        inv.setItem(49, createItem(Component.text("Crea un nuovo Kit")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false), Material.GREEN_STAINED_GLASS_PANE, Collections.emptyList(), 1));
    }

    private static @NotNull List<Component> getComponents(Map.Entry<String, KitTemplate> entry) {
        KitTemplate kit = entry.getValue();

        return List.of(
                mm.deserialize("<gray>(Left-Click)<yellow> Modifica").decoration(TextDecoration.ITALIC, false),
                mm.deserialize("<gray>(Shift+Left-Click)<yellow> Rinomina").decoration(TextDecoration.ITALIC, false),
                mm.deserialize("<gray>(Shift+Right-Click)<yellow> Rinomina DisplayName").decoration(TextDecoration.ITALIC, false),
                mm.deserialize("<gray>(Right-Click)<yellow> Rimuovi").decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                mm.deserialize("<yellow>Cooldown: " + formatTime(kit.getCooldown())).decoration(TextDecoration.ITALIC, false),
                mm.deserialize("<yellow>Permesso: " + kit.getPermission()).decoration(TextDecoration.ITALIC, false)
        );
    }

    @Override
    public void handleClick(Player p, int slot, InventoryClickEvent e) {
        if (slotToKit.containsKey(slot)) {
            KitTemplate kit = slotToKit.get(slot);

            boolean leftClick = e.isLeftClick();
            boolean shiftClick = e.isShiftClick();
            boolean rightClick = e.isRightClick();

            if (leftClick && shiftClick) {
                handleKitRename(p, kit);
            } else if (rightClick && shiftClick){
                handleKitRenameDisplayName(p, kit);
            }else if (leftClick) {
                new EditMenu(kit).open(p);
            } else if (rightClick) {
                handleKitDelete(p, kit);
            }
        } else if (slot == 49) {
            handleKitCreation(p);
        }
    }

    void handleKitCreation(Player p){
        ChatPrompts.getInstance().ask(p, "Inserire il nome del Kit", (player, message) -> {
            kitManager.createKit(message, player);

            Component kitCreated = Component.text()
                    .append(Component.text("Kit '").color(NamedTextColor.GREEN))
                    .append(Component.text(message).color(NamedTextColor.YELLOW))
                    .append(Component.text("' creato con successo!").color(NamedTextColor.GREEN))
                    .build()
                    .hoverEvent(HoverEvent.showText(Component.text("Clicca quì per modificare il Kit").color(NamedTextColor.GREEN)))
                    .clickEvent(ClickEvent.runCommand("/hk kits edit " + message));

            sendMessage(player, kitCreated);
            new EditKitsMenu().open(p);
        });
    }

    void handleKitRename(Player p, KitTemplate kit){
        ChatPrompts.getInstance().ask(p, "Inserire il nuovo nome del Kit", (player, message) -> {
            kitManager.renameKit(kit.getName(), message, p);
            sendMessage(p, "<green>Kit '<yellow>" + kit.getName() + "</yellow>' rinominato in '<yellow>" + message + "</yellow>'!");
        });
    }

    void handleKitRenameDisplayName(Player p, KitTemplate kit){
        ChatPrompts.getInstance().ask(p, "Inserire il nuovo DisplayName del Kit", (player, message) -> {
            kit.setDisplayName(mm.deserialize(message));
            sendMessage(p, Component.text("Hai cambiato il displayname in ").color(NamedTextColor.GREEN).append(kit.getDisplayName()));
        });
    }

    void handleKitDelete(Player p, KitTemplate kit){
        ChatPrompts.getInstance().ask(p, "Sei sicuro di voler rimuovere il Kit " + kit.getName() + "? (sì | no)", (player, message) -> {
            if (message.equalsIgnoreCase("si") || message.equalsIgnoreCase("sì")) {
                kitManager.deleteKit(kit.getName());
                HeavenKits.sendMessage(p, "Hai rimosso il Kit: " + kit.getName());
            }

            new EditKitsMenu().open(player);
        });
    }
}
