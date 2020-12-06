package com.visualfiredev.truediscordlink.commands;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Reload config
        TrueDiscordLink discordlink = TrueDiscordLink.getInstance();
        discordlink.reloadConfig();
        discordlink.loadTranslations();

        // Send notification message
        sender.sendMessage(discordlink.getTranslation("config.reloaded"));

        // The command always works
        return true;
    }

}
