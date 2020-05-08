package com.visualfiredev.truediscordlink.commands;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CommandTrueDiscordLink implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Main Command
        if (args.length == 0) {
            return false; // TODO: Return list of commands :)

        // Reload Command
        } else if (args[0].equalsIgnoreCase("reload")) {
            return (new CommandConfigReload()).onCommand(sender, command, label, args);
        }

        // If it's not the command or any sub command, return false
        return false;
    }

}
