package com.visualfiredev.truediscordlink.nms.advancement;

import org.bukkit.advancement.Advancement;

public interface IAdvancementHelper {

    String getName(Advancement bukkitAdvancement);
    String getDescription(Advancement bukkitAdvancement);

}
