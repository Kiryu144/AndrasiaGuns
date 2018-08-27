package net.andrasia.kiryu144.andrasiaguns.event;

import net.andrasia.kiryu144.andrasiaguns.guns.BulletData;
import net.andrasia.kiryu144.andrasiaguns.guns.Gun;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerShotEntityEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player shooter;
    private LivingEntity victim;
    private Gun gun;
    private BulletData bullet;

    public PlayerShotEntityEvent(Player shooter, LivingEntity victim, Gun gun, BulletData bullet){
        this.shooter = shooter;
        this.victim = victim;
        this.gun = gun;
        this.bullet = bullet;
    }

    public Player getShooter() {
        return shooter;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public Gun getGun() {
        return gun;
    }

    public BulletData getBullet() {
        return bullet;
    }

    public double getDistance(){
        return shooter.getLocation().distance(victim.getLocation());
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
