package com.firecontroller1847.truediscordlink;

import com.firecontroller1847.truediscordlink.commands.CommandTrueDiscordLink;
import com.firecontroller1847.truediscordlink.listeners.minecraft.*;
import com.firecontroller1847.truediscordlink.tabcompleters.TabCompleterTrueDiscordLink;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public class TrueDiscordLink extends FirePlugin {

    // Dependent Plugins
    private PlaceholderAPIPlugin placeholderApi;

    // Managers
    private VersionHelper versionHelper;
    private DiscordManager discordManager;
    private DatabaseManager databaseManager;

    // After Configuration
    @Override
    public boolean onAfterConfiguration() {
        // Load dependent plugins
        placeholderApi = (PlaceholderAPIPlugin) this.loadPlugin("PlaceholderAPI");

        // Ensure that we are configured
        if (!this.getConfig().getBoolean("configured")) {
            this.getLogger().warning("TrueDiscordLink has not been configured! Configure the config.yml file and then reload the plugin.");
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
    public void onDisable() {
        super.onDisable();

        if (this.getConfig().getBoolean("configured")) {
            // Disable Discord Manager
            discordManager.shutdown();

            // Disable Database Manager
            databaseManager.disconnect();
        }
    }

    // Escapes formatting for Discord messages
    public String escapeDiscordFormatting(String content) {
        return content.replace("*", "\\*").replace("_", "\\_").replace("~", "\\~");
    }

    // Getters
    public PlaceholderAPIPlugin getPlaceholderApi() {
        return placeholderApi;
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
