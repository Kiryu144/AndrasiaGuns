package net.andrasia.kiryu144.andrasiaguns;

import net.andrasia.kiryu144.andrasiaguns.guns.Gun;
import net.andrasia.kiryu144.andrasiaguns.guns.GunManager;
import net.andrasia.kiryu144.andrasiaguns.input.Keyboard;
import net.andrasia.kiryu144.kiryucore.KiryuCore;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;

public class Main extends JavaPlugin {
    public static Plugin instance;

    public void onEnable() {
        instance = this;
        KiryuCore.init(this, ChatColor.DARK_GREEN);
        this.getServer().getPluginManager().registerEvents(new GunManager(), this);
        this.getServer().getPluginManager().registerEvents(new Keyboard(), this);

        GunManager.addGun(new Gun(new ItemStack(Material.IRON_HOE), 1, "DMR", 15, 10, 3.5f, 50));
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if(cmd.getName().equalsIgnoreCase("ag")) {
            ((Player) sender).getInventory().addItem(GunManager.getGunContainer().get(1).getItemStack());
            return true;
        }
        return false;
    }

}
