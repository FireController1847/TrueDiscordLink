package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.List;

public class DiscordChatListener implements MessageCreateListener {

    // Instance Variables
    private final TrueDiscordLink discordLink;
    private List<Long> channelIds;
    private boolean initialized;

    // Constructor
    public DiscordChatListener(TrueDiscordLink discordLink) {
        this.discordLink = discordLink;
    }

    // Event Handler
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        // Exclude Non-Server Messages, System Messages, Bot Messages, & Webhook Messages
        if (!event.isServerMessage() || event.getMessageAuthor() == null || event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()) {
            return;
        }

        // Load Configuration
        if (!initialized && !initialize()) {
            return;
        }

        // Bot
        if (channelIds != null && channelIds.size() > 0) {

            // Check for Channel & Send Message
            int index = channelIds.indexOf(event.getChannel().getId());
            if (index != -1) {
                discordLink.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    discordLink.getLangString("messages.receive_format",
                        new String[] { "%username", event.getMessageAuthor().getName() },
                        new String[] { "%nickname", event.getMessageAuthor().getDisplayName() },
                        new String[] { "%discriminator", event.getMessageAuthor().getDiscriminator().toString() },
                        new String[] { "%id", event.getMessageAuthor().getIdAsString() },
                        new String[] { "%message", event.getMessageContent() }
                    )
                ));
            }

        }
    }

    // Configuration Cache Initialization
    private boolean initialize() {
        FileConfiguration config = discordLink.getConfig();

        // Bot Configuration
        if (config.getBoolean("bot.enabled")) {
            channelIds = config.getLongList("bot.receive_channels");
        }

        initialized = true;
        return true;
    }

    // Configuration Cache Reset
    public void reset() {
        channelIds = null;
        initialized = false;
    }

}
