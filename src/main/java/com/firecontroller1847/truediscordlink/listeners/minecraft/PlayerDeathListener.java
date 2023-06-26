package com.firecontroller1847.truediscordlink.listeners.minecraft;

import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    // Variables
    private TrueDiscordLink discordlink;

    // Constructor
    public PlayerDeathListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Check Enabled
        if (!discordlink.getConfig().getBoolean("events.player_death")) {
            return;
        }

        // Send Discord Message
        discordlink.getDiscordManager().sendDiscordMessage(
            discordlink.getTranslation("events.player_death", false,
                new String[] { "%message%", event.getDeathMessage() },
                new String[] { "%name%", TrueDiscordLink.escapeDiscordFormatting(TrueDiscordLink.stripColorCodes(event.getEntity().getName())) },
                new String[] { "%display_name%", TrueDiscordLink.escapeDiscordFormatting(TrueDiscordLink.stripColorCodes(event.getEntity().getDisplayName())) },
                new String[] { "%uuid%", event.getEntity().getUniqueId().toString() }
            )
        );
    }

}
