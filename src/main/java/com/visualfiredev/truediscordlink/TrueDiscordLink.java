package com.visualfiredev.truediscordlink;

import com.visualfiredev.truediscordlink.commands.CommandTrueDiscordLink;
import com.visualfiredev.truediscordlink.events.DiscordChatHandler;
import com.visualfiredev.truediscordlink.events.PlayerChatHandler;
import com.visualfiredev.truediscordlink.tabcompleters.TabCompleterTrueDiscordLink;
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
    private PlayerChatHandler playerChatHandler; // Player Chat Handler
    private DiscordChatHandler discordChatHandler; // Discord Chat Handler

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

        // Bot Login
        loginToDiscord();

        // Log Enabled
        this.getLogger().info("TrueDiscordLink enabled!");
    }

    // Plugin Disabled Handler
    @Override
    public void onDisable() {
        // Log out of Discord
        discord.removeListener(discordChatHandler);
        discord.disconnect();
        try {
            discord.getThreadPool().getExecutorService().awaitTermination(5, TimeUnit.SECONDS);
            discord = null;
        } catch (InterruptedException e) {
            // It's not the end of the world, I suppose
        }

        // Log Disabled
        this.getLogger().info("TrueDiscordLink disabled!");
    }


    // Logs In to Discord
    private void loginToDiscord() {
        // Check if enabled
        if (this.getConfig().getBoolean("messaging.use_bot")) {

            // Fetch token
            String token = this.getConfig().getString("messaging.bot_token");
            if (token == null) {
                (new InvalidConfigurationException("Invalid bot token but use_bot is enabled!")).printStackTrace();
                return;
            }

            // Login
            this.getLogger().info("Discord bot logging in!");
            new DiscordApiBuilder().setToken(token).login().thenAcceptAsync(api -> {

                // Event Listeners
                discordChatHandler = new DiscordChatHandler();
                api.addListener(discordChatHandler);

                // Assign to Variable
                discord = api;

                this.getLogger().info("Discord bot ready!");
            }).exceptionally(ExceptionLogger.get());
        }
    }

    // Getters
    public static TrueDiscordLink getInstance() {
        return instance;
    }
    public PlayerChatHandler getPlayerChatHandler() {
        return playerChatHandler;
    }
    public DiscordChatHandler getDiscordChatHandler() {
        return discordChatHandler;
    }
    public DiscordApi getDiscord() {
        return discord;
    }

}
