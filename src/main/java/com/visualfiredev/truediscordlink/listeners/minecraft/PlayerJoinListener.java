package com.visualfiredev.truediscordlink.listeners.minecraft;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    // Variables
    private TrueDiscordLink discordlink;

    // Constructor
    public PlayerJoinListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check Enabled
        if (!discordlink.getConfig().getBoolean("events.player_join")) {
            return;
        }

        // Send Discord Message
        discordlink.getDiscordManager().sendDiscordMessage(
            discordlink.getTranslation("events.player_join", false,
                new String[] { "%name%", event.getPlayer().getName() },
                new String[] { "%displayName%", event.getPlayer().getDisplayName() },
                new String[] { "%uuid%", event.getPlayer().getUniqueId().toString() }
            )
        );
    }

}