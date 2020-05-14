package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    // Static Variables
    private static TrueDiscordLink discordlink;

    // Constructor
    public PlayerJoinListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!discordlink.getConfig().getBoolean("events.player_join")) {
            return;
        }

        discordlink.getDiscordManager().sendDiscordMessage(
            discordlink.getLangString("events.player_join", false,
                new String[] { "%name", event.getPlayer().getName() },
                new String[] { "%displayname", event.getPlayer().getDisplayName() },
                new String[] { "%uuid", event.getPlayer().getUniqueId().toString() }
            ),
            null
        );
    }

}
