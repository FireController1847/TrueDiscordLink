package com.visualfiredev.truediscordlink;

import com.visualfiredev.truediscordlink.commands.CommandTrueDiscordLink;
import com.visualfiredev.truediscordlink.listeners.*;
import com.visualfiredev.truediscordlink.tabcompleters.TabCompleterTrueDiscordLink;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TrueDiscordLink extends JavaPlugin {

    // Static Variables
    private static TrueDiscordLink instance;

    // Instance Variables
    private FileConfiguration lang;
    private DiscordApi discord;
    private DiscordManager manager;
    private PlayerChatListener playerChatListener;
    private DiscordChatListener discordChatListener;

    // Plugin Enable Listener
    @Override
    public void onEnable() {
        // Set Instance
        instance = this;

        // Initialize Version Helper
        new VersionHelper(this);

        // Configuration Setup (LANG)
        this.saveDefaultConfig();
        this.loadLangConfig();

        // Register Executors & Tab Completers
        PluginCommand cmdTrueDiscordLink = Objects.requireNonNull(this.getCommand("truediscordlink"));
        cmdTrueDiscordLink.setExecutor(new CommandTrueDiscordLink());
        cmdTrueDiscordLink.setTabCompleter(new TabCompleterTrueDiscordLink());

        // Register Event Listeners
        PluginManager pluginManager = this.getServer().getPluginManager();
        playerChatListener = new PlayerChatListener(this);
        pluginManager.registerEvents(playerChatListener, this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new PlayerDeathListener(this), this);
        pluginManager.registerEvents(new PlayerAdvancementDoneListener(this), this);

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

        // Make Discord Manager
        manager = new DiscordManager(this);

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

    // Load Language File
    public void loadLangConfig() {
        try {
            String langString = this.getConfig().getString("lang");
            Path langConfigPath = Paths.get(this.getDataFolder() + "/lang/" + langString + ".yml");
            Files.createDirectories(langConfigPath.getParent());
            if (!Files.exists(langConfigPath)) {
                try {
                    this.saveResource("lang/" + langString + ".yml", false);
                } catch (IllegalArgumentException e) {
                    (new InvalidConfigurationException("Invalid language file! Using default en.yml")).printStackTrace();
                    langConfigPath = Paths.get(this.getDataFolder() + "/lang/en.yml");
                    this.saveResource("lang/en.yml", false);
                }
            }

            lang = new YamlConfiguration();
            lang.load(langConfigPath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    // Language Get String
    public final String getLangString(String key, String[]... arguments) throws NullPointerException {
        // Fetch Value
        String value = lang.getString(key);
        if (value == null) {
            throw new NullPointerException("Lang key cannot be null!");
        }

        // Fetch Prefix
        String prefix = lang.getString("prefix");
        if (prefix == null) {
            prefix = "[Discord] ";
        }

        // Replace Arguments
        if (arguments != null) {
            for (String[] argument : arguments) {
                value = value.replace(argument[0], argument[1]);
            }
        }

        return value.replace("%prefix", prefix);
    }
    public final String getLangString(String key) {
        return this.getLangString(key, null);
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
    public FileConfiguration getLangConfig() {
        return lang;
    }
    public DiscordManager getDiscordManager() {
        return manager;
    }
    public DiscordApi getDiscord() {
        return discord;
    }

}
