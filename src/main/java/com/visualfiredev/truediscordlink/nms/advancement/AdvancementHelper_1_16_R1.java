package com.visualfiredev.truediscordlink.nms.advancement;

import net.minecraft.server.v1_16_R1.*;

public class AdvancementHelper_1_16_R1 implements IAdvancementHelper {

    @Override
    public String getName(org.bukkit.advancement.Advancement bukkitAdvancement) {
        Advancement advancement = MinecraftServer.getServer().getAdvancementData().a(MinecraftKey.a(bukkitAdvancement.getKey().getKey()));
        if (advancement != null) {
            AdvancementDisplay display = advancement.c();
            if (display != null) {
                IChatBaseComponent name = display.a();
                if (name != null) {
                    return name.getString();
                }
            }
        }
        return null;
    }

    @Override
    public String getDescription(org.bukkit.advancement.Advancement bukkitAdvancement) {
        Advancement advancement = MinecraftServer.getServer().getAdvancementData().a(MinecraftKey.a(bukkitAdvancement.getKey().getKey()));
        if (advancement != null) {
            AdvancementDisplay display = advancement.c();
            if (display != null) {
                IChatBaseComponent description = display.b();
                if (description != null) {
                    return description.getString();
                }
            }
        }
        return null;
    }

}
