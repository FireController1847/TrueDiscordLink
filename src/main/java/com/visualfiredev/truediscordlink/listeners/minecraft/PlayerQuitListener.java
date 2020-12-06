package com.visualfiredev.truediscordlink.listeners.minecraft;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    // Variables
    private TrueDiscordLink discordlink;

    // Constructor
    public PlayerQuitListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Check Enabled
        if (!discordlink.getConfig().getBoolean("events.player_quit")) {
            return;
        }

        // Send Discord Message
        discordlink.getDiscordManager().sendDiscordMessage(
            discordlink.getTranslation("events.player_quit", false,
                new String[] { "%name%", event.getPlayer().getName() },
                new String[] { "%displayName%", event.getPlayer().getDisplayName() },
                new String[] { "%uuid%", event.getPlayer().getUniqueId().toString() }
            )
        );
    }

}
