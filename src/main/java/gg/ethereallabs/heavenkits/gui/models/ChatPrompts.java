package gg.ethereallabs.heavenkits.gui.models;

import gg.ethereallabs.heavenkits.HeavenKits;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static gg.ethereallabs.heavenkits.HeavenKits.mm;

public class ChatPrompts implements Listener {

    private static final ChatPrompts instance = new ChatPrompts();

    private record Flow(BiConsumer<Player, String> step) {
        public void execute(Player player, String message) {
            if (message != null) {
                step.accept(player, message);
            }
        }
    }

    private final Map<UUID, Flow> pending = new ConcurrentHashMap<>();

    public static ChatPrompts getInstance() {
        return instance;
    }

    public void ask(Player player, String question, BiConsumer<Player, String> onAnswer) {
        player.closeInventory();
        player.sendMessage(mm.deserialize("<aqua>» " + question + " <gray>(cancel: <red>!cancel<gray>)"));
        pending.put(player.getUniqueId(), new Flow(onAnswer));
    }

    public void cancel(Player player) {
        pending.remove(player.getUniqueId());
        player.sendMessage(mm.deserialize("<gray>Operation cancelled."));
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Flow flow = pending.get(player.getUniqueId());
        if (flow == null) return;

        event.setCancelled(true);

        String msg = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (msg.equalsIgnoreCase("!cancel")) {
            cancel(player);
            return;
        }

        pending.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(HeavenKits.getInstance(), () -> flow.execute(player, msg));
    }
}
