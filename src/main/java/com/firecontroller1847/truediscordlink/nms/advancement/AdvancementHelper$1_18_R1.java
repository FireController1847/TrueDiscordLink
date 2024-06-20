package com.firecontroller1847.truediscordlink.nms.advancement;

import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_18_R1.advancement.CraftAdvancement;

public class AdvancementHelper$1_18_R1 implements IAdvancementHelper {

    @Override
    public String getName(Advancement bukkitAdvancement) {
        try {
            // GetDisplay was not implemented until 1.19. Use alternative methods.
            CraftAdvancement advancement = (CraftAdvancement) bukkitAdvancement;
            if (advancement.getHandle().display().isPresent()) {
                return advancement.getHandle().display().get().getTitle().getString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getDescription(Advancement bukkitAdvancement) {
        try {
            // GetDisplay was not implemented until 1.19. Use alternative methods.
            CraftAdvancement advancement = (CraftAdvancement) bukkitAdvancement;
            if (advancement.getHandle().display().isPresent()) {
                return advancement.getHandle().display().get().getDescription().getString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
