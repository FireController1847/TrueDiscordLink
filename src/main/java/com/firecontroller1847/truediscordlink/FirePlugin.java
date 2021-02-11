package com.firecontroller1847.truediscordlink;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *     This class defines multiple utility methods which is supposed to make
 *     plugin development a lot easier. To use this class, you need to have
 *     a config.yml file in your resources, with the bare minimum in the file
 *     being a "lang: en" option. This is used in {@link FirePlugin#loadTranslations}.
 *     <br><br>
 *     To use the language options, you need to create a lang/en.yml file
 *     in your resources, with the bare minimum in the file being a
 *     "no_permission" language string for when users do not have
 *     permission to run a command. This is used in {@link FirePlugin#runCommand}.
 *     While a "prefix" string is not required, it is strongly recommended,
 *     otherwise the default prefix will be used which is [PluginName].
 *     <br><br>
 *     You are allowed to copy, use, and distribute this in your programs, as long as
 *     you do not remove the author, version, and this copyright notice.
 *     <br><br>
 *     All rights reserved.
 * </p>
 *
 * @author FireController#1847
 * @version 2
 */
// TODO: Add support for multiple configurations
public abstract class FirePlugin extends JavaPlugin {

    // The instance of this plugin for singleton usage.
    protected static FirePlugin instance;

    // Variables
    private Path translationsFile; // The file for the translations file
    // TODO: Remove protected when we handle configuration migrations
    protected FileConfiguration translations; // The translations file configuration
    private LinkedHashMap<String, Thread> loops = new LinkedHashMap<>(); // Utility to allow for looping methods

    /**
     * <p>
     *     Sets up the configuration and translations and logs enablement.
     *     <br><br>
     *     Cannot be overridden. For similar execution, override {@link FirePlugin#onAfterConfiguration()}.
     * </p>
     * @see FirePlugin#onAfterConfiguration()
     */
    @Override
    public final void onEnable() {
        instance = this;

        // Called before configuring
        boolean success1 = this.onBeforeConfiguration();
        if (!success1) {
            disable();
            return;
        }

        // Sets up the default configuration
        this.saveDefaultConfig();

        // Sets up the translations
        this.loadTranslations();

        // Called after configuring
        boolean success2 = this.onAfterConfiguration();
        if (!success2) {
            disable();
            return;
        }

        // Log Enabled
        this.getLogger().info("Enabled " + this.getName() + "!");
    }

    /**
     * <p>
     *     This is an optional method that can be overridden before the configuration
     *     is ready. This method is provided since the instance has already been set.
     *     <br><br>
     *     If you return false, it will automatically disable the plugin.
     *     Please note that it will not send an error message.
     * </p>
     * @return Whether or not this method was successful and to continue setting up.
     */
    public boolean onBeforeConfiguration() {
        return true; // This method does nothing, it's here in case the user wants to override it.
    }

    /**
     * <p>
     *     This is the primary method you should override when setting up anything in your plugin.
     *     <br><br>
     *     Called after the configuration has been loaded.
     *     <br><br>
     *     If you return false, it will automatically disable the plugin.
     *     Please note that it will not send an error message.
     * </p>
     * @return Whether or not the setup was successful and to continue setting up.
     */
    public boolean onAfterConfiguration() {
        return true; // This method does nothing, it's here in case the user wants to override it.
    }

    /**
     * <p>
     *     This method is called after the configuration as been reloaded. Similar to
     *     {@link FirePlugin#onAfterConfiguration()}, but should not be used for
     *     first-time setup.
     *     <br><br>
     *     If you return false, it will automatically disable the plugin.
     *     Please note that it will not send an error message.
     * </p>
     */
    public boolean onConfigReload() {
        return true; // This method does nothing, it's here in case the user wants to override it.
    }

    /**
     * This is the primary method you should override when cleaning up anything in your plugin.
     * <br><br>
     * Called before all threads have been shut down.
     */
    public void onShutdown() {
        // This method does nothing, it's here in case the user wants to override it.
    }

    /**
     * Shuts down all loops and logs disablement
     */
    @Override
    public final void onDisable() {
        // Called before thread shutdown
        this.onShutdown();

        // Shutdown Loops
        for (Thread thread : loops.values()) {
            thread.interrupt();
        }

        // Log Disabled
        this.getLogger().info("Disabled " + this.getName() + "!");
    }

    /**
     * Enables the plugin.
     */
    public void enable() {
        this.getServer().getPluginManager().enablePlugin(this);
    }

    /**
     * Disables the plugin.
     */
    public void disable() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Reloads the configuration and translation files for this plugin.
     */
    public void reload() {
        this.reloadConfig();
        this.loadTranslations();
        boolean success = this.onConfigReload();
        if (!success) {
            disable();
        }
    }

    /**
     * Attempts to load a plugin. See {@link org.bukkit.plugin.PluginManager#getPlugin(String)}
     * @param plugin The name of the plugin to load.
     * @return The {@link Plugin}
     */
    public Plugin loadPlugin(String plugin) {
        return this.getServer().getPluginManager().getPlugin(plugin);
    }

    /**
     * Loads the translations configuration file.
     *
     * <p>
     *     The language files are created in the lang/ file in the plugin's
     *     folder. The default configuration is English or en.yml. By using
     *     a "lang" configuration option, it will define the language to
     *     be used.
     *     <br><br>
     *     Users can create their own language files simply by putting their
     *     own language into the lang configuration option and then creating
     *     a language file with that name.
     * </p>
     */
    protected void loadTranslations() {
        // Destroy it if it exists
        if (translationsFile != null || translations != null) {
            translationsFile = null;
            translations = null;
        }

        try {
            // Prepare File
            String code = this.getConfig().getString("lang");
            if (code == null) {
                code = "en";
            }
            translationsFile = Paths.get(this.getDataFolder() + "/lang/" + code + ".yml");

            // Ensure Directories Exist
            Files.createDirectories(translationsFile.getParent());

            // Ensure File Exists
            if (!Files.exists(translationsFile)) {
                try {
                    this.saveResource("lang/" + code + ".yml", false);
                } catch (IllegalArgumentException e) {
                    (new InvalidConfigurationException("Invalid language file! Using default en.yml")).printStackTrace();
                    translationsFile = Paths.get(this.getDataFolder() + "/lang/en.yml");
                    this.saveResource("lang/en.yml", false);
                }
            }

            // Load Configuration
            translations = new YamlConfiguration();
            translations.load(translationsFile.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            this.disable();
        }
    }

    /**
     * Saves the translations configuration file.
     */
    protected void saveTranslations() throws IOException {
        translations.save(translationsFile.toFile());
    }

    /**
     * <p>
     *     Fetches a translation from the translations configuration.
     *     <br><br>
     *     Arguments work in a key/value pair configuration. For example,
     *     I have a key %user%, and I want the value to be "MyUser". I would
     *     pass a <code>new String[] { "%user%, "MyUser" }</code>, which would then replace
     *     %user% in the translation file with "MyUser".
     * </p>
     *
     * @param key The key for this translation, with indents being notated by periods.
     * @param parseColors Whether or not colors should be parsed for this translation.
     * @param arguments A list of key/value pairs, in which the key will be replaced with the value.
     * @return The translated string.
     */
    public String getTranslation(String key, boolean parseColors, String[]... arguments) {
        // Fetch Value
        String value = translations.getString(key);
        if (value == null) {
            throw new NullPointerException("Lang key cannot be null!");
        }

        // Fetch Prefix
        String prefix = translations.getString("prefix");
        if (prefix == null) {
            prefix = "[" + this.getName() + "] ";
        }
        value = value.replace("%prefix%", prefix);

        // Replace Arguments
        if (arguments != null) {
            for (String[] argument : arguments) {
                value = value.replace(argument[0], argument[1]);
            }
        }

        // Translate Colors & Return
        if (parseColors) {
            return translateColorCodes(value);
        } else {
            return value;
        }
    }

    /**
     * Fetches a translation from the translations configuration with no arguments.
     * @see FirePlugin#getTranslation(String, boolean, String[]...)
     */
    public String getTranslation(String key, boolean parseColors) {
        return this.getTranslation(key, parseColors, null);
    }

    /**
     * Fetches a translation from the translations configuration while parsing colors.
     * @see FirePlugin#getTranslation(String, boolean, String[]...)
     */
    public String getTranslation(String key, String[]... arguments) {
        return this.getTranslation(key, true, arguments);
    }

    /**
     * Fetches a translation from the translations configuration while parsing colors and with no arguments.
     * @see FirePlugin#getTranslation(String, boolean, String[]...)
     */
    public String getTranslation(String key) {
        return this.getTranslation(key, true, null);
    }

    /**
     * Adds a loop to this plugin using the specified time as a delay between loops.
     *
     * <p>
     *     If a loop is interrupted, it will not automatically re-start. The
     *     loop will be automatically removed from the loop array and this
     *     method will need to be called again.
     * </p>
     *
     * @param id The id for this loop to prevent multiple loops running and to remove loops.
     * @param delay The delay in milliseconds.
     * @param consumer The method to be called every loop.
     * @param parameters The parameters to be passed to the consumer.
     */
    public void addLoop(String id, int delay, Consumer<Object> consumer, Object parameters) {
        // Interrupt existing loop
        if (loops.containsKey(id)) {
            loops.get(id).interrupt();
            loops.remove(id);
        }

        // Create thread
        Thread thread = new Thread(() -> loop(id, delay, consumer, parameters));

        // Add thread to list and start it
        loops.put(id, thread);
        thread.start();
    }

    /**
     * Adds a loop with no parameters.
     * @see FirePlugin#addLoop(String, int, Consumer, Object)
     */
    public void addLoop(String id, int delay, Runnable runnable) {
        this.addLoop(id, delay, Void -> runnable.run(), null);
    }

    /**
     * Removes a loop.
     * <p>
     *     A loop is only interrupted during the delay phase unless
     *     the user has manually added interruption catches in the
     *     method provided.
     * </p>
     *
     * @param id The id for the loop to be removed.
     */
    public void removeLoop(String id) {
        if (loops.containsKey(id)) {
            loops.get(id).interrupt();
            loops.remove(id);
        }
    }

    /**
     * Loops a method using the specified delay.
     *
     * @param id The string that identifies this loop.
     * @param delay The delay in milliseconds between loops.
     * @param consumer The method to be called every loop.
     * @param parameters The paramaters to be passed to the consumer.
     */
    private void loop(String id, int delay, Consumer<Object> consumer, Object parameters) {
        try {
            // Run the work
            consumer.accept(parameters);

            // Wait the time
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // Remove thread from list
            loops.remove(id);

            // Return to prevent looping again
            return;
        }

        // Loop
        loop(id, delay, consumer, parameters);
    }

    /**
     * Translates color codes in the specified text using the alternate character.
     *
     * @param alt The alternate character to use.
     * @param text The text to have colors parsed.
     * @return The text with all colors parsed.
     */
    public static String translateColorCodes(char alt, String text) {
        Matcher matcher = Pattern.compile(alt + "#(\\w{5}[0-9A-Fa-f])").matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes(alt, matcher.appendTail(buffer).toString());
    }

    /**
     * Translates color codes in the specified text using Minecraft's default color code character (\u00a7).
     * Useful for translating hex codes \u00a7#FFFFFF -&gt; \u00a7x\u00a7F\u00a7F\u00a7F\u00a7F\u00a7F\u00a7F
     *
     * @param text The text to have colors parsed.
     * @return The text with all colors parsed.
     */
    public static String translateColorCodes(String text) {
        return translateColorCodes('\u00a7', text);
    }

    /**
     * Strips color codes from the specified text using the alternate character.
     * @param alt The alternate character to use.
     * @param text The text to have colors stripped.
     * @return The text with all colors stripped.
     */
    public static String stripColorCodes(char alt, String text) {
        Matcher matcher = Pattern.compile(alt + "#(\\w{5}[0-9A-Fa-f])").matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "");
        }
        return org.bukkit.ChatColor.stripColor(matcher.appendTail(buffer).toString());
    }

    /**
     * Strips color codes from the specified text using Minecraft's default color code character (\u00a7).
     * Useful for stripping hex codes \u00a7#FFFFFF -&gt; \u00a7x\u00a7F\u00a7F\u00a7F\u00a7F\u00a7F\u00a7F
     *
     * @param text The text to have colors stripped.
     * @return The text with all colors stripped.
     */
    public static String stripColorCodes(String text) {
        return stripColorCodes('\u00a7', text);
    }

    /**
     * Determines whether or not the specified command sender has the specified permission.
     * A console has all permissions.
     *
     * @param sender The command sender.
     * @param permission The permission to check.
     * @return Whether or not the command sender has the permission.
     */
    public static boolean hasPermission(CommandSender sender, String permission) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission(permission);
    }

    /**
     * Alerts a user that they do not have permission to run a specific command.
     * Ensure that you have a "no_permission" translation string in your translations file.
     *
     * @param sender The command sender to yell at.
     */
    public static void tellNoPermission(CommandSender sender) {
        sender.sendMessage(instance.getTranslation("no_permission"));
    }

    /**
     * Checks for permission to run a command, and then runs the specified command.
     * Mostly used for sub-commands.
     *
     * @param executor The command.
     * @param permission The permission to check for.
     * @param sender The arguments for {@link CommandExecutor#onCommand}
     * @param command The arguments for {@link CommandExecutor#onCommand}
     * @param label The arguments for {@link CommandExecutor#onCommand}
     * @param args The arguments for {@link CommandExecutor#onCommand}
     * @return Whether or not the command succeeded.
     * @see CommandExecutor#onCommand
     */
    public static boolean runCommand(CommandExecutor executor, String permission, CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, permission)) {
            tellNoPermission(sender);
            return true;
        } else {
            return executor.onCommand(sender, command, label, args);
        }
    }

    /**
     * Returns the instance for this plugin.
     * @return The instance for this plugin.
     */
    public static FirePlugin getInstance() {
        return instance;
    }

}