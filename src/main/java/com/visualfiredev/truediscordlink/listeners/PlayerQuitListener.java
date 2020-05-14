package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    // Static Variables
    private static TrueDiscordLink discordlink;

    // Constructor
    public PlayerQuitListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!discordlink.getConfig().getBoolean("events.player_quit")) {
            return;
        }

        discordlink.getDiscordManager().sendDiscordMessage(
            discordlink.getLangString("events.player_quit", false,
                new String[] { "%name", event.getPlayer().getName() },
                new String[] { "%displayname", event.getPlayer().getDisplayName() },
                new String[] { "%uuid", event.getPlayer().getUniqueId().toString() }
            ),
            null
        );
    }

}
