package com.visualfiredev.truediscordlink.events;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.configuration.InvalidConfigurationException;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.List;

public class DiscordChatHandler implements MessageCreateListener {

    // Instance Variables
    private final TrueDiscordLink discordlink;
    private List<Long> channelIds;

    // Constructor
    public DiscordChatHandler() {
        this.discordlink = TrueDiscordLink.getInstance();
    }

    // Event Handler
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        // Exclude Messages by Self, Other Bots, or with No Author
        if (!event.isServerMessage() || event.getMessageAuthor() == null || event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()) {
            return;
        }

        // Initialize Channel Id List
        if (channelIds == null && !initialize()) {
            return;
        }

        // Fetch Channel & Send Message
        int index = channelIds.indexOf(event.getChannel().getId());
        if (index != -1) {
            discordlink.getServer().broadcastMessage("<[D] " + event.getMessageAuthor().getDisplayName() + "> " + event.getMessageContent());
        }
    }

    // Initializer
    private boolean initialize() {
        channelIds = discordlink.getConfig().getLongList("messaging.receive_channels");
        return true;
    }

    // Resets all of the nessecary information for a configuration reload
    public void reset() {
        channelIds = null;
    }

}
