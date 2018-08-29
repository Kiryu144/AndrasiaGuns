package net.andrasia.kiryu144.andrasiaguns.guns;

import net.andrasia.kiryu144.andrasiaguns.Main;
import net.andrasia.kiryu144.andrasiaguns.event.PlayerShotEntityEvent;
import net.andrasia.kiryu144.andrasiaguns.event.PlayerShotKilledEvent;
import net.andrasia.kiryu144.andrasiaguns.external.NBTEditor;
import net.andrasia.kiryu144.andrasiaguns.input.Keyboard;
import org.bukkit.Bukkit;
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
    private static void onPlayerJoin(PlayerJoinEvent e){
        scoped.put(e.getPlayer(), false);
        for(Gun gun : guns.getGuns()){
            gun.registerPlayer(e.getPlayer());
        }
    }

    @EventHandler
    private static void onPlayerLeave(PlayerQuitEvent e){
        scoped.remove(e.getPlayer());
        for(Gun gun : guns.getGuns()){
            gun.unregisterPlayer(e.getPlayer());
        }
    }

}








/*
public class GunManager implements Listener {
    private static GunContainer guns = new GunContainer();
    private static HashMap<Player, Boolean> scoped = new HashMap<>();

    public GunManager() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, ()->{
            for(Player p : Bukkit.getOnlinePlayers()){
                if(Keyboard.isPressed(p)){
                    ItemStack item = p.getInventory().getItemInMainHand();
                    if(item != null){
                        Object id = NBTEditor.getItemTag(item, "gunid");
                        if(id != null){
                            guns.get((Integer) id).handleShooting(p);
                        }
                    }
                }
            }
        }, 0, 1);
    }

    public static void addGun(Gun gun){
        guns.put(gun.getId(), gun);
    }

    public static GunContainer getGunContainer() {
        return guns;
    }

    public static boolean isScoped(Player p){
        return scoped.get(p);
    }

    @EventHandler
    private static void onHit(ProjectileHitEvent e){
        Projectile projectile = e.getEntity();
        LivingEntity victim = (e.getHitEntity() instanceof LivingEntity) ? (LivingEntity) e.getHitEntity() : null;

        if(projectile.getCustomName() != null && victim != null){
            BulletData bullet = BulletDatabase.getBullet(Integer.valueOf(projectile.getCustomName()));
            PlayerShotEntityEvent event = new PlayerShotEntityEvent(bullet.getShooter(), victim, bullet.getGun(), bullet);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if(!event.isCancelled()){
                victim.damage(bullet.getDamage(), bullet.getShooter());
            }
        }
    }

    @EventHandler
    private static void onPlayerDeath(PlayerDeathEvent e){
        if(e.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK){
            ItemStack item = e.getEntity().getKiller().getInventory().getItemInMainHand();
            Object gunID = (item != null) ? NBTEditor.getItemTag(item, "gunid") : null;
            if(gunID != null) {
                Bukkit.getServer().getPluginManager().callEvent(new PlayerShotKilledEvent(e.getEntity(), e.getEntity().getKiller(), GunManager.getGunContainer().get((Integer)gunID)));
            }
        }
    }

    @EventHandler
    private static void onPlayerInteract(PlayerInteractEvent e){
        if((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getHand() == EquipmentSlot.HAND ){
            ItemStack item = e.getItem();
            Gun gun = null;
            if(item != null){
                Object gunId = NBTEditor.getItemTag(item, "gunid");
                if(gunId != null){
                    gun = getGunContainer().get((Integer) gunId);
                }
            }

            if(gun == null){
                return;
            }

            if(!scoped.containsKey(e.getPlayer())){
                scoped.put(e.getPlayer(), false);
            }

            boolean isScoped = !scoped.get(e.getPlayer());
            scoped.put(e.getPlayer(), isScoped);
            if(isScoped){
                e.getPlayer().getInventory().setItemInMainHand(gun.getTemplateItemAimed());
            }else{
                e.getPlayer().getInventory().setItemInMainHand(gun.getTemplateItemNormal());
            }
        }
    }

    @EventHandler
    private static void onPlayerJoin(PlayerJoinEvent e){
        scoped.put(e.getPlayer(), false);
    }

    @EventHandler
    private static void onPlayerLeave(PlayerQuitEvent e){
        scoped.remove(e.getPlayer());
    }

    @EventHandler
    private static void onPlayerDrop(PlayerDropItemEvent e){

    }

}*/




















