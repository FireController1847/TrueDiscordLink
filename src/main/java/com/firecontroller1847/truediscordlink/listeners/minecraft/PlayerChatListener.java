package com.firecontroller1847.truediscordlink.listeners.minecraft;

import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;

public class PlayerChatListener implements Listener {

    // Variables
    private TrueDiscordLink discordlink;

    // Constructor
    public PlayerChatListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Send Discord Message
        ArrayList<String[]> modifications = discordlink.getDiscordManager().sendDiscordMessage(event.getMessage(), false, event.getPlayer());

        // Apply Modifications
        if (modifications != null) {
            for (String[] modification : modifications) {
                event.setMessage(TrueDiscordLink.translateColorCodes('&', event.getMessage().replace(modification[0], modification[1])));
            }
        }
    }

}
