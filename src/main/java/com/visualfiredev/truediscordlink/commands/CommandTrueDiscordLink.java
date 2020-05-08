package com.visualfiredev.truediscordlink.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandTrueDiscordLink implements CommandExecutor {

    // Handle Command Execution
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Base Command
        if (args.length == 0) {
            return false; // Return false; TODO: Somehow make this show the help page for it...

        // Reload Command
        } else if (args[0].equalsIgnoreCase("reload")) {
            return (new CommandConfigReload()).onCommand(sender, command, label, args);
        }

        // If it's not the command or any sub command, return false
        return false;
    }

}
