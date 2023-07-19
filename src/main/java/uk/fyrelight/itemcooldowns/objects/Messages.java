package uk.fyrelight.itemcooldowns.objects;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import uk.fyrelight.itemcooldowns.ItemCooldownsPlugin;

public enum Messages {
    COOLDOWN("cooldown", "<red>You can use this again in <white>%s<red> seconds."),
    COMMAND_DEFAULT("command.default", "<gold>ItemCooldowns <yellow>by Lexicon"),
    COMMAND_NO_PERMISSION("command.no_permission", "<red>You do not have permission to use this command."),
    COMMAND_INVALID_SUBCOMMAND("command.invalid_subcommand", "<red>Invalid subcommand."),
    COMMAND_RELOAD_SUCCESS("command.reload.successful", "<green>Reloaded successfully."),
    COMMAND_RELOAD_FAILURE("command.reload.failure", "<red>Failed to reload."),
    ;

    private final String path;
    private final String defaultMessage;
    Messages(String path, String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }

    private String getFormattedString(Object[] replacements) {
        String unformattedString = ItemCooldownsPlugin.getInstance().getMessages().getString(path, defaultMessage);
        return unformattedString.formatted(replacements);
    }

    private Component getMessage(Object[] replacements) {
        String messageString = getFormattedString(replacements);
        if (messageString.isEmpty()) return null;
        return MiniMessage.miniMessage().deserialize(messageString);
    }

    private Component getPrefix() {
        String unformattedString = ItemCooldownsPlugin.getInstance().getMessages().getString("prefix", "<gold>[<yellow>ItemCooldowns<gold>] ");
        return MiniMessage.miniMessage().deserialize(unformattedString);
    }

    public void sendPrefixedMessage(Audience audience, Object... replacements) {
        Component messageToSend = getMessage(replacements);
        if (messageToSend == null) return;
        audience.sendMessage(getPrefix().append(messageToSend));
    }

    public void sendMessage(Audience audience, Object... replacements) {
        Component messageToSend = getMessage(replacements);
        if (messageToSend == null) return;
        audience.sendMessage(messageToSend);
    }

    public void sendActionBar(Audience audience, Object... replacements) {
        Component messageToSend = getMessage(replacements);
        if (messageToSend == null) return;
        audience.sendActionBar(messageToSend);
    }
}
