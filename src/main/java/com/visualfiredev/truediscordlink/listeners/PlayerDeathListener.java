package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    // Instance Variables
    private final TrueDiscordLink discordlink;

    // Constructor
    public PlayerDeathListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    // Event Handler
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!discordlink.getConfig().getBoolean("events.player_death")) {
            return;
        }

        discordlink.getDiscordManager().sendDiscordMessage(
            discordlink.getLangString("events.player_death",
                new String[] { "%name", event.getEntity().getName() },
                new String[] { "%displayname", event.getEntity().getDisplayName() },
                new String[] { "%uuid", event.getEntity().getUniqueId().toString() },
                new String[] { "%message", event.getDeathMessage() }
            ),
            null
        );
    }

}
