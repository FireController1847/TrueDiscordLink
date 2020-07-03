package com.visualfiredev.truediscordlink;

import com.visualfiredev.truediscordlink.nms.advancement.AdvancementHelper_1_15_R1;
import com.visualfiredev.truediscordlink.nms.advancement.AdvancementHelper_1_16_R1;
import com.visualfiredev.truediscordlink.nms.advancement.IAdvancementHelper;
import org.bukkit.Bukkit;

public final class VersionHelper {

    // Static Variables
    private static VersionHelper instance;

    // Instance Variables
    private TrueDiscordLink discordlink;
    private IAdvancementHelper advancementHelper;

    // Constructor
    protected VersionHelper(TrueDiscordLink discordlink) {
        instance = this;
        this.discordlink = discordlink;

        // Create & Load In Classes
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        discordlink.getLogger().info("Server running version " + version);
        if (version.equals("v1_15_R1")) {
            advancementHelper = new AdvancementHelper_1_15_R1();
        } else if (version.equals("v1_16_R1")) {
            advancementHelper = new AdvancementHelper_1_16_R1();
        } else {
            advancementHelper = null;
        }

    }

    // Getters
    public static VersionHelper getInstance() {
        return instance;
    }
    public IAdvancementHelper getAdvancementHelper() {
        return advancementHelper;
    }

}
