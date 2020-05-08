package com.visualfiredev.truediscordlink.commands;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandConfigReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        TrueDiscordLink.getInstance().reloadConfig();
        TrueDiscordLink.getInstance().getPlayerChatHandler().reset();
        TrueDiscordLink.getInstance().getDiscordChatHandler().reset();
        sender.sendMessage("Config reloaded!");

        // The command always works as expected
        return true;
    }

}
