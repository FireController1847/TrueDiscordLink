package com.firecontroller1847.truediscordlink.nms.advancement;

import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_18_R2.advancement.CraftAdvancement;

import java.util.Objects;

public class AdvancementHelper$1_18_R2 implements IAdvancementHelper {

    // Use reflection to get the name of an advancement
    @Override
    public String getName(Advancement bukkitAdvancement) {
        try {
            CraftAdvancement advancement = (CraftAdvancement) bukkitAdvancement;
            return Objects.requireNonNull(advancement.getHandle().getDisplay()).getTitle().getString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Use reflection to get the description of an advancement
    @Override
    public String getDescription(Advancement bukkitAdvancement) {
        try {
            CraftAdvancement advancement = (CraftAdvancement) bukkitAdvancement;
            return Objects.requireNonNull(advancement.getHandle().getDisplay()).getDescription().getString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}