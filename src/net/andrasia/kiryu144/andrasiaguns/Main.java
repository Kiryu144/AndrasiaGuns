package net.andrasia.kiryu144.andrasiaguns;

import net.andrasia.kiryu144.andrasiaguns.guns.Gun;
import net.andrasia.kiryu144.andrasiaguns.guns.GunManager;
import net.andrasia.kiryu144.andrasiaguns.input.Keyboard;
import net.andrasia.kiryu144.kiryucore.KiryuCore;
import net.andrasia.kiryu144.kiryucore.config.YamlConfig;
import net.andrasia.kiryu144.kiryucore.console.KiryuLogger;
import net.andrasia.kiryu144.kiryucore.util.KiryuSimpleSerialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin {
    public static Plugin instance;

    public void onEnable() {
        instance = this;
        KiryuCore.init(this, ChatColor.DARK_AQUA);
        this.getServer().getPluginManager().registerEvents(new GunManager(), this);
        this.getServer().getPluginManager().registerEvents(new Keyboard(), this);

        parseConfig();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, GunManager::handleReload, 40);
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if(cmd.getName().equalsIgnoreCase("ag")) {
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("reload")){
                    GunManager.getGun((Player) sender).initiateReload((Player) sender);
                    sender.sendMessage("Reloading ..");
                }else if(args[0].equalsIgnoreCase("info")){
                    sender.sendMessage(GunManager.getGun((Player) sender).generateInfo((Player) sender));
                }
            }else if(args.length == 2){
                if(args[0].equalsIgnoreCase("give")){
                    Gun gun = GunManager.getGun(Integer.valueOf(args[1]));
                    ((Player) sender).getInventory().addItem(gun.getTemplateItemNormal());
                    gun.getPlayerSpecificData().get(((Player) sender)).setTotalAmmoLeft(100);
                }
            }
            return true;
        }
        return false;
    }

    public void parseConfig() {
        YamlConfig config = new YamlConfig("./guns.yml");
        for(String key : config.getYamlConfiguration().getKeys(false)){
            ConfigurationSection weaponSection = config.getYamlConfiguration().getConfigurationSection(key);

            String name = weaponSection.getString("display_name");
            ItemStack baseItem = KiryuSimpleSerialization.getSimpleItemStack(weaponSection.getString("base_item"));
            int id = weaponSection.getInt("id");
            float damage = (float) weaponSection.getDouble("damage");
            float rate = (float) weaponSection.getDouble("rate");
            float spread = (float) weaponSection.getDouble("spread");
            float spread_crouched = (float) weaponSection.getDouble("spread_crouched");
            int magsize = weaponSection.getInt("magsize");
            int max_ammo_reserve = weaponSection.getInt("max_ammo_reserve");
            int reloadTime = weaponSection.getInt("reload_time");
            ConfigurationSection bulletOffset = weaponSection.getConfigurationSection("bullet_offset");
            ConfigurationSection sound = weaponSection.getConfigurationSection("sound");

            Gun gun = new Gun(id, name, damage, rate, spread, spread_crouched, magsize, max_ammo_reserve, reloadTime, new Vector(bulletOffset.getDouble("x"), bulletOffset.getDouble("y"), bulletOffset.getDouble("z")));
            gun.generateTemplates(baseItem.getType(), baseItem.getDurability());
            gun.setSounds(sound.getString("shooting"), sound.getString("bolt"), sound.getString("reloading"));
            GunManager.addGun(gun);
        }

        KiryuLogger.info(String.format("Parsed %d guns!", GunManager.getAmount()));
    }

}




















