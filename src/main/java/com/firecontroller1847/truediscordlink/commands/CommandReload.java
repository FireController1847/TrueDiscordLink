package com.firecontroller1847.truediscordlink.commands;

import com.firecontroller1847.truediscordlink.FireCommand;
import com.firecontroller1847.truediscordlink.FirePlugin;
import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandReload extends FireCommand {

    public CommandReload(FirePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Reload config
        TrueDiscordLink discordlink = (TrueDiscordLink) plugin;
        discordlink.reload();

        // Reconnect database
        discordlink.getDatabaseManager().disconnect();
        discordlink.getDatabaseManager().connect();

        // Send notification message
        sender.sendMessage(discordlink.getTranslation("config.reloaded"));

        // The command always works
        return true;
    }

}
