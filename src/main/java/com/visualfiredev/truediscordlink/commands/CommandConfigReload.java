package com.visualfiredev.truediscordlink.commands;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandConfigReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Reload Config & Clear Cache
        TrueDiscordLink instance = TrueDiscordLink.getInstance();
        instance.reloadConfig();
        instance.getPlayerChatListener().reset();
        instance.getDiscordChatListener().reset();

        // Send Notification Message
        sender.sendMessage("Config reloaded!");

        // Return True as Command Always Works
        return true;
    }

}
