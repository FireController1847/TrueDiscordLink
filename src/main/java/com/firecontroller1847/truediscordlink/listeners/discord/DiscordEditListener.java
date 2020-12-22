package com.firecontroller1847.truediscordlink.listeners.discord;

import com.firecontroller1847.truediscordlink.DiscordManager;
import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageEditListener;

public class DiscordEditListener implements MessageEditListener {

    // Variables
    private TrueDiscordLink discordlink;
    private DiscordManager manager;

    // Constructor
    public DiscordEditListener(TrueDiscordLink discordlink, DiscordManager manager) {
        this.discordlink = discordlink;
        this.manager = manager;
    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        // Cancel if disabled
        if (!discordlink.getConfig().getBoolean("bot.show_edits")) {
            return;
        }

        // If the message doesn't exist, return
        Message message = event.getMessage().orElse(null);
        if (message == null) {
            return;
        }

        // If the message is older than 2 minutes, ignore
        try {
            if (message.getLastEditTimestamp().orElseThrow(() -> new Exception("Last edit timestamp cannot be null for message edit event!")).toEpochMilli() - message.getCreationTimestamp().toEpochMilli() > 120000) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Exclude Non-Server Messages, System Messages, & Webhook Messages
        if (!message.isServerMessage() || message.getAuthor() == null || message.getAuthor().isYourself() || message.getAuthor().isWebhook()) {
            return;
        }

        // Send the message to the server
        manager.sendMinecraftMessage(message, true);
    }
}
