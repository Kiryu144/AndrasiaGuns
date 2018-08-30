package net.andrasia.kiryu144.andrasiaguns.input;

import net.andrasia.kiryu144.andrasiaguns.Main;
import net.andrasia.kiryu144.andrasiaguns.guns.ActivePlayerList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class Keyboard implements Listener {
    private static HashMap<Player, Long> lastPress = new HashMap<>();
    private static HashMap<Player, Boolean> moving = new HashMap<>();

    public Keyboard() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> {
            for(Player p : moving.keySet()){
                moving.put(p, false);
            }
        }, 0, 5);
    }

    @EventHandler
    public static void onInteract(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            lastPress.put(e.getPlayer(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public static void onMove(PlayerMoveEvent e){
        moving.put(e.getPlayer(), true);
    }

    public static boolean isPressed(Player p){
        Long last = lastPress.get(p);
        return last != null && System.currentTimeMillis() - last < 220;
    }

    public static boolean isRunning(Player p){
        return moving.get(p);
    }

    public static void registerPlayer(Player p) {
        lastPress.put(p, 0L);
        moving.put(p, false);
    }

    public static void unregisterPlayer(Player p) {
        lastPress.remove(p);
        moving.remove(p);
    }
}
