package com.firecontroller1847.truediscordlink.commands;

import com.firecontroller1847.truediscordlink.DiscordManager;
import com.firecontroller1847.truediscordlink.FireCommand;
import com.firecontroller1847.truediscordlink.FirePlugin;
import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CommandRelay extends FireCommand {

    private boolean silent;

    public CommandRelay(FirePlugin plugin, boolean silent) {
        super(plugin);
        this.silent = silent;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Get Discord manager
        DiscordManager discordManager = ((TrueDiscordLink) this.getPlugin()).getDiscordManager();

        // Check if you can communicate with Discord
        if (!discordManager.canCommunicateWithDiscord()) {
            sender.sendMessage(plugin.getTranslation("commands.relay.no_discord_communication"));
            return true;
        }

        // Message
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Handle empty message
        if (message.isEmpty()) {
            sender.sendMessage(plugin.getTranslation("commands.relay.no_message"));
            return true;
        }

        // Send the message to Discord
        discordManager.sendDiscordMessage(String.join(" ", message));

        // Respond to sender
        if (!silent) {
            sender.sendMessage(plugin.getTranslation("commands.relay.success"));
        }

        // The command always works
        return true;
    }

}
