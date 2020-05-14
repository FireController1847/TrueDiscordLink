package com.visualfiredev.truediscordlink;

import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordManager {

    // Constants
    private final Pattern mentionRegex = Pattern.compile("@[\\p{Alnum}\\p{Punct}]*");

    // Instance Variables
    private final TrueDiscordLink discordlink;
    private boolean webhooksEnabled;
    private boolean botEnabled;
    private List<String> webhookUrls;
    private List<Long> channelIds;
    private List<Long> mentionServersIds;
    private String skinsUrl;
    private boolean enableTagging;
    private boolean enableEveryoneTagging;
    private boolean enableRoleTagging;
    private boolean mentionDiscordUsers;
    private boolean useAvatar;
    private boolean initialized = false;

    // Constructor
    public DiscordManager(TrueDiscordLink discordLink) {
        this.discordlink = discordLink;
    }

    // Send message function
    public ArrayList<String[]> sendDiscordMessage(String rawContent, boolean blocking, Player player) {
        // Load Configuration
        if (!initialized && !initialize()) {
            return null;
        }

        // Make Atomic Reference
        final AtomicReference<String> content = new AtomicReference<>(rawContent); // The content that will be modified
        final AtomicReference<ArrayList<String[]>> modifications  = new AtomicReference<>(new ArrayList<>()); // Modifications to make to the original chat message

        // Check for Tags
        if (!enableTagging) {
            content.set(content.get().replace("@", "@ "));
        } else {
            if (!enableEveryoneTagging) {
                content.set(content.get().replace("@here", "@ here").replace("@everyone", "@ everyone"));
            }
            if (!enableRoleTagging) {
                content.set(content.get().replace("@&", "@& "));
            }
        }

        // Check for Mentions
        if (mentionDiscordUsers && botEnabled) {

            // Check Matches
            List<String> matches = new ArrayList<String>();
            Matcher matcher = mentionRegex.matcher(content.get());
            while (matcher.find()) {
                matches.add(matcher.group());
            }

            // Manage Matches
            for (String match : matches) {
                String username = match.substring(1).toLowerCase();

                System.out.println("MATCH: " + match);
                System.out.println("USERNAME: " + username);

                // Loop Through Each Server
                for (Long serverId : mentionServersIds) {
                    discordlink.getDiscord().getServerById(serverId).ifPresent(server -> {
                        // Search for User in Members
                        Collection<User> users = server.getMembers();
                        for (User user : users) {
                            String name = user.getName();
                            String nickname = user.getNickname(server).orElse(null);

                            System.out.println("NAME: " + name);
                            System.out.println("NICKNAME: " + nickname);

                            // Check for Exact Match
                            boolean isMatch = false;
                            if (username.equalsIgnoreCase(name) || (nickname != null && username.equalsIgnoreCase(nickname))) {
                                isMatch = true;

                            // Check for Partial Match (min length of 3)
                            } else if (username.length() > 3 && (name.toLowerCase().startsWith(username) || (nickname != null && nickname.toLowerCase().startsWith(username)))) {
                                isMatch = true;
                            }

                            // Replace First Occurance
                            if (isMatch) {
                                System.out.println("IS MATCH!");
                                content.set(content.get().replace(match, "<@" + user.getId() + ">"));
                                modifications.get().add(new String[] { match, "&a@" + (nickname != null ? nickname : name) + "&r" });
                                break;
                            }
                        }
                    });
                }
            }
        }

        // Webhooks
        if (webhooksEnabled) {

            // Prepare Skin
            String skin = null;
            if (useAvatar && player != null) {
                skin = skinsUrl.replace("%uuid", player.getUniqueId().toString());
            }

            // Send Message
            for (String webhookUrl : webhookUrls) {
                if (player != null) {
                    sendWebhookMessage(webhookUrl, discordlink.getLangString("messages.webhook_relay_format",
                        new String[] { "%name", player.getName() },
                        new String[] { "%displayname", player.getDisplayName() },
                        new String[] { "%uuid", player.getUniqueId().toString() },
                        new String[] { "%message", content.get() }
                    ), player.getName(), skin);
                } else {
                    sendWebhookMessage(webhookUrl, content.get());
                }
            }

        }

        // Bot
        if (botEnabled) {

            // Find Channel & Send Message
            for (long channelId : channelIds) {
                discordlink.getDiscord().getTextChannelById(channelId).ifPresent(channel -> {
                    if (player != null) {
                        CompletableFuture<Message> future = channel.sendMessage(
                            discordlink.getLangString("messages.bot_relay_format",
                                new String[] { "%name", player.getName() },
                                new String[] { "%displayname", player.getDisplayName() },
                                new String[] { "%uuid", player.getUniqueId().toString() },
                                new String[] { "%message", content.get() }
                            )
                        );
                        if (blocking) {
                            future.join();
                        }
                    } else {
                        CompletableFuture<Message> future = channel.sendMessage(ChatColor.translateAlternateColorCodes('&', content.get()));
                        if (blocking) {
                            future.join();
                        }
                    }
                });
            }

        }

        return modifications.get();
    }
    public ArrayList<String[]> sendDiscordMessage(String content, Player player) {
        return this.sendDiscordMessage(content, false, player);
    }
    public ArrayList<String[]> sendDiscordMessage(String content, boolean blocking) {
        return this.sendDiscordMessage(content, blocking);
    }
    public ArrayList<String[]> sendDiscordMessage(String content) {
        return this.sendDiscordMessage(content, false);
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
            webhooksEnabled = true;

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
            botEnabled = true;

            // Get Relay Channels
            channelIds = config.getLongList("bot.relay_channels");

            // Get Tagging Booleans
            enableTagging = config.getBoolean("tagging.enable_tagging");
            enableEveryoneTagging = config.getBoolean("tagging.enable_everyone_tagging");
            enableRoleTagging = config.getBoolean("tagging.enable_role_tagging");

            // Get Mention Servers
            mentionDiscordUsers = config.getBoolean("tagging.mention_discord_users");
            mentionServersIds = config.getLongList("tagging.mention_servers");
            if (!config.getBoolean("tagging.mention_discord_users") && mentionServersIds.size() == 0) {
                (new InvalidConfigurationException("Invalid amount of mention servers!")).printStackTrace();
                return false;
            }
        }

        initialized = true;
        return true;
    }

    // Configuration Cache Reset
    public void reset() {
        webhooksEnabled = false;
        botEnabled = false;
        webhookUrls = null;
        channelIds = null;
        mentionServersIds = null;
        skinsUrl = null;
        enableTagging = false;
        enableEveryoneTagging = false;
        enableRoleTagging = false;
        mentionDiscordUsers = false;
        useAvatar = false;
        initialized = false;
    }

}
