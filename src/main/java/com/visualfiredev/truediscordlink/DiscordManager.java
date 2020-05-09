package com.visualfiredev.truediscordlink;

import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.Message;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DiscordManager {

    // Instance Variables
    private final TrueDiscordLink discordlink;
    private List<String> webhookUrls;
    private List<Long> channelIds;
    private String skinsUrl;
    private boolean useAvatar;
    private boolean initialized = false;

    // Constructor
    public DiscordManager(TrueDiscordLink discordLink) {
        this.discordlink = discordLink;
    }

    // Send message function
    public void sendDiscordMessage(String content, boolean blocking, Player player) {
        // Load Configuration
        if (!initialized && !initialize()) {
            return;
        }

        // Webhooks
        if (webhookUrls != null) {

            // Prepare Skin
            String skin = null;
            if (useAvatar && player != null) {
                skin = skinsUrl.replace("%uuid", player.getUniqueId().toString());
            }

            // Send Message
            for (String webhookUrl : webhookUrls) {
                if (player != null) {
                    sendWebhookMessage(webhookUrl, ChatColor.translateAlternateColorCodes('&',
                            discordlink.getLangString("messages.webhook_relay_format",
                                    new String[] { "%name", player.getName() },
                                    new String[] { "%displayname", player.getDisplayName() },
                                    new String[] { "%uuid", player.getUniqueId().toString() },
                                    new String[] { "%message", content }
                            )
                    ), player.getName(), skin);
                } else {
                    sendWebhookMessage(webhookUrl, ChatColor.translateAlternateColorCodes('&', content));
                }
            }

        }

        // Bot
        if (channelIds != null && channelIds.size() > 0 && discordlink.getDiscord() != null) {

            // Find Channel & Send Message
            for (long channelId : channelIds) {
                discordlink.getDiscord().getTextChannelById(channelId).ifPresent(channel -> {
                    if (player != null) {
                        CompletableFuture<Message> future = channel.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                discordlink.getLangString("messages.bot_relay_format",
                                        new String[] { "%name", player.getName() },
                                        new String[] { "%displayname", player.getDisplayName() },
                                        new String[] { "%uuid", player.getUniqueId().toString() },
                                        new String[] { "%message", content }
                                )
                        ));
                        if (blocking) {
                            future.join();
                        }
                    } else {
                        CompletableFuture<Message> future = channel.sendMessage(ChatColor.translateAlternateColorCodes('&', content));
                        if (blocking) {
                            future.join();
                        }
                    }
                });
            }

        }

    }
    public void sendDiscordMessage(String content, Player player) {
        this.sendDiscordMessage(content, false, player);
    }
    public void sendDiscordMessage(String content, boolean blocking) {
        this.sendDiscordMessage(content, blocking, null);
    }
    public void sendDiscordMessage(String content) {
        this.sendDiscordMessage(content, false, null);
    }

    // Sends a message via a webhook
    private void sendWebhookMessage(String webhookUrl, String content, String username, String skin) {
        try {
            // Make Connection
            HttpsURLConnection connection = (HttpsURLConnection) new URL(webhookUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Make JSON Data
            JsonObject object = new JsonObject();
            object.addProperty("content", content);
            if (username != null) {
                object.addProperty("username", username);
            }
            if (skin != null) {
                object.addProperty("avatar_url", skin);
            }
            byte[] data = object.toString().getBytes(StandardCharsets.ISO_8859_1);
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
    private void sendWebhookMessage(String webhookUrl, String content, String username) {
        this.sendWebhookMessage(webhookUrl, content, username, null);
    }
    private void sendWebhookMessage(String webhookUrl, String content) {
        this.sendWebhookMessage(webhookUrl, content, null, null);
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
