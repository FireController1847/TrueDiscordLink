package com.firecontroller1847.truediscordlink;

import com.firecontroller1847.truediscordlink.nms.advancement.*;
import org.bukkit.Bukkit;

public class VersionHelper {

    // Variables
    private TrueDiscordLink discordlink;
    private IAdvancementHelper advancementHelper;

    // Constructor
    protected VersionHelper(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;

        // Create & Load In Classes
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        discordlink.getLogger().info("Server running version " + version);
        if (version.equals("v1_15_R1")) {
            advancementHelper = new AdvancementHelper_1_15_R1();
        } else if (version.equals("v1_16_R1")) {
            advancementHelper = new AdvancementHelper_1_16_R1();
        } else if (version.equals("v1_16_R2")) {
            advancementHelper = new AdvancementHelper_1_16_R2();
        } else if (version.equals("v1_16_R3")) {
            advancementHelper = new AdvancementHelper_1_16_R3();
        } else {
            advancementHelper = null;
        }
    }

    // Getters
    public IAdvancementHelper getAdvancementHelper() {
        return advancementHelper;
    }

}
