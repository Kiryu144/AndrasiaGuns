package net.andrasia.kiryu144.andrasiaguns.guns;

import net.andrasia.kiryu144.andrasiaguns.Main;
import net.andrasia.kiryu144.andrasiaguns.external.NBTEditor;
import net.andrasia.kiryu144.kiryucore.console.KiryuLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.HashMap;

public class Gun implements ActivePlayerList {
    private HashMap<Player, GunPlayerData> playerSpecificData = new HashMap<>();

    private String displayName;

    private float damage;
    private float rate;
    private float delay;
    private float accuracy;

    private int id;
    private int magSize;

    private long reloadTime;

    private ItemStack templateItemNormal;
    private ItemStack templateItemAimed;

    public Gun(int id, String displayName, float damage, float rate, float accuracy, int magSize, long reloadTime){
        this.id = id;
        this.displayName = displayName;
        this.damage = damage;
        this.rate = rate;
        this.delay = Math.round(1000/rate);
        this.accuracy = accuracy;
        this.magSize = magSize;
        this.reloadTime = reloadTime;
    }

    public void generateTemplates(Material material, Short durability) {
        templateItemNormal = new ItemStack(material, 1, durability);
        ItemMeta meta = templateItemNormal.getItemMeta();
        meta.setDisplayName(this.displayName);
        meta.setUnbreakable(true);
        templateItemNormal.setItemMeta(meta);
        templateItemNormal = NBTEditor.setItemTag(templateItemNormal, id, "gunid");

        templateItemAimed = templateItemNormal.clone();
        templateItemAimed.setDurability(((short) (templateItemNormal.getDurability()+1)));
    }

    public float getDamage() {
        return damage;
    }

    public float getRate() {
        return rate;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public int getMagSize() {
        return magSize;
    }

    public ItemStack getTemplateItemNormal() {
        return templateItemNormal;
    }

    public ItemStack getTemplateItemAimed() {
        return templateItemAimed;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getId() {
        return id;
    }

    public boolean canShoot(Player p){
        GunPlayerData data = playerSpecificData.get(p);
        long current = System.currentTimeMillis();
        if(current - data.getLastShot() > this.delay){
            if(current - data.getTimeoutStart() > data.getTimeout()){
                if(data.getAmmoLeft() > 0){
                    return true;
                }
            }
        }
        return false;
    }

    public Location getStartingLocation(Player p, boolean aimed){
        Location loc = p.getEyeLocation();
        loc.add(0, -0.3, 0);

        if(!aimed){
            float yaw = -((float) Math.PI * 2 / 360 * (p.getLocation().getYaw() + 25));
            double radius = 1.2;
            loc.add(Math.sin(yaw) * radius, 0, Math.cos(yaw) * radius);
        }

        return loc;
    }

    public void initiateReload(Player p) {
        GunPlayerData data = this.playerSpecificData.get(p);
        data.setTimeout(reloadTime);
        data.startTimeout();

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, () -> {
            playerSpecificData.get(p).setAmmoLeft(magSize);
        }, reloadTime/50);
    }

    public void handleShooting(Player p){
        if(this.canShoot(p)){
            Location startLocation = this.getStartingLocation(p, GunManager.isPlayerScoped(p));
            Projectile bullet = startLocation.getWorld().spawn(startLocation, Snowball.class);
            int bulletId = BulletDatabase.addBullet(new BulletData(bullet, p, this, this.damage));

            bullet.setCustomName(String.valueOf(bulletId));
            bullet.setGravity(false);
            bullet.setVelocity(p.getLocation().getDirection().normalize().multiply(4));

            playerSpecificData.get(p).setAmmoLeft(playerSpecificData.get(p).getAmmoLeft() - 1);
            playerSpecificData.get(p).setLastShot(System.currentTimeMillis());
        }
    }

    @Override
    public void registerPlayer(Player p) {
        playerSpecificData.put(p, new GunPlayerData());
    }

    @Override
    public void unregisterPlayer(Player p) {
        playerSpecificData.remove(p);
    }
}




















