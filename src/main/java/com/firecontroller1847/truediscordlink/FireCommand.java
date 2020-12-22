package com.firecontroller1847.truediscordlink;

import org.bukkit.command.CommandExecutor;

/**
 * An abstract command that has an instance of the plugin readily available.
 */
public abstract class FireCommand implements CommandExecutor {

    // Variables
    protected FirePlugin plugin;

    // Constructor
    public FireCommand(FirePlugin plugin) {
        this.plugin = plugin;
    }

    // Getters
    public FirePlugin getPlugin() {
        return plugin;
    }

    // Setters
    public void setPlugin(FirePlugin plugin) {
        this.plugin = plugin;
    }

}