package com.firecontroller1847.truediscordlink.nms.advancement;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;

public class AdvancementHelperReflection implements IAdvancementHelper {

    // Use reflection to get the name of an advancement
    @Override
    public String getName(Advancement bukkitAdvancement) {
        try {
            Object advancement = getNmsAdvancement(bukkitAdvancement);
            if (advancement != null) {
                Object advancementDisplay = advancement.getClass().getMethod("c").invoke(advancement);
                if (advancementDisplay != null) {
                    Object chatBaseComponentName = advancementDisplay.getClass().getMethod("a").invoke(advancementDisplay);
                    if (chatBaseComponentName != null) {
                        return (String) chatBaseComponentName.getClass().getMethod("getString").invoke(chatBaseComponentName);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Use reflection to get the description of an advancement
    @Override
    public String getDescription(Advancement bukkitAdvancement) {
        try {
            Object advancement = getNmsAdvancement(bukkitAdvancement);
            if (advancement != null) {
                Object advancementDisplay = advancement.getClass().getMethod("c").invoke(advancement);
                if (advancementDisplay != null) {
                    Object chatBaseComponentName = advancementDisplay.getClass().getMethod("b").invoke(advancementDisplay);
                    if (chatBaseComponentName != null) {
                        return (String) chatBaseComponentName.getClass().getMethod("getString").invoke(chatBaseComponentName);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Uses reflection to get the advancement from Minecraft
    private Object getNmsAdvancement(Advancement bukkitAdvancement) throws Exception {
        // Fetch The Server
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        Class<?> MinecraftServer = ClassLoader.getSystemClassLoader().loadClass("net.minecraft.server." + version + ".MinecraftServer");
        Object server = MinecraftServer.getMethod("getServer").invoke(null);
        Object advancementDataWorld = server.getClass().getMethod("getAdvancementData").invoke(server);

        // Fetch The Key
        Class<?> MinecraftKey = ClassLoader.getSystemClassLoader().loadClass("net.minecraft.server." + version + ".MinecraftKey");
        Object key = MinecraftKey.getMethod("a", String.class).invoke(null, bukkitAdvancement.getKey().getKey());

        // Fetch The Advancement
        return advancementDataWorld.getClass().getMethod("a", MinecraftKey).invoke(advancementDataWorld, key);
    }

}
