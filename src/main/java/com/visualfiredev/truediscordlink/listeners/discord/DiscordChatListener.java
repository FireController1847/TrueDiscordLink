package com.visualfiredev.truediscordlink.listeners.discord;

import com.visualfiredev.javabase.Database;
import com.visualfiredev.javabase.DatabaseValue;
import com.visualfiredev.truediscordlink.DiscordManager;
import com.visualfiredev.truediscordlink.TrueDiscordLink;
import com.visualfiredev.truediscordlink.database.DbPlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class DiscordChatListener implements MessageCreateListener {

    // Variables
    private TrueDiscordLink discordlink;
    private DiscordManager manager;

    // Constructor
    public DiscordChatListener(TrueDiscordLink discordlink, DiscordManager manager) {
        this.discordlink = discordlink;
        this.manager = manager;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message message = event.getMessage();

        // Exclude System Messages, & Webhook Messages
        if (message.getAuthor() == null || message.getAuthor().isYourself() || message.getAuthor().isWebhook()) {
            return;
        }

        // Prefix
        String prefix = discordlink.getConfig().getString("bot.discord.prefix");
        if (prefix == null || prefix.isEmpty()) {
            prefix = "tdl!";
        }

        // Check for prefix / mention to handle commands
        if (
            message.getContent().startsWith(prefix) ||
            (message.getMentionedUsers().size() > 0 && message.getMentionedUsers().contains(manager.getApi().getYourself()))
        ) {
            String content = message.getContent().replace("<@!", "<@").replace(manager.getApi().getYourself().getMentionTag() + " ", prefix);
            ArrayList<String> args = new ArrayList<>(Arrays.asList(content.substring(prefix.length()).split(" ")));
            String command = args.get(0);
            args.remove(0);

            // Important Pre-Made Commands
            if (command.equalsIgnoreCase("link") && args.size() > 0 && args.get(0).equalsIgnoreCase("confirm")) {
                try {
                    // Search for value in database
                    Database database = discordlink.getDatabaseManager().getDatabase();
                    ArrayList<DbPlayer> results = database.select(DbPlayer.getTableSchema(database), "discord_id = '" + message.getAuthor().getIdAsString() + "'", DbPlayer.class);
                    if (results.size() == 0) {
                        message.getChannel().sendMessage(discordlink.getTranslation("linking.discord.no_request"));
                        return;
                    }
                    DbPlayer player = results.get(0);

                    // Update value in database
                    database.update(DbPlayer.getTableSchema(database), "discord_id = '" + message.getAuthor().getIdAsString() + "'", new DatabaseValue("linked", 1));

                    // Search for roles
                    for (String guildAndRole : discordlink.getConfig().getStringList("bot.linking.roles")) {
                        String[] parts = guildAndRole.split(":");
                        String guildId = parts[0];
                        String roleId = parts[1];

                        // Search for guild
                        discordlink.getDiscordManager().getApi().getServerById(guildId).ifPresent(server -> {
                            // Search for role
                            server.getRoleById(roleId).ifPresent(role -> {
                                // Add role to user
                                server.getMemberById(message.getAuthor().getId()).ifPresent(user -> {
                                    try {
                                        user.addRole(role);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            });
                        });
                    }

                    // Send confirmation message
                    message.getChannel().sendMessage(discordlink.getTranslation("linking.discord.success"));
                } catch (Exception e) {
                    message.getChannel().sendMessage("There was an internal error while running this command.");
                }
                return;
            }

            // Handle Custom Commands
            // TODO: Check to make sure it's in the right channel
            ConfigurationSection commands = discordlink.getConfig().getConfigurationSection("bot.discord.commands");
            if (commands != null) {

                // Fetch Commands
                Map<String, Object> values = commands.getValues(false);
                if (values.size() > 0) {

                    // Find Command
                    for (Map.Entry<String, Object> entry : values.entrySet()) {
                        if (!(entry.getValue() instanceof String)) {
                            continue;
                        }

                        // Handle Command
                        if (command.equalsIgnoreCase(entry.getKey())) {
                            String value = (String) entry.getValue();

                            // Placeholder API
                            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                                value = PlaceholderAPI.setPlaceholders(null, value);
                                value = TrueDiscordLink.stripColorCodes(value);
                            }

                            // Send Message
                            message.getChannel().sendMessage(value);

                            // Return to prevent sending the message in the server
                            return;
                        }
                    }

                }

            }

            // Less-Important Pre-Made Commands
            if (command.equalsIgnoreCase("ping")) {
                message.getChannel().sendMessage("Pong!");

                // Return to prevent sending the message in the server
                return;
            }
        }

        // If we made it to this point, ignore non-server DMs
        if (!message.isServerMessage()) {
            return;
        }

        // If not a command, send the message to the server
        manager.sendMinecraftMessage(message);
    }

}
