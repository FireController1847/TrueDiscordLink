package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageEditListener;

public class DiscordEditListener implements MessageEditListener {

    // Instance Variables
    private final TrueDiscordLink discordlink;

    // Constructor
    public DiscordEditListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    // Event Handler
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

        // Send Minecraft message
        discordlink.getDiscordManager().sendMinecraftMessage(message, true);
    }

}
