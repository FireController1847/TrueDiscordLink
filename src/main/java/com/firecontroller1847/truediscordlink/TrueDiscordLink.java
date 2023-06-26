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
            this.getLogger().severe("TrueDiscordLink has not been configured! Configure the config.yml file and then reload the plugin.");
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
        pluginManager.registerEvents(new CommandListener(this), this);

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
        if (!translations.contains("messages.to_mc_attachment_color", true)) {
            translations.set("messages.to_mc_attachment_color", "§9§n");
            this.getLogger().info("[en.yml] Added messages.to_mc_attachment_color");
            migrated = true;
        }

        // v1.0.4 -> v1.1.0
        if (!this.getConfig().contains("tagging.enable_shortcut_use_database", true)) {
            this.getConfig().set("tagging.enable_shortcut_use_database", true);
            this.getLogger().info("[config.yml] Added tagging.enable_shortcut_use_database");
            migrated = true;
        }

        // v1.1.0 -> v1.2.0
        if (!this.getConfig().contains("tagging.enable_channel_tagging", true)) {
            this.getConfig().set("tagging.enable_channel_tagging", true);
            this.getLogger().info("[config.yml] Added tagging.enable_channel_tagging");
            migrated = true;
        }
        if (!translations.contains("tagging.minecraft_channel_tag_color", true)) {
            translations.set("tagging.minecraft_channel_tag_color", "§a#%name%§r");
            this.getLogger().info("[en.yml] Added tagging.minecraft_channel_tag_color");
            migrated = true;
        }
        if (!this.getConfig().contains("bot.linking.notify", true)) {
            this.getConfig().set("bot.linking.notify.link.enabled", false);
            this.getConfig().set("bot.linking.notify.link.channel", "000000000000000000");
            this.getConfig().set("bot.linking.notify.unlink.enabled", false);
            this.getConfig().set("bot.linking.notify.unlink.channel", "000000000000000000");
            this.getLogger().info("[config.yml] Added bot.linking.notify.*");
            migrated = true;
        }
        if (!translations.contains("linking.discord.notify", true)) {
            translations.set("linking.discord.notify.link", "%name% has linked with %mention%!");
            translations.set("linking.discord.notify.unlink", "%name% has unlinked with %mention%!");
            this.getLogger().info("[en.yml] Added linking.discord.notify.*");
            migrated = true;
        }

        // v1.2.0 -> v1.3.0
        if (!translations.contains("no_console_usage", true)) {
            translations.set("no_console_usage", "%prefix% §cYou cannot use this command from console!");
            this.getLogger().info("[en.yml] Added no_console_usage");
            migrated = true;
        }
        if (!translations.contains("commands.relay.no_message", true)) {
            translations.set("commands.relay.no_message", "%prefix% §cYou must include a message for me to relay.");
            this.getLogger().info("[en.yml] Added commands.relay.no_message");
            migrated = true;
        }
        if (!translations.contains("commands.relay.no_discord_communication", true)) {
            translations.set("commands.relay.no_discord_communication", "%prefix% §cThere is no way for me to communicate with Discord! Ensure you have either webhooks enabled or a bot enabled with 'from_mc_channels' configured!");
            this.getLogger().info("[en.yml] Added commands.relay.no_discord_communication");
            migrated = true;
        }
        if (!translations.contains("commands.relay.success", true)) {
            translations.set("commands.relay.success", "%prefix% Message successfully relayed to the Discord server.");
            this.getLogger().info("[en.yml] Added commands.relay.success");
            migrated = true;
        }
        if (!this.getConfig().contains("events.relay_tellraw_messages")) {
            this.getConfig().set("events.relay_tellraw_messages", true);
            this.getLogger().info("[config.yml] Added events.relay_tellraw_messages");
            migrated = true;
        }

        // v1.3.0 -> v1.3.3
        if (Objects.requireNonNull(translations.getString("linking.discord.notify.link")).contains("%username%")) {
            translations.set("linking.discord.notify.link", this.getTranslation("linking.discord.notify.link", false).replace("%username%", "%name%"));
            migrated = true;
        }
        if (Objects.requireNonNull(translations.getString("linking.discord.notify.unlink")).contains("%username%")) {
            translations.set("linking.discord.notify.unlink", this.getTranslation("linking.discord.notify.link", false).replace("%username%", "%name%"));
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
