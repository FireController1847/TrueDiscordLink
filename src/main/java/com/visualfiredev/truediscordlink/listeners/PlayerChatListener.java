package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;

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
        ArrayList<String[]> modifications = discordlink.getDiscordManager().sendDiscordMessage(event.getMessage(), false, event.getPlayer());
        if (modifications != null) {
            for (String[] modification : modifications) {
                event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage().replace(modification[0], modification[1])));
            }
        }
    }

}
