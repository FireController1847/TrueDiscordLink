package com.firecontroller1847.truediscordlink.nms.advancement;

import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_19_R1.advancement.CraftAdvancement;

import java.util.Objects;

public class AdvancementHelper$1_19_R1 implements IAdvancementHelper {

    @Override
    public String getName(Advancement bukkitAdvancement) {
        try {
            CraftAdvancement advancement = (CraftAdvancement) bukkitAdvancement;
            return Objects.requireNonNull(advancement.getDisplay()).getTitle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getDescription(Advancement bukkitAdvancement) {
        try {
            CraftAdvancement advancement = (CraftAdvancement) bukkitAdvancement;
            return Objects.requireNonNull(advancement.getDisplay()).getDescription();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
