package net.andrasia.kiryu144.andrasiaguns.guns;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class BulletData {
    private Projectile instance;
    private Player shooter;
    private float damage;
    private Gun gun;

    public BulletData(Projectile instance, Player shooter, Gun gun, float damage){
        this.instance = instance;
        this.shooter = shooter;
        this.damage = damage;
        this.gun = gun;
    }

    public Projectile getInstance() {
        return instance;
    }

    public Player getShooter() {
        return shooter;
    }

    public float getDamage() {
        return damage;
    }

    public Gun getGun() {
        return gun;
    }
}
