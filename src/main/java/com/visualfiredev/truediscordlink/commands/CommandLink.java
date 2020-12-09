package com.visualfiredev.truediscordlink.commands;

import com.visualfiredev.javabase.Database;
import com.visualfiredev.javabase.DatabaseValue;
import com.visualfiredev.truediscordlink.DiscordManager;
import com.visualfiredev.truediscordlink.TrueDiscordLink;
import com.visualfiredev.truediscordlink.database.DbPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CommandLink implements CommandExecutor {

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

        // Check if user is in database
        try {
            ArrayList<DbPlayer> results;
            results = database.select(DbPlayer.getTableSchema(database), "minecraft_uuid = '" + player.getUniqueId().toString() + "'", DbPlayer.class);

            // Store dbPlayer
            DbPlayer dbPlayer;

            // If we aren't in the database...
            if (results.size() == 0) {

                // Create new dbPlayer
                DbPlayer dbPlayerNew = new DbPlayer(player.getName(), player.getUniqueId().toString(), null, false);
                database.insert(dbPlayerNew);

                // Re-fetch DbPlayer
                dbPlayer = database.select(DbPlayer.getTableSchema(database), "minecraft_uuid = '" + player.getUniqueId().toString() + "'", DbPlayer.class).get(0);

            // If we are in the database...
            } else {
                // Use existing
                dbPlayer = results.get(0);

                // Already linked
                if (dbPlayer.isLinked()) {
                    player.sendMessage(discordlink.getTranslation("linking.exists"));
                    return true;
                }
            }


            // If the results are confirm, mark we're now linked and perform actions
            // If the results are deny, note that the user can type the first four letters of their username in
            // If the results are neither confirm nor deny, assume we're still trying to link
            if (args.length > 1 && isConfirm(args[1])) {
                // Send DM message
                User user = discordlink.getDiscordManager().getApi().getUserById(dbPlayer.getDiscordId()).get();
                user.sendMessage(discordlink.getTranslation("linking.discord.request", new String[] { "%discord_prefix%", discordlink.getConfig().getString("bot.discord.prefix") }));

                // Send "Check Discord" Message
                player.sendMessage(discordlink.getTranslation("linking.minecraft.check_discord"));
            } else if (args.length > 1 && isDeny(args[1])) {
                // Send "Help" Message
                player.sendMessage(discordlink.getTranslation("linking.minecraft.i_am_not"));
            } else {
                // Get server from Discord
                Server server = discordlink.getDiscordManager().getApi().getServerById(discordlink.getConfig().getString("bot.linking.server")).orElseThrow(() -> new Exception("Linking server cannot be null!"));

                // Find user on Discord
                ArrayList<User> possibleUsers = DiscordManager.getUsersPartiallyMatching(server, args.length > 1 ? args[1] : player.getName().substring(0, 4), true);
                if (possibleUsers.size() == 0) {
                    sender.sendMessage(discordlink.getTranslation("linking.minecraft.missing"));
                    return true;
                }

                // Ask Question
                sender.sendMessage(discordlink.getTranslation("linking.minecraft.question", new String[] { "%message%", possibleUsers.get(0).getDiscriminatedName() }));

                // Update DbPlayer with the new potential discord_id
                database.update(DbPlayer.getTableSchema(database), "minecraft_uuid = '" + player.getUniqueId().toString() + "'",
                    new DatabaseValue("discord_id", possibleUsers.get(0).getIdAsString())
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(discordlink.getTranslation("linking.failure"));
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
