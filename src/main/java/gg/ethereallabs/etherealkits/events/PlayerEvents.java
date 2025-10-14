package gg.ethereallabs.etherealkits.events;

import gg.ethereallabs.etherealkits.EtherealKits;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        EtherealKits.getInstance().getKitManager().loadCooldowns(p);
    }
}
