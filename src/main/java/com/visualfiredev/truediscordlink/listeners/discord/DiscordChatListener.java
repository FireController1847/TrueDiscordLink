package com.visualfiredev.truediscordlink.listeners.discord;

import com.visualfiredev.truediscordlink.DiscordManager;
import com.visualfiredev.truediscordlink.TrueDiscordLink;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

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

        // Exclude Non-Server Messages, System Messages, & Webhook Messages
        if (!message.isServerMessage() || message.getAuthor() == null || message.getAuthor().isYourself() || message.getAuthor().isWebhook()) {
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
            String command = content.substring(prefix.length());

            // Handle Custom Commands
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

            // Pre-Made Commands
            if (command.equalsIgnoreCase("ping")) {
                message.getChannel().sendMessage("Pong!");

                // Return to prevent sending the message in the server
                return;
            }
        }

        // If not a command, send the message to the server
        manager.sendMinecraftMessage(message);
    }

}
