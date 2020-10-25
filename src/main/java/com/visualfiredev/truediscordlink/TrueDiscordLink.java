package com.visualfiredev.truediscordlink;

import com.visualfiredev.truediscordlink.commands.CommandTrueDiscordLink;
import com.visualfiredev.truediscordlink.listeners.*;
import com.visualfiredev.truediscordlink.tabcompleters.TabCompleterTrueDiscordLink;
import org.bukkit.ChatColor;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrueDiscordLink extends JavaPlugin {

    // Static Variables
    private static TrueDiscordLink instance;

    // Hex Pattern
    public static final Pattern HEX_PATTERN = Pattern.compile("§#(\\w{5}[0-9a-f])");

    // Instance Variables
    private FileConfiguration lang;
    private DiscordApi discord;
    private DiscordManager manager;
    private PlayerChatListener playerChatListener;
    private DiscordChatListener discordChatListener;
    private DiscordEditListener discordEditListener;

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
        try {
            this.validateConfig();
        } catch (Exception e) {
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Check if Configured
        if (!this.getConfig().getBoolean("configured")) {
            this.getLogger().warning("TrueDiscordLink has not been configured! Configure the config.yml file and then reload the plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

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

        // Make Discord Manager
        manager = new DiscordManager(this);

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

                discordEditListener = new DiscordEditListener(this);
                api.addListener(discordEditListener);

                // Assign to Variable & Output Login Status
                discord = api;
                this.getLogger().info("Logged in!");

                // Send Server Started Message
                if (this.getConfig().getBoolean("events.server_start")) {
                    manager.sendDiscordMessage(getLangString("events.server_start"));
                }

                // Begin Loops
                manager.statusLoop(0);
                manager.channelTopicLoop();
            }).exceptionally(ExceptionLogger.get());
        } else {
            // Send Server Started Message
            if (this.getConfig().getBoolean("events.server_start")) {
                manager.sendDiscordMessage(getLangString("events.server_start"));
            }
        }

        // Output Plugin Enabled
        this.getLogger().info("TrueDiscordLink enabled!");
    }

    // Plugin Disable Listener
    @Override
    public void onDisable() {
        // Check if Configured
        if (!this.getConfig().getBoolean("configured")) {
            return;
        }

        // Send Shutdown Message
        if (this.getConfig().getBoolean("events.server_shutdown")) {
            manager.sendDiscordMessage(getLangString("events.server_shutdown"), true);
        }

        // Remove DiscordManager Loop Threads
        if (manager.statusLoopThread != null) {
            manager.statusLoopThread.interrupt();
        }
        if (manager.channelTopicLoopThread != null) {
            manager.channelTopicLoopThread.interrupt();
        }

        // Remove Discord Event Listeners & Log Out
        if (discord != null) {
            discord.removeListener(discordChatListener);
            discord.disconnect();
            try {
                discord.getThreadPool().getExecutorService().awaitTermination(5, TimeUnit.SECONDS);
                discord = null;
            } catch (InterruptedException e) {
                // It's not the end of the world, I suppose
            }
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

    // Validates all of the configuration values
    public void validateConfig() throws Exception {
        FileConfiguration config = getConfig();

        // Check webhooks
        if (config.getBoolean("webhooks.enabled")) {

            // Validate URLs
            List<String> webhookUrls = config.getStringList("webhooks.urls");
            if (webhookUrls.size() == 0) {
                throw new InvalidConfigurationException("Webhooks are enabled but there are no webhook urls!");
            } else {
                for (String webhookUrl : webhookUrls) {
                    try {
                        new URL(webhookUrl);
                    } catch (MalformedURLException e) {
                        throw new MalformedURLException("Invalid Webhook URL! " + webhookUrl);
                    }
                }
            }

            // Validate Avatar URL
            if (config.getBoolean("webhooks.use_avatar")) {
                String skinsUrl = config.getString("webhooks.skins_url");
                if (skinsUrl == null) {
                    throw new InvalidConfigurationException("Missing Skins URL!");
                } else {
                    try {
                        new URL(skinsUrl);
                    } catch (MalformedURLException e) {
                        throw new MalformedURLException("Invalid Skins URL!");
                    }
                }
            }

        }

        // Bot Configuration
        if (config.getBoolean("bot.enabled")) {

            // Validate Mention Servers
            List<Long> mentionServersIds = config.getLongList("tagging.mention_servers");
            if (!config.getBoolean("tagging.mention_discord_users") && mentionServersIds.size() == 0) {
                throw new InvalidConfigurationException("Invalid amount of mention servers!");
            }

        }

    }

    // Language Get String
    public final String getLangString(String key, boolean colors, String[]... arguments) throws NullPointerException {
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
        value = value.replace("%prefix", prefix);

        // Replace Arguments
        if (arguments != null) {
            for (String[] argument : arguments) {
                value = value.replace(argument[0], argument[1]);
            }
        }

        if (colors) {
            return translateHexCodes(value);
        } else {
            return value;
        }
    }
    public final String getLangString(String key, boolean colors) {
        return this.getLangString(key, colors, null);
    }
    public final String getLangString(String key, String[]... arguments) {
        return this.getLangString(key, true, arguments);
    }
    public final String getLangString(String key) {
        return this.getLangString(key, true);
    }

    // Utility method to translate hex codes to color codes
    // Special thanks to the Spigot community!
    // https://www.spigotmc.org/threads/hex-color-code-translate.449748/#post-3867795
    public static String translateHexCodes(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes('§', matcher.appendTail(buffer).toString());
    }

    // Utility method to strip hex codes and color codes
    public static String stripHexCodes(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "");
        }
        return ChatColor.stripColor(matcher.appendTail(buffer).toString());
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
