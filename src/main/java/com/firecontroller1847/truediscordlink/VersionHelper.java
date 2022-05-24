package com.firecontroller1847.truediscordlink;

import com.firecontroller1847.truediscordlink.nms.advancement.*;
import org.bukkit.Bukkit;

// TODO: This class is unnecessary, it needs to be removed.
// I used to use this class to help generify the AdvancementHelper logic. But back then I'd have
// to build every version and test it, then keep the version. This removes support for TravisCI
// and makes the project dependent on having every raw NMS version available. I changed it to
// use reflection, but now the abstraction of classes between versions is no longer necessary.
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
        advancementHelper = new AdvancementHelper$1_18_R2();
//        if (version.equals("v1_15_R1")) {
//            advancementHelper = new AdvancementHelper_1_15_R1();
//        } else if (version.equals("v1_16_R1")) {
//            advancementHelper = new AdvancementHelper_1_16_R1();
//        } else if (version.equals("v1_16_R2")) {
//            advancementHelper = new AdvancementHelper_1_16_R2();
//        } else if (version.equals("v1_16_R3")) {
//            advancementHelper = new AdvancementHelper_1_16_R3();
//        } else {
//            advancementHelper = null;
//        }
    }

    // Getters
    public IAdvancementHelper getAdvancementHelper() {
        return advancementHelper;
    }

}
