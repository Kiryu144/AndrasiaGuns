package net.andrasia.kiryu144.andrasiaguns.guns;

import net.andrasia.kiryu144.andrasiaguns.external.NBTEditor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.SplittableRandom;

public class Gun {
    private static SplittableRandom random = new SplittableRandom(System.currentTimeMillis());
    private HashMap<Player, Long> lastShot = new HashMap<>();
    private ItemStack template;
    private ItemStack templateAimed;
    private String displayname;
    private float damage;
    private long delay;
    private float spread;
    private int id;
    private float speed;
    private String data;

    public Gun(ItemStack base, int id, String displayname, float damage, float rate, float speed, float spread){

        this.displayname = displayname;
        this.damage = damage;
        this.delay = Math.round(1000/rate);
        this.spread = spread;
        this.id = id;
        this.speed = speed;

        this.template = new ItemStack(base.getType(), 1, base.getDurability());
        ItemMeta meta = this.template.getItemMeta();
        meta.setDisplayName(displayname);
        this.template.setItemMeta(meta);
        this.template = NBTEditor.setItemTag(this.template, id, "gunid");

        this.templateAimed = this.template.clone();
        this.templateAimed.setDurability(((short) (template.getDurability()+1)));
    }

    public String getDisplayName() {
        return displayname;
    }

    public float getDamage() {
        return damage;
    }

    public float getDelay() {
        return delay;
    }

    public float getSpread() {
        return spread;
    }

    public ItemStack getItemStack() {
        return template;
    }

    public ItemStack getItemStackAimed() {
        return templateAimed;
    }

    public int getId() {
        return id;
    }

    public void handleShooting(Player shooter) {
        Long last = lastShot.get(shooter);
        Long curr = System.currentTimeMillis();
        if(last == null || curr - last > delay){
            lastShot.put(shooter, curr);

            float yaw = -((float)Math.PI*2/360 * (shooter.getLocation().getYaw() + 25));
            Location loc = shooter.getEyeLocation();
            double radius = 1.2;
            loc.add(Math.sin(yaw) * radius, -0.3, Math.cos(yaw) * radius);

            Projectile snowball = shooter.getWorld().spawn(loc, Snowball.class);
            int b_id = BulletDatabase.addBullet(new BulletData(snowball, shooter, this, this.damage));

            snowball.setCustomName(String.valueOf(b_id));
            snowball.setGravity(false);
            snowball.setVelocity(shooter.getLocation().getDirection().normalize().multiply(speed));

            shooter.playSound(shooter.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 3.0f, 3.0f);
        }
    }
}

























