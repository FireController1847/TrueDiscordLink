package com.visualfiredev.truediscordlink.listeners.minecraft;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import com.visualfiredev.truediscordlink.VersionHelper;
import com.visualfiredev.truediscordlink.nms.advancement.IAdvancementHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlayerAdvancementDoneListener implements Listener {

    // Variables
    private TrueDiscordLink discordlink;

    // Constructor
    public PlayerAdvancementDoneListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        // Check Enabled
        if (!discordlink.getConfig().getBoolean("events.player_advance")) {
            return;
        }

        // If the advancement is a recipe, return
        if (event.getAdvancement().getKey().getKey().contains("recipes/")) {
            return;
        }

        // Fetch Name & Description
        IAdvancementHelper helper = discordlink.getVersionHelper().getAdvancementHelper();
        String name;
        String description;
        if (helper != null) {
            name = helper.getName(event.getAdvancement());
            description = helper.getDescription(event.getAdvancement());
        } else {
            String rawName = event.getAdvancement().getKey().getKey();
            name = Arrays.stream(rawName.substring(rawName.lastIndexOf('/') + 1).toLowerCase().split("_")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1)).collect(Collectors.joining());
            description = "Unable to fetch description.";
        }

        // Send Discord Message
        discordlink.getDiscordManager().sendDiscordMessage(
            discordlink.getTranslation("events.player_advance", false,
                new String[] { "%name%", event.getPlayer().getName() },
                new String[] { "%displayName%", event.getPlayer().getDisplayName() },
                new String[] { "%uuid%", event.getPlayer().getUniqueId().toString() },
                new String[] { "%advancement_name%", name },
                new String[] { "%advancement_description%", description }
            )
        );
    }

}
