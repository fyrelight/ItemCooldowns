package uk.fyrelight.itemcooldowns;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import uk.fyrelight.itemcooldowns.commands.ItemCooldownsCommand;
import uk.fyrelight.itemcooldowns.listeners.CooldownsListener;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class ItemCooldownsPlugin extends JavaPlugin {
    private static ItemCooldownsPlugin instance;
    private final File messagesFile = new File(getDataFolder(), "messages.yml");
    private YamlConfiguration messages;

    public YamlConfiguration getMessages() {
        if (messages == null) {
            // Equivalent to reloadConfig()
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            InputStream defaultMessages = getResource("messages.yml");
            if (defaultMessages != null) {
                messages.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultMessages, Charsets.UTF_8)));
            }
        }
        return messages;
    }

    public boolean reloadPlugin() {
        reloadConfig();
        CooldownsListener.reload();
        return true;
    }

    public void registerCommands() {
        Objects.requireNonNull(getCommand("itemcooldowns")).setExecutor(new ItemCooldownsCommand(this));
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new CooldownsListener(), this);
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);
        registerCommands();
        registerListeners();
        reloadPlugin();
    }

    public static ItemCooldownsPlugin getInstance() {
        return instance;
    }
}
