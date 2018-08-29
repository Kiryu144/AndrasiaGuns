package net.andrasia.kiryu144.andrasiaguns;

import net.andrasia.kiryu144.andrasiaguns.guns.Gun;
import net.andrasia.kiryu144.andrasiaguns.guns.GunManager;
import net.andrasia.kiryu144.andrasiaguns.input.Keyboard;
import net.andrasia.kiryu144.kiryucore.KiryuCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

public class Main extends JavaPlugin {
    public static Plugin instance;

    public void onEnable() {
        instance = this;
        KiryuCore.init(this, ChatColor.DARK_GREEN);
        this.getServer().getPluginManager().registerEvents(new GunManager(), this);
        this.getServer().getPluginManager().registerEvents(new Keyboard(), this);

        Gun dmr = new Gun(1, "DMR", 6.5f, 5.0f, 0, 10, 3500);
        dmr.generateTemplates(Material.IRON_HOE, (short)0);
        GunManager.addGun(dmr);
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if(cmd.getName().equalsIgnoreCase("ag")) {
            if(args.length == 2){
                if(args[0].equalsIgnoreCase("give")){
                    ((Player) sender).getInventory().addItem(GunManager.getGun(Integer.valueOf(args[1])).getTemplateItemNormal());
                }
                if(args[0].equalsIgnoreCase("reload")){
                    GunManager.getGun(Integer.valueOf(args[1])).initiateReload(((Player) sender));
                    sender.sendMessage("Reloading ..");
                }
            }
            return true;
        }
        return false;
    }

}
