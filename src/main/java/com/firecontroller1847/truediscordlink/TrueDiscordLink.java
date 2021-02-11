package com.firecontroller1847.truediscordlink;

import com.earth2me.essentials.Essentials;
import com.firecontroller1847.truediscordlink.commands.CommandTrueDiscordLink;
import com.firecontroller1847.truediscordlink.listeners.minecraft.*;
import com.firecontroller1847.truediscordlink.tabcompleters.TabCompleterTrueDiscordLink;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;

import java.io.IOException;
import java.util.Objects;

public class TrueDiscordLink extends FirePlugin {

    // Dependent Plugins
    private PlaceholderAPIPlugin placeholderApi;
    private Essentials essentialsApi;

    // Managers
    private VersionHelper versionHelper;
    private DiscordManager discordManager;
    private DatabaseManager databaseManager;

    // After Configuration
    @Override
    public boolean onAfterConfiguration() {
        // Load dependent plugins
        placeholderApi = (PlaceholderAPIPlugin) this.loadPlugin("PlaceholderAPI");
        essentialsApi = (Essentials) this.loadPlugin("Essentials");

        // Ensure that we are configured
        if (!this.getConfig().getBoolean("configured")) {
            this.getLogger().warning("TrueDiscordLink has not been configured! Configure the config.yml file and then reload the plugin.");
            return false;
        }

        // Migrate configurations
        try {
            this.migrateConfigurations();
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("There was an error attempting to migrate the old configurations to the new ones. Please contact the developer(s), " + this.getDescription().getAuthors().get(0) + ".");
            return false;
        }

        // Initialize Managers
        versionHelper = new VersionHelper(this);
        discordManager = new DiscordManager(this);
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

        // If we haven't returned already, it was a success
        return true;
    }

    @Override
    public void onShutdown() {
        if (this.getConfig().getBoolean("configured")) {
            // Disable Discord Manager
            discordManager.shutdown();

            // Disable Database Manager
            databaseManager.disconnect();
        }
    }

    // TODO: FirePlugin should handle this stuff!
    private void migrateConfigurations() throws IOException {
        this.getLogger().info("Checking for version migrations...");
        boolean migrated = false;

        // v1.0.3 -> v1.0.4
        if (!translations.contains("messages.to_mc_attachment_color")) {
            translations.set("messages.to_mc_attachment_color", "§9§n");
            this.getLogger().info("[en.yml] Added messages.to_mc_attachment_color");
            migrated = true;
        }

        // Save
        if (migrated) {
            this.saveConfig();
            this.saveTranslations();
            this.getLogger().info("Migrations saved!");
        } else {
            this.getLogger().info("No migrations found!");
        }
    }

    // Escapes formatting for Discord messages
    public static String escapeDiscordFormatting(String content) {
        return content.replace("*", "\\*").replace("_", "\\_").replace("~", "\\~");
    }

    // Getters
    public PlaceholderAPIPlugin getPlaceholderApi() {
        return placeholderApi;
    }
    public Essentials getEssentialsApi() {
        return essentialsApi;
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
