package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import com.visualfiredev.truediscordlink.VersionHelper;
import com.visualfiredev.truediscordlink.nms.advancement.IAdvancementHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlayerAdvancementDoneListener implements Listener {

    // Instance Variables
    private final TrueDiscordLink discordlink;

    // Constructor
    public PlayerAdvancementDoneListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    // Event Handler
    @EventHandler
    public void onPlayerAvancementDone(PlayerAdvancementDoneEvent event) {
        if (!discordlink.getConfig().getBoolean("events.player_advance")) {
            return;
        }

        // Check if advancement is a recipe, if so then return
        if (event.getAdvancement().getKey().getKey().contains("recipes/")) {
            return;
        }

        // Fetch Advancement Name & Description
        IAdvancementHelper advancementHelper = VersionHelper.getInstance().getAdvancementHelper();
        String name;
        String description;
        if (advancementHelper != null) {
            name = advancementHelper.getName(event.getAdvancement());
            description = advancementHelper.getDescription(event.getAdvancement());
        } else {
            String rawName = event.getAdvancement().getKey().getKey();
            name = Arrays.stream(rawName.substring(rawName.lastIndexOf('/') + 1).toLowerCase().split("_")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1)).collect(Collectors.joining());
            description = "";
        }

        discordlink.getDiscordManager().sendDiscordMessage(
            discordlink.getLangString("events.player_advance", false,
                new String[] { "%name", event.getPlayer().getName() },
                new String[] { "%displayname", event.getPlayer().getDisplayName() },
                new String[] { "%uuid", event.getPlayer().getUniqueId().toString() },
                new String[] { "%advancement_name", name },
                new String[] { "%advancement_description", description }
            ),
            null
        );
    }

}
