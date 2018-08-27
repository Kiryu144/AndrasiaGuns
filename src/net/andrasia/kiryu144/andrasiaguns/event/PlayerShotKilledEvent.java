package net.andrasia.kiryu144.andrasiaguns.event;

import net.andrasia.kiryu144.andrasiaguns.guns.Gun;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerShotKilledEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    Player victim;
    Player killer;
    Gun gun;

    public PlayerShotKilledEvent(Player victim, Player killer, Gun gun){
        this.victim = victim;
        this.killer = killer;
        this.gun = gun;
    }

    public Player getVictim() {
        return victim;
    }

    public Player getKiller() {
        return killer;
    }

    public Gun getGun() {
        return gun;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
