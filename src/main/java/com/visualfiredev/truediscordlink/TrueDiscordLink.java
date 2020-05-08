package com.visualfiredev.truediscordlink;

import com.visualfiredev.truediscordlink.commands.CommandTrueDiscordLink;
import com.visualfiredev.truediscordlink.listeners.DiscordChatListener;
import com.visualfiredev.truediscordlink.listeners.PlayerChatListener;
import com.visualfiredev.truediscordlink.tabcompleters.TabCompleterTrueDiscordLink;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TrueDiscordLink extends JavaPlugin {

    // Static Variables
    private static TrueDiscordLink instance;

    // Instance Variables
    private DiscordApi discord;
    private PlayerChatListener playerChatListener;
    private DiscordChatListener discordChatListener;

    // Plugin Enable Listener
    @Override
    public void onEnable() {
        // Set Instance
        instance = this;

        // Configuration Setup
        this.saveDefaultConfig();

        // Register Executors & Tab Completers
        PluginCommand cmdTrueDiscordLink = Objects.requireNonNull(this.getCommand("truediscordlink"));
        cmdTrueDiscordLink.setExecutor(new CommandTrueDiscordLink());
        cmdTrueDiscordLink.setTabCompleter(new TabCompleterTrueDiscordLink());

        // Register Event Listeners
        playerChatListener = new PlayerChatListener(this);
        this.getServer().getPluginManager().registerEvents(playerChatListener, this);

        // Login to Discord
        if (this.getConfig().getBoolean("bot.enabled")) {
            // Fetch Bot Token
            String token = this.getConfig().getString("bot.token");
            if (token == null) {
                (new InvalidConfigurationException("Invalid bot token!")).printStackTrace();
            }

            // Login
            this.getLogger().info("Logging in to Discord...");
            new DiscordApiBuilder().setToken(token).login().thenAcceptAsync(api -> {
                // Register Event Listeners
                discordChatListener = new DiscordChatListener(this);
                api.addListener(discordChatListener);

                // Assign to Variable & Output Login Status
                discord = api;
                this.getLogger().info("Logged in!");
            }).exceptionally(ExceptionLogger.get());
        }

        // Output Plugin Enabled
        this.getLogger().info("TrueDiscordLink enabled!");
    }

    // Plugin Disable Listener
    @Override
    public void onDisable() {
        // Remove Discord Event Listeners & Log Out
        discord.removeListener(discordChatListener);
        discord.disconnect();
        try {
            discord.getThreadPool().getExecutorService().awaitTermination(5, TimeUnit.SECONDS);
            discord = null;
        } catch (InterruptedException e) {
            // It's not the end of the world, I suppose
        }

        // Output Plugin Disabled
        this.getLogger().info("TrueDiscordLink disabled!");
    }

    // Getters
    public static TrueDiscordLink getInstance() {
        return instance;
    }
    public PlayerChatListener getPlayerChatListener() {
        return playerChatListener;
    }
    public DiscordChatListener getDiscordChatListener() {
        return discordChatListener;
    }
    public DiscordApi getDiscord() {
        return discord;
    }

}
