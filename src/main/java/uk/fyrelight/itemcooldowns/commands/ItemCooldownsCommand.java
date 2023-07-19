package uk.fyrelight.itemcooldowns.commands;

import org.bukkit.command.*;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.fyrelight.itemcooldowns.ItemCooldownsPlugin;
import uk.fyrelight.itemcooldowns.objects.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemCooldownsCommand implements TabExecutor {
    private final List<String> subcommands = List.of("reload");
    private final ItemCooldownsPlugin plugin;
    public ItemCooldownsCommand(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            Messages.COMMAND_NO_PERMISSION.sendMessage(sender);
            return false;
        }
        return true;
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
        if (!checkPermission(sender, "itemcooldowns.command")) {
            return true;
        }
        if (args == null || args.length == 0) {
            Messages.COMMAND_DEFAULT.sendMessage(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!checkPermission(sender, "itemcooldowns.command.reload")) {
                return true;
            }
            if (plugin.reloadPlugin()) {
                Messages.COMMAND_RELOAD_SUCCESS.sendMessage(sender);
            } else {
                Messages.COMMAND_RELOAD_FAILURE.sendMessage(sender);
            }
            return true;
        }
        Messages.COMMAND_INVALID_SUBCOMMAND.sendMessage(sender);
        return true;
    }

}
