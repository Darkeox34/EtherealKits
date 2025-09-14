package gg.ethereallabs.heavenkits.events;

import gg.ethereallabs.heavenkits.HeavenKits;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        HeavenKits.kitManager.loadCooldowns(p);
    }
}
