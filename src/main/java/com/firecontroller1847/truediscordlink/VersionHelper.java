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
        String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String version;
        if (split.length != 4) {
            // 1.20.4+ detection, manual impl
            version = Bukkit.getServer().getBukkitVersion().split("-")[0];
            switch (version) {
                case "1.20.4":
                    version = "v1_20_R3";
                    break;
                case "1.20.5":
                case "1.20.6":
                    version = "v1_20_R4";
                    break;
                case "1.21":
                    version = "v1_21_R1";
                    break;
                default:
                    version = "";
            }
        } else {
            version = split[3];
        }
        if (version == null) {
            discordlink.getLogger().severe("Unable to detect Bukkit version. Some features may not work correctly!");
        }
        discordlink.getLogger().info("Server running version " + version);
        if (version.equals("v1_12_R1")) {
            advancementHelper = new AdvancementHelper$1_12_R1();
        } else if (version.equals("v1_13_R1")) {
            advancementHelper = new AdvancementHelper$1_13_R1();
        } else if (version.equals("v1_13_R2")) {
            advancementHelper = new AdvancementHelper$1_13_R2();
        } else if (version.equals("v1_14_R1")) {
            advancementHelper = new AdvancementHelper$1_14_R1();
        } else if (version.equals("v1_15_R1")) {
            advancementHelper = new AdvancementHelper$1_15_R1();
        } else if (version.equals("v1_16_R1")) {
            advancementHelper = new AdvancementHelper$1_16_R1();
        } else if (version.equals("v1_16_R2")) {
            advancementHelper = new AdvancementHelper$1_16_R2();
        } else if (version.equals("v1_16_R3")) {
            advancementHelper = new AdvancementHelper$1_16_R3();
        } else if (version.equals("v1_17_R1")) {
            advancementHelper = new AdvancementHelper$1_17_R1();
        } else if (version.equals("v1_18_R1")) {
            advancementHelper = new AdvancementHelper$1_18_R1();
        } else if (version.equals("v1_18_R2")) {
            advancementHelper = new AdvancementHelper$1_18_R2();
        } else if (version.equals("v1_19_R1")) {
            advancementHelper = new AdvancementHelper$1_19_R1();
        } else if (version.equals("v1_20_R1")) {
            advancementHelper = new AdvancementHelper$1_20_R1();
        } else if (version.equals("v1_20_R2")) {
            advancementHelper = new AdvancementHelper$1_20_R2();
        } else if (version.equals("v1_20_R3")) {
            advancementHelper = new AdvancementHelper$1_20_R3();
        } else if (version.equals("v1_20_R4")) {
            advancementHelper = new AdvancementHelper$1_20_R4();
        } else if (version.equals("v1_21_R1")) {
            advancementHelper = new AdvancementHelper$1_21_R1();
        } else {
            advancementHelper = new AdvancementHelper$UNSUPPORTED();
        }
    }

    // Getters
    public IAdvancementHelper getAdvancementHelper() {
        return advancementHelper;
    }

}
