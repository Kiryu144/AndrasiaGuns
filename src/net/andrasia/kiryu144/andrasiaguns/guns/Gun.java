package net.andrasia.kiryu144.andrasiaguns.guns;

import net.andrasia.kiryu144.andrasiaguns.Main;
import net.andrasia.kiryu144.andrasiaguns.external.NBTEditor;
import net.andrasia.kiryu144.andrasiaguns.input.Keyboard;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Gun implements ActivePlayerList {
    private HashMap<Player, GunPlayerData> playerSpecificData = new HashMap<>();

    private String displayName;

    private float damage;
    private float rate;
    private float delay;
    private float spread;
    private float spread_crouched;

    private int id;
    private int magSize;
    private int maxAmmo;

    private long reloadTime;

    private ItemStack templateItemNormal;
    private ItemStack templateItemAimed;

    public Vector bulletOffset = new Vector(0, 0, 0);

    public String sound_shooting;
    public String sound_bolt;
    public String sound_reloading;

    public Gun(int id, String displayName, float damage, float rate, float spread, float spread_crouched, int magSize, int maxAmmo, long reloadTime, Vector bulletOffset){
        this.id = id;
        this.displayName = displayName;
        this.damage = damage;
        this.rate = rate;
        this.delay = Math.round(1000/rate);
        this.spread = spread;
        this.spread_crouched = spread_crouched;
        this.magSize = magSize;
        this.maxAmmo = maxAmmo;
        this.reloadTime = reloadTime;
        this.bulletOffset = bulletOffset;
    }

    public HashMap<Player, GunPlayerData> getPlayerSpecificData() {
        return playerSpecificData;
    }

    public void setSounds(String shooting, String bolt, String reloading){
        this.sound_shooting = shooting;
        this.sound_bolt = bolt;
        this.sound_reloading = reloading;
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

    public float getSpread() {
        return spread;
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

    public CannotShootReason canShoot(Player p){
        GunPlayerData data = playerSpecificData.get(p);
        long current = System.currentTimeMillis();
        if(current - data.getLastShot() <= this.delay){
            return CannotShootReason.TOO_FAST;
        }

        if(current - data.getTimeoutStart() <= data.getTimeout()){
            return CannotShootReason.IN_TIMEOUT;
        }

        if(data.getAmmoLeft() <= 0){
            if(data.getTotalAmmoLeft() <= 0){
                return CannotShootReason.NO_RESERVE;
            }else {
                return CannotShootReason.NO_AMMO;
            }
        }
        return CannotShootReason.CAN_SHOOT;
    }

    private static Vector rotateVector(Vector v, float yawDegrees, float pitchDegrees) {
        double yaw = Math.toRadians(-1 * (yawDegrees + 90));
        double pitch = Math.toRadians(-pitchDegrees);

        double cosYaw = Math.cos(yaw);
        double cosPitch = Math.cos(pitch);
        double sinYaw = Math.sin(yaw);
        double sinPitch = Math.sin(pitch);

        double initialX, initialY, initialZ;
        double x, y, z;

        // Z_Axis rotation (Pitch)
        initialX = v.getX();
        initialY = v.getY();
        x = initialX * cosPitch - initialY * sinPitch;
        y = initialX * sinPitch + initialY * cosPitch;

        // Y_Axis rotation (Yaw)
        initialZ = v.getZ();
        initialX = x;
        z = initialZ * cosYaw - initialX * sinYaw;
        x = initialZ * sinYaw + initialX * cosYaw;

        return new Vector(x, y, z);
    }

    public Location getStartingLocation(Player p, boolean aimed){
        Location loc = p.getEyeLocation();

        if(!aimed){
            loc.add(rotateVector(bulletOffset, p.getLocation().getYaw(), p.getLocation().getPitch()));
            return loc;
        }else{
            loc.add(0, -0.3, 0);
            return loc;
        }
    }

    public void initiateReload(Player p) {
        if(!playerSpecificData.get(p).isInTimeout() && playerSpecificData.get(p).getTotalAmmoLeft() > 0) {
            GunPlayerData data = this.playerSpecificData.get(p);
            int ammoToFill = magSize - data.getAmmoLeft();
            if(ammoToFill > data.getTotalAmmoLeft()) {
                ammoToFill = data.getTotalAmmoLeft();
            }
            final int _ammoToFill = ammoToFill;

            data.setTimeout(reloadTime);
            data.startTimeout();

            this.showHotbarTitle(p, "§4§lReloading ..");
            p.getLocation().getWorld().playSound(p.getLocation(), sound_reloading, 3.0f, 1.0f);

            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, () -> {
                data.setAmmoLeft(data.getAmmoLeft() + _ammoToFill);
                data.setTotalAmmoLeft(data.getTotalAmmoLeft() - _ammoToFill);
                this.showHotbarTitle(p, "§a§lReloaded!");
            }, reloadTime / 50);
        }
    }

    public void handleShooting(Player p){
        CannotShootReason reason = this.canShoot(p);
        if(reason.equals(CannotShootReason.CAN_SHOOT)){
            Location startLocation = this.getStartingLocation(p, GunManager.isPlayerScoped(p));
            Projectile bullet = startLocation.getWorld().spawn(startLocation, Snowball.class);
            int bulletId = BulletDatabase.addBullet(new BulletData(bullet, p, this, this.damage));

            Vector spreadVector = null;
            if(p.isSneaking()) {
                spreadVector = new Vector(((Math.random() * 2) - 1) * spread_crouched, ((Math.random() * 2) - 1) * spread_crouched, ((Math.random() * 2) - 1) * spread_crouched);
            } else {
                spreadVector = new Vector(((Math.random() * 2) - 1) * spread, ((Math.random() * 2) - 1) * spread, ((Math.random() * 2) - 1) * spread);
            }
            if(Keyboard.isRunning(p)){
                spreadVector.multiply(3);
            }

            bullet.setCustomName(String.valueOf(bulletId));
            bullet.setGravity(false);
            bullet.setVelocity(p.getLocation().getDirection().normalize().multiply(4).add(spreadVector));

            playerSpecificData.get(p).setAmmoLeft(playerSpecificData.get(p).getAmmoLeft() - 1);
            playerSpecificData.get(p).setLastShot(System.currentTimeMillis());

            showAmmo(p);
            p.getLocation().getWorld().playSound(p.getLocation(), sound_shooting, 3.0f, 1.0f);
        }else if(reason.equals(CannotShootReason.NO_AMMO)){
            this.showHotbarTitle(p, "§c§l>>> RELOAD WITH Q! <<<");
        }
    }

    public String generateInfo(Player p){
        String s = "";
        s += "§6================\n";
        s += "§cWeapon Data:\n";
        s += String.format("§2%s: §b%s\n", "ID", String.valueOf(this.id));
        s += String.format("§2%s: §b%s\n", "Name", String.valueOf(this.displayName));
        s += String.format("§2%s: §b%s\n", "Damage", String.valueOf(this.damage));
        s += String.format("§2%s: §b%s\n", "Rate", String.valueOf(this.rate));
        s += String.format("§2%s: §b%s\n", "Spread", String.valueOf(this.spread));
        s += String.format("§2%s: §b%s\n", "Spread Crouched", String.valueOf(this.spread_crouched));
        s += String.format("§2%s: §b%s\n", "Magazine Size", String.valueOf(this.magSize));
        s += String.format("§2%s: §b%s\n", "Maximum Reserve", String.valueOf(this.maxAmmo));
        s += String.format("§2%s: §b%sms\n", "Reload Time", String.valueOf(this.reloadTime));
        s += String.format("§2%s: §b(%s, %s, %s)\n", "Bullet Offset", String.valueOf(this.bulletOffset.getX()), String.valueOf(this.bulletOffset.getY()), String.valueOf(this.bulletOffset.getZ()));
        s += String.format("§2%s: §b%s:%s\n", "Base Item", this.templateItemNormal.getType().toString(), String.valueOf(this.templateItemNormal.getDurability()));
        s += "§cPlayer Data:\n";
        s += String.format("§2%s: §b%s\n", "Last Shot", String.valueOf(this.playerSpecificData.get(p).getLastShot()));
        s += String.format("§2%s: §b%s\n", "Timeout Start", String.valueOf(this.playerSpecificData.get(p).getTimeoutStart()));
        s += String.format("§2%s: §b%s\n", "Timeout", String.valueOf(this.playerSpecificData.get(p).getTimeout()));
        s += String.format("§2%s: §b%s\n", "Ammo Left", String.valueOf(this.playerSpecificData.get(p).getAmmoLeft()));
        s += "§6================\n";

        return s;
    }

    public void showAmmo(Player p){
        this.showHotbarTitle(p, String.format("§e§l« %d | %d »", playerSpecificData.get(p).getAmmoLeft(), playerSpecificData.get(p).getTotalAmmoLeft()));
    }

    public void showHotbarTitle(Player p, String message){
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.ACTIONBAR, IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), 60, 10, 10);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);
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




















