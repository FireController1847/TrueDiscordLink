package com.visualfiredev.truediscordlink.commands;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public final class CommandUtil {

    public static boolean hasPermission(CommandSender sender, String permission) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission(permission);
    }

    public static void tellNoPermission(CommandSender sender) {
        TrueDiscordLink instance = TrueDiscordLink.getInstance();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getLangString("no_permission")));
    }

}
