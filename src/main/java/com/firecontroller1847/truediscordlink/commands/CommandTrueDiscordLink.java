package com.firecontroller1847.truediscordlink.commands;

import com.firecontroller1847.truediscordlink.FirePlugin;
import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandTrueDiscordLink implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        TrueDiscordLink plugin = (TrueDiscordLink) TrueDiscordLink.getInstance();

        // Handle Sub-Commands
        if (args.length > 0) {
            // Reload
            if (args[0].equalsIgnoreCase("reload")) {
                return TrueDiscordLink.runCommand(new CommandReload(plugin), "truediscordlink.command.reload", sender, command, label, args);
            }

            // Link
            if (args[0].equalsIgnoreCase("link")) {
                return TrueDiscordLink.runCommand(new CommandLink(plugin), "truediscordlink.command.link", sender, command, label, args);
            }

            // Unlink
            if (args[0].equalsIgnoreCase("unlink")) {
                return TrueDiscordLink.runCommand(new CommandUnlink(plugin), "truediscordlink.command.unlink", sender, command, label, args);
            }

            // Relay
            if (args[0].equalsIgnoreCase("relay")) {
                return TrueDiscordLink.runCommand(new CommandRelay(plugin, false), "truediscordlink.command.relay", sender, command, label, args);
            }
            if (args[0].equalsIgnoreCase("relaysilent")) {
                return TrueDiscordLink.runCommand(new CommandRelay(plugin, true), "truediscordlink.command.relaysilent", sender, command, label, args);
            }
        }

        // If it's not the command or any sub command, list all commands
        if (FirePlugin.hasPermission(sender, "truediscordlink.command.reload")) {
            sender.sendMessage("/" + label + " reload");
        }
        if (FirePlugin.hasPermission(sender, "truediscordlink.command.link")) {
            sender.sendMessage("/" + label + " link");
        }
        if (FirePlugin.hasPermission(sender, "truediscordlink.command.unlink")) {
            sender.sendMessage("/" + label + " unlink");
        }
        if (FirePlugin.hasPermission(sender, "truediscordlink.command.relay")) {
            sender.sendMessage("/" + label + " relay");
        }
        if (FirePlugin.hasPermission(sender, "truediscordlink.command.relaysilent")) {
            sender.sendMessage("/" + label + " relaysilent");
        }
        return true;
    }

}
