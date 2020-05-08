package com.visualfiredev.truediscordlink;

import com.visualfiredev.truediscordlink.commands.CommandTrueDiscordLink;
import com.visualfiredev.truediscordlink.discord.PlayerChatHandler;
import com.visualfiredev.truediscordlink.tabcompleters.TabCompleterTrueDiscordLink;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class TrueDiscordLink extends JavaPlugin {

    // Static Variables
    private static TrueDiscordLink instance;

    // Instance Variables
    private PlayerChatHandler playerChatHandler;

    // Plugin Enabled Handler
    @Override
    public void onEnable() {
        instance = this;

        // TODO: Add sending via Discord bot
        // TODO: Add receiving via Discord bot
        // TODO: Add lang file & prefix / message configuration
        // TODO: Full config naming scheme & categorization overhaul

        // Load Default Configuration
        this.saveDefaultConfig();

        // Register Commands
        Objects.requireNonNull(this.getCommand("truediscordlink")).setExecutor(new CommandTrueDiscordLink());

        // Register Tab Completers
        Objects.requireNonNull(this.getCommand("truediscordlink")).setTabCompleter(new TabCompleterTrueDiscordLink());

        // Event Listeners
        playerChatHandler = new PlayerChatHandler();
        this.getServer().getPluginManager().registerEvents(playerChatHandler, this);

        // Log Enabled
        this.getLogger().info("TrueDiscordLink enabled!");
    }

    // Plugin Disabled Handler
    @Override
    public void onDisable() {
        // Log Disabled
        this.getLogger().info("TrueDiscordLink disabled!");
    }

    // Getters
    public static TrueDiscordLink getInstance() {
        return instance;
    }
    public PlayerChatHandler getPlayerChatHandler() {
        return playerChatHandler;
    }

}
