package uk.fyrelight.itemcooldowns;

import org.bukkit.plugin.java.JavaPlugin;
import uk.fyrelight.itemcooldowns.commands.ItemCooldownsCommand;
import uk.fyrelight.itemcooldowns.listeners.CooldownsListener;

public class ItemCooldownsPlugin extends JavaPlugin {
    private CooldownsListener listener;
    public boolean reloadPlugin() {
        this.reloadConfig();
        listener.reloadListener();
        return true;
    }
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getCommand("itemcooldowns").setExecutor(new ItemCooldownsCommand(this));
        this.listener = new CooldownsListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
