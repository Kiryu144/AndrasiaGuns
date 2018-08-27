package net.andrasia.kiryu144.andrasiaguns.input;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class Keyboard implements Listener {
    private static HashMap<Player, Long> lastPress = new HashMap<>();

    public Keyboard() {
        /* pass */
    }

    @EventHandler
    public static void onInteract(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            lastPress.put(e.getPlayer(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public static void onLeave(PlayerQuitEvent e){
        lastPress.remove(e.getPlayer());
    }

    public static boolean isPressed(Player p){
        Long last = lastPress.get(p);
        return last != null && System.currentTimeMillis() - last < 220;
    }
}
