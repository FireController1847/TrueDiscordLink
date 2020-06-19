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
        instance.loadLangConfig();
        try {
            instance.validateConfig();
        } catch (Exception e) {
            sender.sendMessage(instance.getLangString("config.error"));
            return true;
        }
        instance.getDiscordManager().statusLoop(0);
        instance.getDiscordManager().channelTopicLoop();

        // Send Notification Message
        sender.sendMessage(instance.getLangString("config.reloaded"));

        // Return True as Command Always Works
        return true;
    }

}
