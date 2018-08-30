package net.andrasia.kiryu144.andrasiaguns.guns;

import net.andrasia.kiryu144.andrasiaguns.Main;
import net.andrasia.kiryu144.andrasiaguns.event.PlayerShotEntityEvent;
import net.andrasia.kiryu144.andrasiaguns.event.PlayerShotKilledEvent;
import net.andrasia.kiryu144.andrasiaguns.external.NBTEditor;
import net.andrasia.kiryu144.andrasiaguns.input.Keyboard;
import net.andrasia.kiryu144.kiryucore.console.KiryuLogger;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class GunManager implements Listener {
    private static GunContainer guns = new GunContainer();
    private static HashMap<Player, Boolean> scoped = new HashMap<>();

    public GunManager() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, GunManager::handleShooting, 1, 1);
    }

    private static void handleShooting() {
        for(Player p : Bukkit.getOnlinePlayers()){
            if(Keyboard.isPressed(p)){
                Gun gun = getGun(p);
                if(gun != null){
                    gun.handleShooting(p);
                }
            }
        }
    }

    public static int getAmount() {
        return guns.getGuns().size();
    }

    public static void handleReload() {
        for(Player p : Bukkit.getOnlinePlayers()){
            onPlayerJoin(new PlayerJoinEvent(p, ""));
        }
    }

    public static void addGun(Gun gun){
        guns.put(gun.getId(), gun);
    }

    public static Gun getGun(int id){
        return guns.get(id);
    }

    public static Gun getGun(ItemStack item){
        if(item != null){
            Integer gunId = (Integer) NBTEditor.getItemTag(item, "gunid");
            if(gunId != null){
                return guns.get(gunId);
            }
        }
        return null;
    }

    public static Gun getGun(Player p){
        return getGun(p.getInventory().getItemInMainHand());
    }

    public static boolean isPlayerScoped(Player p){
        return scoped.get(p);
    }

    @EventHandler
    private static void onPlayerDrop(PlayerDropItemEvent e){
        Gun gun = getGun(e.getItemDrop().getItemStack());
        if(gun != null){
            e.setCancelled(true);
            gun.initiateReload(e.getPlayer());
        }
    }

    @EventHandler
    private static void onPlayerDeath(PlayerDeathEvent e){
        Gun gun = getGun(e.getEntity().getKiller());
        if(gun != null){
            Bukkit.getServer().getPluginManager().callEvent(new PlayerShotKilledEvent(e.getEntity(), e.getEntity().getKiller(), gun));
        }
    }

    @EventHandler
    private static void onPlayerInteract(PlayerInteractEvent e){
        if(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
            if(e.getHand().equals(EquipmentSlot.HAND)){
                Gun gun = getGun(e.getItem());
                if(gun != null){
                    scoped.put(e.getPlayer(), !scoped.get(e.getPlayer()));

                    if(scoped.get(e.getPlayer())){
                        e.getPlayer().getInventory().setItemInMainHand(gun.getTemplateItemAimed());
                    }else{
                        e.getPlayer().getInventory().setItemInMainHand(gun.getTemplateItemNormal());
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onProjectileHit(ProjectileHitEvent e){
        if(e.getEntity().getType() == EntityType.SNOWBALL){
            Projectile snowball = e.getEntity();
            if(snowball.getCustomName() != null){
                int id = Integer.valueOf(snowball.getCustomName());
                BulletData bullet = BulletDatabase.getBullet(id);
                if(e.getHitEntity() != null) {
                    ((LivingEntity) e.getHitEntity()).damage(bullet.getDamage(), bullet.getShooter());
                }

                BulletDatabase.removeBullet(id);
            }
        }
    }

    @EventHandler
    private static void onPlayerJoin(PlayerJoinEvent e){
        scoped.put(e.getPlayer(), false);
        for(Gun gun : guns.getGuns()){
            gun.registerPlayer(e.getPlayer());
        }
        Keyboard.registerPlayer(e.getPlayer());
    }

    @EventHandler
    private static void onPlayerLeave(PlayerQuitEvent e){
        scoped.remove(e.getPlayer());
        for(Gun gun : guns.getGuns()){
            gun.unregisterPlayer(e.getPlayer());
        }
        Keyboard.unregisterPlayer(e.getPlayer());
    }

    @EventHandler
    public static void switchSlots(PlayerItemHeldEvent e){
        Gun gun = getGun(e.getPlayer().getInventory().getItem(e.getNewSlot()));
        if(gun != null){
            gun.showAmmo(e.getPlayer());
        }
    }

}






