package com.firecontroller1847.truediscordlink.commands;

import com.firecontroller1847.truediscordlink.*;
import com.firecontroller1847.truediscordlink.database.DbPlayer;
import com.visualfiredev.javabase.Database;
import com.visualfiredev.javabase.DatabaseValue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CommandLink extends FireCommand {

    public CommandLink(FirePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Check if linking is enabled
        if (!plugin.getConfig().getBoolean("bot.linking.enabled")) {
            sender.sendMessage(plugin.getTranslation("linking.disabled"));
            return true;
        }

        // Check against console
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(plugin.getTranslation("no_console_usage"));
            return true;
        }

        // Fetch database & player
        Player player = (Player) sender;
        DatabaseManager databaseManager = ((TrueDiscordLink) plugin).getDatabaseManager();
        Database database = databaseManager.getDatabase();

        // Validate database connection
        databaseManager.validateConnection();

        // Check if user is in database
        try {
            ArrayList<DbPlayer> results;
            results = database.select(DbPlayer.getTableSchema(database), DbPlayer.class, "minecraft_uuid = ?", player.getUniqueId().toString());

            // Store dbPlayer
            DbPlayer dbPlayer;

            // If we aren't in the database...
            if (results.size() == 0) {

                // Create new dbPlayer
                DbPlayer dbPlayerNew = new DbPlayer(player.getName(), player.getUniqueId().toString(), null, false);
                database.insert(dbPlayerNew);

                // Re-fetch DbPlayer
                dbPlayer = database.select(DbPlayer.getTableSchema(database), DbPlayer.class, "minecraft_uuid = ?", player.getUniqueId().toString()).get(0);

            // If we are in the database...
            } else {
                // Use existing
                dbPlayer = results.get(0);

                // Already linked
                if (dbPlayer.isLinked()) {
                    player.sendMessage(plugin.getTranslation("linking.exists"));
                    return true;
                }
            }


            // If the results are confirm, mark we're now linked and perform actions
            // If the results are deny, note that the user can type the first four letters of their username in
            // If the results are neither confirm nor deny, assume we're still trying to link
            if (args.length > 1 && isConfirm(args[1])) {
                // Send DM message
                ((TrueDiscordLink) plugin).getDiscordManager().getApi().getUserById(dbPlayer.getDiscordId()).thenAccept(user -> {
                    user.sendMessage(plugin.getTranslation("linking.discord.request", new String[] { "%discord_prefix%", plugin.getConfig().getString("bot.discord.prefix") }));

                    // Send "Check Discord" Message
                    player.sendMessage(plugin.getTranslation("linking.minecraft.check_discord"));
                }).exceptionally(e -> {
                    sender.sendMessage(plugin.getTranslation("linking.minecraft.missing"));
                    return null;
                });
            } else if (args.length > 1 && isDeny(args[1])) {
                // Send "Help" Message
                player.sendMessage(plugin.getTranslation("linking.minecraft.i_am_not"));
            } else {
                // Get server from Discord
                Server server = ((TrueDiscordLink) plugin).getDiscordManager().getApi().getServerById(plugin.getConfig().getString("bot.linking.server")).orElseThrow(() -> new Exception("Linking server cannot be null!"));

                // Find user on Discord
                ArrayList<User> possibleUsers = DiscordManager.getUsersPartiallyMatching(server, args.length > 1 ? args[1] : player.getName().substring(0, 4), true);
                if (possibleUsers.size() == 0) {
                    sender.sendMessage(plugin.getTranslation("linking.minecraft.missing"));
                    return true;
                }

                // Ask Question
                sender.sendMessage(plugin.getTranslation("linking.minecraft.question", new String[] { "%message%", possibleUsers.get(0).getDiscriminatedName() }));

                // Update DbPlayer with the new potential discord_id
                database.update(DbPlayer.getTableSchema(database), "minecraft_uuid = '" + player.getUniqueId().toString() + "'",
                    new DatabaseValue("discord_id", possibleUsers.get(0).getIdAsString())
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(plugin.getTranslation("linking.failure"));
        }

        // The command always works
        return true;
    }

    public static boolean isConfirm(String value) {
        return value.equalsIgnoreCase("indeed") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("y");
    }

    public static boolean isDeny(String value) {
        return value.equalsIgnoreCase("never") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("n");
    }

}
