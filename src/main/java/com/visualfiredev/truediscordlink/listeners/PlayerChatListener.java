package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    // Instance Variables
    private final TrueDiscordLink discordlink;

    // Constructor
    public PlayerChatListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    // Event Handler
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        discordlink.getDiscordManager().sendDiscordMessage(event.getMessage(), event.getPlayer());
    }

}
