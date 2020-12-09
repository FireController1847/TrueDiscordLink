package com.visualfiredev.truediscordlink.commands;

import com.visualfiredev.javabase.Database;
import com.visualfiredev.truediscordlink.TrueDiscordLink;
import com.visualfiredev.truediscordlink.database.DbPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CommandUnlink implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        TrueDiscordLink discordlink = TrueDiscordLink.getInstance();

        // Check if linking is enabled
        if (!discordlink.getConfig().getBoolean("bot.linking.enabled")) {
            sender.sendMessage(discordlink.getTranslation("linking.disabled"));
            return true;
        }

        // Check against console
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("You cannot use this command from console!");
            return true;
        }

        // Fetch database & player
        Player player = (Player) sender;
        Database database = discordlink.getDatabaseManager().getDatabase();

        // Send processing because sometimes it can take a long time
        sender.sendMessage(discordlink.getTranslation("linking.waiting"));

        try {
            ArrayList<DbPlayer> results;
            results = database.select(DbPlayer.getTableSchema(database), "minecraft_uuid = '" + player.getUniqueId().toString() + "'", DbPlayer.class);
            if (results.size() == 0) {
                player.sendMessage(discordlink.getTranslation("linking.does_not_exist"));
                return true;
            }

            // Get user from Discord
            User user = discordlink.getDiscordManager().getApi().getUserById(results.get(0).getDiscordId()).get();
            if (user != null) {
                // Search for roles
                for (String guildAndRole : discordlink.getConfig().getStringList("bot.linking.roles")) {
                    String[] parts = guildAndRole.split(":");
                    String guildId = parts[0];
                    String roleId = parts[1];

                    // Search for guild
                    discordlink.getDiscordManager().getApi().getServerById(guildId).ifPresent(server -> {
                        // Search for role
                        server.getRoleById(roleId).ifPresent(role -> {
                            // Remove role from user
                            server.getMemberById(user.getId()).ifPresent(serverUser -> {
                                try {
                                    serverUser.removeRole(role);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        });
                    });
                }
            }

            // Delete From Database
            database.delete(DbPlayer.getTableSchema(database), "minecraft_uuid = '" + player.getUniqueId().toString() + "'");

            // Unlink
            player.sendMessage(discordlink.getTranslation("linking.unlink"));
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(discordlink.getTranslation("linking.failure"));
        }

        // The command always works
        return true;
    }

}
