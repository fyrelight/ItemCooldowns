package uk.fyrelight.itemcooldowns.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.fyrelight.itemcooldowns.ItemCooldownsPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemCooldownsCommand implements TabExecutor {
    private final List<String> subcommands = List.of("reload");
    private final ItemCooldownsPlugin plugin;
    public ItemCooldownsCommand(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            for (String subcommand : subcommands) {
                if (!sender.hasPermission("itemcooldowns.command." + subcommand)) {
                    continue;
                }
                options.add(subcommand);
            }
            return StringUtil.copyPartialMatches(args[0], options, new ArrayList<>());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("itemcooldowns.command")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return true;
        }
        if (args == null || args.length == 0) {
            sender.sendMessage(Component.text("ItemCooldowns by Lexicon").color(NamedTextColor.GOLD));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("itemcooldowns.command.reload")) {
                sender.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
                return true;
            }
            if (plugin.reloadPlugin()) {
                sender.sendMessage(Component.text("ItemCooldowns reloaded successfully.").color(NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("ItemCooldowns failed to reload.").color(NamedTextColor.RED));
            }
            return true;
        }
        sender.sendMessage(Component.text("Invalid subcommand.").color(NamedTextColor.RED));
        return true;
    }

}
