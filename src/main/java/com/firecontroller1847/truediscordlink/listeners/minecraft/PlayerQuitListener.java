package com.firecontroller1847.truediscordlink.listeners.minecraft;

import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
                new String[] { "%name%", TrueDiscordLink.escapeDiscordFormatting(event.getPlayer().getName()) },
                new String[] { "%display_name%", TrueDiscordLink.escapeDiscordFormatting(event.getPlayer().getDisplayName()) },
                new String[] { "%uuid%", event.getPlayer().getUniqueId().toString() }
            )
        );
    }

}
