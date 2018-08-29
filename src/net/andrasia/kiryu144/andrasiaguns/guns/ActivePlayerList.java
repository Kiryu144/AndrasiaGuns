package net.andrasia.kiryu144.andrasiaguns.guns;

import org.bukkit.entity.Player;

public interface ActivePlayerList {
    void registerPlayer(Player p);
    void unregisterPlayer(Player p);
}
