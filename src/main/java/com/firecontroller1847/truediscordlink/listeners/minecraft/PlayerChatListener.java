package com.firecontroller1847.truediscordlink.listeners.minecraft;

import com.earth2me.essentials.User;
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
        if (discordlink.getEssentialsApi() != null) {
            User user = discordlink.getEssentialsApi().getUser(event.getPlayer().getUniqueId());
            if (user != null && user.isMuted()) {
                return;
            }
        }

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
