package uk.fyrelight.itemcooldowns;

import org.bukkit.plugin.java.JavaPlugin;
import uk.fyrelight.itemcooldowns.commands.ItemCooldownsCommand;
import uk.fyrelight.itemcooldowns.listeners.CooldownsListener;

public class ItemCooldownsPlugin extends JavaPlugin {
    public void reloadPlugin() {
        this.reloadConfig();
        this.getLogger().info("Reloaded config.");
    }
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getCommand("itemcooldowns").setExecutor(new ItemCooldownsCommand(this));
        getServer().getPluginManager().registerEvents(new CooldownsListener(this), this);
    }
}
