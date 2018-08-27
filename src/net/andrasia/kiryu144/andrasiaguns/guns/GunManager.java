package net.andrasia.kiryu144.andrasiaguns.guns;

import net.andrasia.kiryu144.andrasiaguns.Main;
import net.andrasia.kiryu144.andrasiaguns.event.PlayerShotEntityEvent;
import net.andrasia.kiryu144.andrasiaguns.event.PlayerShotKilledEvent;
import net.andrasia.kiryu144.andrasiaguns.external.NBTEditor;
import net.andrasia.kiryu144.andrasiaguns.input.Keyboard;
import net.andrasia.kiryu144.kiryucore.console.KiryuLogger;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.util.HashMap;

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
                scoped.put(e.getPlayer(), true);
            }else{
                boolean isScoped = !scoped.get(e.getPlayer());
                scoped.put(e.getPlayer(), isScoped);
                if(isScoped){
                    e.getPlayer().getInventory().setItemInMainHand(gun.getItemStackAimed());
                }else{
                    e.getPlayer().getInventory().setItemInMainHand(gun.getItemStack());
                }
            }
        }
    }

}




















