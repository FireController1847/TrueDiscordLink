package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class DiscordChatListener implements MessageCreateListener {

    // Instance Variables
    private final TrueDiscordLink discordlink;

    // Constructor
    public DiscordChatListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    // Event Handler
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        discordlink.getDiscordManager().sendMinecraftMessage(event.getMessage());
    }

}
