package com.visualfiredev.truediscordlink.commands;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandTrueDiscordLink implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle Sub-Commands
        if (args.length > 0) {
            // Reload
            if (args[0].equalsIgnoreCase("reload")) {
                return TrueDiscordLink.runCommand(new CommandReload(), "truediscordlink.command.reload", sender, command, label, args);
            }

            // Link
            if (args[0].equalsIgnoreCase("link")) {
                return TrueDiscordLink.runCommand(new CommandLink(), "truediscordlink.command.link", sender, command, label, args);
            }

            // Unlink
            if (args[0].equalsIgnoreCase("unlink")) {
                return TrueDiscordLink.runCommand(new CommandUnlink(), "truediscordlink.command.unlink", sender, command, label, args);
            }
        }

        // If it's not the command or any sub command, return false
        // TODO: Return a list of commands
        return false;
    }

}
