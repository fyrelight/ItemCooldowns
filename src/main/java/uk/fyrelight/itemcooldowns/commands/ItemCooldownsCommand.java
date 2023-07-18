package uk.fyrelight.itemcooldowns.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import uk.fyrelight.itemcooldowns.ItemCooldownsPlugin;

public class ItemCooldownsCommand implements CommandExecutor {
    private final ItemCooldownsPlugin plugin;
    public ItemCooldownsCommand(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }
        if (!args[0].equalsIgnoreCase("reload")
                && sender.hasPermission("fyrelight.itemcooldowns.reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(Component.text("ItemCooldowns reloaded successfully.").color(NamedTextColor.GREEN));
            return true;
        }
        return false;
    }
}
