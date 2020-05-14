package com.visualfiredev.truediscordlink.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandTrueDiscordLink implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Main Command
        if (args.length == 0) {
            if (!CommandUtil.hasPermission(sender, "truediscordlink.command.discord")) {
                CommandUtil.tellNoPermission(sender);
                return true;
            }

            return false; // TODO: Return list of commands :)

        // Reload Command
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!CommandUtil.hasPermission(sender, "truediscordlink.command.reload")) {
                CommandUtil.tellNoPermission(sender);
                return true;
            }

            return (new CommandConfigReload()).onCommand(sender, command, label, args);
        }

        // If it's not the command or any sub command, return false
        return false;
    }

}
