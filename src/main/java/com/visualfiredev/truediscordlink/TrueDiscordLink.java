package com.visualfiredev.truediscordlink;

import com.visualfiredev.truediscordlink.commands.CommandTrueDiscordLink;
import com.visualfiredev.truediscordlink.listeners.minecraft.*;
import com.visualfiredev.truediscordlink.tabcompleters.TabCompleterTrueDiscordLink;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrueDiscordLink extends JavaPlugin {

    // Singleton
    private static TrueDiscordLink instance;

    // Variables
    private FileConfiguration translations;
    private VersionHelper versionHelper;
    private DiscordManager discordManager;
    private DatabaseManager databaseManager;

    // Constructor
    public TrueDiscordLink() {
        instance = this;
    }

    // On Enable
    @Override
    public void onEnable() {
        // Setup Configuration & Language
        this.saveDefaultConfig();
        this.loadTranslations();

        // Ensure that we are configured
        if (!this.getConfig().getBoolean("configured")) {
            this.getLogger().warning("TrueDiscordLink has not been configured! Configure the config.yml file and then reload the plugin.");
            this.disable();
            return;
        }

        // Initialize Version Helper
        versionHelper = new VersionHelper(this);

        // Initialize Discord Manager
        discordManager = new DiscordManager(this);

        // Initialize Database Manager
        databaseManager = new DatabaseManager(this);

        // Register Commands
        PluginCommand cmdtdl = Objects.requireNonNull(this.getCommand("truediscordlink"));
        cmdtdl.setExecutor(new CommandTrueDiscordLink());
        cmdtdl.setTabCompleter(new TabCompleterTrueDiscordLink());

        // Register Events
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerChatListener(this), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new PlayerDeathListener(this), this);
        pluginManager.registerEvents(new PlayerAdvancementDoneListener(this), this);

        // Log Enabled
        this.getLogger().info("Enabled TrueDiscordLink!");
    }

    // On Disable
    @Override
    public void onDisable() {
        if (this.getConfig().getBoolean("configured")) {
            // Disable Discord Manager
            discordManager.shutdown();

            // Disable Database Manager
            databaseManager.disconnect();
        }

        // Log Disabled
        this.getLogger().info("Disabled TrueDiscordLink!");
    }

    // Utility methods to make disabling and enabling the plugin easier
    public void enable() {
        this.getServer().getPluginManager().enablePlugin(this);
    }
    public void disable() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

    // Utility method to load the language file
    public void loadTranslations() {
        // Destroy if exists
        if (translations != null) {
            translations = null;
        }

        try {
            // Prepare File
            String code = this.getConfig().getString("lang");
            Path filePath = Paths.get(this.getDataFolder() + "/lang/" + code + ".yml");

            // Ensure Directories Exist
            Files.createDirectories(filePath.getParent());

            // Ensure File Exists
            if (!Files.exists(filePath)) {
                try {
                    this.saveResource("lang/" + code + ".yml", false);
                } catch (IllegalArgumentException e) {
                    (new InvalidConfigurationException("Invalid language file! Using default en.yml")).printStackTrace();
                    filePath = Paths.get(this.getDataFolder() + "/lang/en.yml");
                    this.saveResource("lang/en.yml", false);
                }
            }

            // Load Configuration
            translations = new YamlConfiguration();
            translations.load(filePath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            this.disable();
        }
    }

    // Utility method to make language handling easier
    public String getTranslation(String key, boolean parseColors, String[]... arguments) {
        // Fetch Value
        String value = translations.getString(key);
        if (value == null) {
            throw new NullPointerException("Lang key cannot be null!");
        }

        // Fetch Prefix
        String prefix = translations.getString("prefix");
        if (prefix == null) {
            prefix = "[Discord] ";
        }
        value = value.replace("%prefix%", prefix);

        // Replace Arguments
        if (arguments != null) {
            for (String[] argument : arguments) {
                value = value.replace(argument[0], this.escapeDiscordFormatting(argument[1]));
            }
        }

        // Translate Colors & Return
        if (parseColors) {
            return translateColorCodes(value);
        } else {
            return value;
        }
    }
    public String getTranslation(String key, boolean parseColors) {
        return this.getTranslation(key, parseColors, null);
    }
    public String getTranslation(String key, String[]... arguments) {
        return this.getTranslation(key, true, arguments);
    }
    public String getTranslation(String key) {
        return this.getTranslation(key, true, null);
    }

    public String escapeDiscordFormatting(String content) {
        return content.replace("*", "\\*").replace("_", "\\_").replace("~", "\\~");
    }

    // Utility method to translate color codes
    // Special thanks to the Spigot community for hex color code parsing!
    // https://www.spigotmc.org/threads/hex-color-code-translate.449748/#post-3867795
    public static String translateColorCodes(char alt, String text) {
        Matcher matcher = Pattern.compile(alt + "#(\\w{5}[0-9A-Fa-f])").matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes(alt, matcher.appendTail(buffer).toString());
    }
    public static String translateColorCodes(String text) {
        return translateColorCodes('§', text);
    }

    // Utility method to strip color codes
    public static String stripColorCodes(char alt, String text) {
        Matcher matcher = Pattern.compile(alt + "#(\\w{5}[0-9A-Fa-f])").matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "");
        }
        return ChatColor.stripColor(matcher.appendTail(buffer).toString());
    }
    public static String stripColorCodes(String text) {
        return stripColorCodes('§', text);
    }

    // Utility method to determine whether or not a command sender has a permission
    public static boolean hasPermission(CommandSender sender, String permission) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission(permission);
    }

    // Utility method to yell at a user for having no permission
    public static void tellNoPermission(CommandSender sender) {
        sender.sendMessage(instance.getTranslation("no_permission"));
    }

    // Utility method to call a sub-command while checking the permission for the command
    public static boolean runCommand(CommandExecutor executor, String permission, CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, permission)) {
            tellNoPermission(sender);
            return true;
        } else {
            return executor.onCommand(sender, command, label, args);
        }
    }

    // Getters
    public static TrueDiscordLink getInstance() {
        return instance;
    }
    public FileConfiguration getTranslationsConfig() {
        return translations;
    }
    public VersionHelper getVersionHelper() {
        return versionHelper;
    }
    public DiscordManager getDiscordManager() {
        return discordManager;
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

}
