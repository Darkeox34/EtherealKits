package gg.ethereallabs.etherealkits.gui.admin;

import gg.ethereallabs.etherealkits.EtherealKits;
import gg.ethereallabs.etherealkits.gui.models.BaseMenu;
import gg.ethereallabs.etherealkits.gui.models.ChatPrompts;
import gg.ethereallabs.etherealkits.kits.models.KitTemplate;
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

        HashMap<String, KitTemplate> kits = EtherealKits.getInstance().getKitManager().getKits();

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

        inv.setItem(49, createItem(Component.text("Create a new Kit")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false), Material.GREEN_STAINED_GLASS_PANE, Collections.emptyList(), 1));
    }

    private static @NotNull List<Component> getComponents(Map.Entry<String, KitTemplate> entry) {
        KitTemplate kit = entry.getValue();

        List<Component> kitLore = kit.getLore();
        List<Component> components = new ArrayList<>();

        List<Component> fixedLore = kitLore.stream()
                .map(component -> {
                    TextDecoration.State italicState = component.decoration(TextDecoration.ITALIC);
                    if (italicState == TextDecoration.State.NOT_SET) {
                        return component.decoration(TextDecoration.ITALIC, false);
                    }
                    return component;
                })
                .toList();

        components.add(EtherealKits.mm.deserialize("<gray>(Left-Click)<yellow> Edit").decoration(TextDecoration.ITALIC, false));
        components.add(EtherealKits.mm.deserialize("<gray>(Shift+Left-Click)<yellow> Rename").decoration(TextDecoration.ITALIC, false));
        components.add(EtherealKits.mm.deserialize("<gray>(Shift+Right-Click)<yellow> Rename DisplayName").decoration(TextDecoration.ITALIC, false));
        components.add(EtherealKits.mm.deserialize("<gray>(Right-Click)<yellow> Remove").decoration(TextDecoration.ITALIC, false));
        components.add(Component.empty());
        components.add(EtherealKits.mm.deserialize("<yellow>Cooldown: " + EtherealKits.formatTime(kit.getCooldown())).decoration(TextDecoration.ITALIC, false));
        components.add(EtherealKits.mm.deserialize("<yellow>Permission: " + kit.getPermission()).decoration(TextDecoration.ITALIC, false));
        components.add(EtherealKits.mm.deserialize("<yellow>Lore: ").decoration(TextDecoration.ITALIC, false));
        if(kitLore.isEmpty()){
            components.add(EtherealKits.mm.deserialize("<yellow>None").decoration(TextDecoration.ITALIC, false));
        }
        else{
            components.addAll(fixedLore);
        }

        return components;
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
        ChatPrompts.getInstance().ask(p, "Enter the Kit name", (player, message) -> {
            if(!EtherealKits.getInstance().getKitManager().createKit(message, player))
                return;


            Component kitCreated = Component.text()
                    .append(Component.text("Kit '").color(NamedTextColor.GREEN))
                    .append(Component.text(message).color(NamedTextColor.YELLOW))
                    .append(Component.text("' created successfully!").color(NamedTextColor.GREEN))
                    .build()
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to edit the Kit").color(NamedTextColor.GREEN)))
                    .clickEvent(ClickEvent.runCommand("/hk kits edit " + message));

            EtherealKits.sendMessage(player, kitCreated);
            new EditKitsMenu().open(p);
        });
    }

    void handleKitRename(Player p, KitTemplate kit){
        ChatPrompts.getInstance().ask(p, "Enter the new Kit name", (player, message) -> {
            EtherealKits.getInstance().getKitManager().renameKit(kit.getName(), message, p);
            EtherealKits.sendMessage(p, "<green>Kit '<yellow>" + kit.getName() + "</yellow>' renamed to '<yellow>" + message + "</yellow>'!");
            EtherealKits.getInstance().getKitManager().updateKit(kit);
        });
    }

    void handleKitRenameDisplayName(Player p, KitTemplate kit){
        ChatPrompts.getInstance().ask(p, "Enter the new Kit DisplayName", (player, message) -> {
            kit.setDisplayName(EtherealKits.mm.deserialize(message));
            EtherealKits.sendMessage(p, Component.text("You changed the displayname to ").color(NamedTextColor.GREEN).append(kit.getDisplayName()));
            EtherealKits.getInstance().getKitManager().updateKit(kit);
        });
    }

    void handleKitDelete(Player p, KitTemplate kit){
        ChatPrompts.getInstance().ask(p, "Are you sure you want to remove the Kit " + kit.getName() + "? (yes | no)", (player, message) -> {
            if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y")) {
                EtherealKits.getInstance().getKitManager().deleteKit(kit.getName());
                EtherealKits.sendMessage(p, "You removed the Kit: " + kit.getName());
            }

            new EditKitsMenu().open(player);
        });
    }
}
