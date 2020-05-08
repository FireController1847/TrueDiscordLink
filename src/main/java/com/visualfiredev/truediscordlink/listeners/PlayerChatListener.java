package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PlayerChatListener implements Listener {

    // Instance Variables
    private final TrueDiscordLink discordlink;
    private List<String> webhookUrls;
    private List<Long> channelIds;
    private String skinsUrl;
    private boolean useAvatar;
    private boolean initialized = false;

    // Constructor
    public PlayerChatListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    // Event Handler
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Load Configuration
        if (!initialized && !initialize()) {
            return;
        }

        // Webhooks
        if (webhookUrls != null) {

            // Prepare Skin
            String skin = null;
            if (useAvatar) {
                skin = skinsUrl.replace("%uuid", event.getPlayer().getUniqueId().toString());
            }

            // Send Message
            for (String webhookUrl : webhookUrls) {
                sendWebhookMessage(ChatColor.translateAlternateColorCodes('&',
                    discordlink.getLangString("messages.webhook_relay_format",
                        new String[] { "%name", event.getPlayer().getName() },
                        new String[] { "%displayname", event.getPlayer().getDisplayName() },
                        new String[] { "%uuid", event.getPlayer().getUniqueId().toString() },
                        new String[] { "%message", event.getMessage() }
                    )
                ), event.getPlayer().getName(), skin, webhookUrl);
            }

        }

        // Bot
        if (channelIds != null && channelIds.size() > 0) {

            // Find Channel & Send Message
            for (long channelId : channelIds) {
                discordlink.getDiscord().getTextChannelById(channelId).ifPresent(channel -> {
                    channel.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        discordlink.getLangString("messages.bot_relay_format",
                            new String[] { "%name", event.getPlayer().getName() },
                            new String[] { "%displayname", event.getPlayer().getDisplayName() },
                            new String[] { "%uuid", event.getPlayer().getUniqueId().toString() },
                            new String[] { "%message", event.getMessage() }
                        )
                    ));
                });
            }

        }
    }

    // Sends a message via a webhook
    private void sendWebhookMessage(String content, String username, String skin, String webhookUrl) {
        try {
            // Make Connection
            HttpsURLConnection connection = (HttpsURLConnection) new URL(webhookUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Make JSON Data
            byte[] data;
            if (skin != null) {
                data = ("{\"content\": \"" + content.replace("\"", "\\\"") + "\", \"username\": \"" + username + "\", \"avatar_url\": \"" + skin + "\"}").getBytes(StandardCharsets.ISO_8859_1);
            } else {
                data = ("{\"content\": \"" + content.replace("\"", "\\\"") + "\", \"username\": \"" + username + "\"}").getBytes(StandardCharsets.ISO_8859_1);
            }
            int length = data.length;

            // Add Data & Make Request
            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type", "application/json; charset=ISO_8859_1");
            connection.connect();
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(data);
            }

            // Get Response Code
            int code = connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Configuration Cache Initialization
    private boolean initialize() {
        FileConfiguration config = discordlink.getConfig();

        // Webhooks Configuration
        if (config.getBoolean("webhooks.enabled")) {

            // Load Webook URLs
            webhookUrls = config.getStringList("webhooks.urls");
            if (webhookUrls.size() == 0) {
                (new InvalidConfigurationException("Webhooks are enabled but there are no webhook urls!")).printStackTrace();
                return false;
            } else {
                for (String webhookUrl : webhookUrls) {
                    try {
                        new URL(webhookUrl);
                    } catch (MalformedURLException e) {
                        (new MalformedURLException("Invalid Webhook URL! " + webhookUrl)).printStackTrace();
                        return false;
                    }
                }
            }

            // Avatar Configuration
            useAvatar = config.getBoolean("webhooks.use_avatar");
            if (useAvatar) {
                // Load Skins URL
                skinsUrl = config.getString("webhooks.skins_url");
                if (skinsUrl == null) {
                    (new InvalidConfigurationException("Missing Skins URL!")).printStackTrace();
                    return false;
                } else {
                    try {
                        new URL(skinsUrl);
                    } catch (MalformedURLException e) {
                        (new MalformedURLException("Invalid Skins URL!")).printStackTrace();
                        skinsUrl = null;
                        return false;
                    }
                }
            }

        }

        // Bot Configuration
        if (config.getBoolean("bot.enabled")) {
            channelIds = config.getLongList("bot.relay_channels");
        }

        initialized = true;
        return true;
    }

    // Configuration Cache Reset
    public void reset() {
        webhookUrls = null;
        channelIds = null;
        skinsUrl = null;
        useAvatar = false;
        initialized = false;
    }

}
