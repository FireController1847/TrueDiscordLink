package com.firecontroller1847.truediscordlink;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vdurmont.emoji.EmojiParser;
import com.firecontroller1847.truediscordlink.listeners.discord.DiscordChatListener;
import com.firecontroller1847.truediscordlink.listeners.discord.DiscordEditListener;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordManager {

    // Constants
    private static final Pattern DISCORD_MENTION_REGEX = Pattern.compile("@[\\p{Alnum}\\p{Punct}]*");

    // Variables
    private TrueDiscordLink discordlink;
    private DiscordApi api;
    private Thread activityLoopThread;
    private Thread channelTopicLoopThread;

    // Constructor
    public DiscordManager(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;

        // If bot is enabled, login
        if (discordlink.getConfig().getBoolean("bot.enabled")) {
            // Log Logging In...
            discordlink.getLogger().info("Logging in to Discord...");

            // Login
            if (discordlink.getConfig().getBoolean("bot.enabled")) {
                new DiscordApiBuilder()
                        .setToken(discordlink.getConfig().getString("bot.token"))
                        .setIntents(Intent.GUILDS, Intent.GUILD_MESSAGES, Intent.GUILD_MEMBERS, Intent.DIRECT_MESSAGES)
                        .login().thenAcceptAsync(api -> {
                    this.api = api;

                    // Register Events
                    api.addListener(new DiscordChatListener(discordlink, this));
                    api.addListener(new DiscordEditListener(discordlink, this));

                    // Log Logged In
                    discordlink.getLogger().info("Logged in to Discord!");

                    // Send Startup Message
                    sendStartupMessage();

                    // Activity Loop
                    int activityCycleSpeed = discordlink.getConfig().getInt("bot.discord.activity_cycle_speed");
                    if (activityCycleSpeed < 15) {
                        activityCycleSpeed = 15;
                    }
                    discordlink.addLoop("activity_loop", activityCycleSpeed * 1000, this::activityLoop, new AtomicInteger(0));

                    // Channel Topic Loop
                    int autoTopicUpdateSpeed = discordlink.getConfig().getInt("bot.discord.auto_channel_topic_update_rate");
                    if (autoTopicUpdateSpeed != -1) {
                        if (autoTopicUpdateSpeed < 300) {
                            autoTopicUpdateSpeed = 300;
                        }
                        discordlink.addLoop("channel_topic_loop", autoTopicUpdateSpeed * 1000, this::channelTopicLoop);
                    }

                    channelTopicLoop();
                }).exceptionally(ExceptionLogger.get());
            } else {
                // Send Startup Message (via webhook)
                sendStartupMessage();
            }
        }
    }

    // Sends the startup message
    public void sendStartupMessage() {
        if (discordlink.getConfig().getBoolean("events.server_start")) {
            this.sendDiscordMessage(discordlink.getTranslation("events.server_start"));
        }
    }

    // Sends the shutdown message
    public void sendShutdownMessage() {
        if (discordlink.getConfig().getBoolean("events.server_shutdown")) {
            this.sendDiscordMessage(discordlink.getTranslation("events.server_shutdown"), true);
        }
    }

    // Shuts down all looping threads and the bot
    public void shutdown() {
        // Send shutdown message
        sendShutdownMessage();

        // Disconnect bot
        if (api != null) {
            // Announce disconnecting
            discordlink.getLogger().info("Disconnecting from Discord...");

            // See issue https://github.com/Javacord/Javacord/issues/598 for why we wait
            CountDownLatch shutdownWaiter = new CountDownLatch(1);
            api.addLostConnectionListener(event -> shutdownWaiter.countDown());
            api.disconnect();
            try {
                shutdownWaiter.await(2, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Announce disconnected!
            discordlink.getLogger().info("Disconnected from Discord!");
        }
    }

    // Sends a message to the Minecraft server
    public void sendMinecraftMessage(Message message, boolean edit) {
        // Gets the list of channels that are listened to and ensure this message is a part of them.
        List<Long> channelIds = discordlink.getConfig().getLongList("bot.to_mc_channels");
        if (channelIds.size() == 0) {
            (new InvalidConfigurationException("The bot is enabled and chat relay is enabled but there are no to_mc_channels!")).printStackTrace();
            return;
        }
        if (!channelIds.contains(message.getChannel().getId())) {
            return;
        }

        // Unparse emojis
        String content = message.getReadableContent();
        content = EmojiParser.parseToAliases(content);

        // Filter colors
        if (discordlink.getConfig().getBoolean("bot.allow_colors")) {
            content = TrueDiscordLink.translateColorCodes(TrueDiscordLink.translateColorCodes('&', content));
        } else {
            content = TrueDiscordLink.stripColorCodes(content);
        }

        // Add a star to edited messages
        if (edit) {
            content = content + "*";
        }

        // Alert Users who might have been tagged
        if (discordlink.getConfig().getBoolean("tagging.mention_minecraft_users")) {
            for (Player player : discordlink.getServer().getOnlinePlayers()) {
                String contentLower = content.toLowerCase();
                String playerNameLower = player.getName().toLowerCase();
                String playerDisplayNameLower = player.getDisplayName().toLowerCase();
                if (contentLower.contains(playerNameLower) || contentLower.contains(playerDisplayNameLower)) {
                    // Parse sound from config
                    Sound sound;
                    try {
                        sound = Sound.valueOf(discordlink.getConfig().getString("tagging.mention_minecraft_noise"));
                    } catch (IllegalArgumentException e) {
                        (new IllegalArgumentException("Invalid sound! Using default ENTITY_EXPERIENCE_ORB_PICKUP", e)).printStackTrace();
                        sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                    }

                    // Play sound
                    player.playSound(player.getLocation(), sound, SoundCategory.NEUTRAL, 2, 1);

                    // Content
                    content = content
                        .replaceAll("(?i)" + playerNameLower, "§a" + player.getName() + "§r")
                        .replaceAll("(?i)" + playerDisplayNameLower, "§a" + player.getDisplayName() + "§r");
                }
            }
        }

        if (message.getAttachments().size() > 0) {
            TextComponent text = buildAttachmentTextComponent(message, content);
            discordlink.getServer().spigot().broadcast(text);
            discordlink.getServer().getConsoleSender().spigot().sendMessage(text);
        } else {
            String text = discordlink.getTranslation("messages.to_mc_format", true,
                new String[] { "%name%", message.getAuthor().getName() },
                new String[] { "%nickname%", message.getAuthor().getDisplayName() },
                new String[] { "%discriminator%", message.getAuthor().getDiscriminator().toString() },
                new String[] { "%id%", message.getAuthor().getIdAsString() }
            );
            text = text.replace("%message%", content); // Replace content after translation to prevent parsing color codes
            discordlink.getServer().broadcastMessage(text);
        }
    }
    public void sendMinecraftMessage(Message message) {
        sendMinecraftMessage(message, false);
    }

    // Builds an attachment link from a message and its content
    public TextComponent buildAttachmentTextComponent(Message message, String content) {
        // Handle attachment format
        String attachmentFormat = discordlink.getTranslation("messages.to_mc_attachment_format", true,
                new String[] { "%name%", message.getAuthor().getName() },
                new String[] { "%nickname%", message.getAuthor().getDisplayName() },
                new String[] { "%discriminator%", message.getAuthor().getDiscriminator().toString() },
                new String[] { "%id%", message.getAuthor().getIdAsString() }
        );

        // Get URL
        String url = message.getAttachments().get(0).getProxyUrl().toString();

        // Use placeholder if content is empty
        if (content.isEmpty()) {
            content = discordlink.getTranslation("messages.to_mc_attachment_placeholder", true);
        }

        // Make sure only the %message% is clickable by splitting the message into three parts: pre-message, message, post-message
        int messageIndex = attachmentFormat.indexOf("%message%");
        String preMessage = attachmentFormat.substring(0, messageIndex);
        String postMessage = attachmentFormat.substring(messageIndex + 9); // "message%" length plus one

        // Make Pre-Message Component
        TextComponent preMessageComponent = new TextComponent(preMessage);

        // Make Message Component
        TextComponent messageComponent = new TextComponent(content);

        // Make Content Clickable
        messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        // Add Color & Make Hover Show URL
        TextComponent messageComponentHover = new TextComponent(url);
        messageComponentHover.setItalic(true);
        messageComponentHover.setColor(ChatColor.BLUE);
        messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(messageComponentHover).create())));

        // Make Post-Message Component
        TextComponent postMessageComponent = new TextComponent(postMessage);

        // Combine Components
        preMessageComponent.addExtra(messageComponent);
        preMessageComponent.addExtra(postMessageComponent);

        // Return Final Component
        return preMessageComponent;
    }

    // Sends a message to the Discord server
    public ArrayList<String[]> sendDiscordMessage(String content, boolean blocking, Player player) {
        // Create Message Modifications
        AtomicReference<String> atomicContent = new AtomicReference<>(content);
        AtomicReference<ArrayList<String[]>> atomicModifications = new AtomicReference<>(new ArrayList<>());

        // Run Modifications
        this.modifyAddCheckMentions(atomicContent, atomicModifications, player);

        // Send Messages
        this.sendDiscordBotMessage(atomicContent.get(), blocking, player);
        this.sendDiscordWebhookMessage(atomicContent.get(), player); // Webhooks are always blocking because of HTTP requests... Is this able to be changed?

        // Return Modifications
        return atomicModifications.get();
    }
    public ArrayList<String[]> sendDiscordMessage(String content, Player player) {
        return this.sendDiscordMessage(content, false, player);
    }
    public ArrayList<String[]> sendDiscordMessage(String content, boolean blocking) {
        return this.sendDiscordMessage(content, blocking, null);
    }
    public ArrayList<String[]> sendDiscordMessage(String content) {
        return this.sendDiscordMessage(content, false);
    }

    // Sends a message to the Discord server via the bot
    private void sendDiscordBotMessage(String content, boolean blocking, Player player) {
        if (!isBotConnected()) {
            return;
        }

        // Loop through configured channel ids
        for (long channelId : discordlink.getConfig().getLongList("bot.from_mc_channels")) {
            // Check if channel exists
            api.getTextChannelById(channelId).ifPresent(channel -> {
                // If it exists, send a message dependent on whether or not there's a player
                if (player != null) {
                    CompletableFuture<Message> future = channel.sendMessage(discordlink.getTranslation("messages.from_mc_bot_format", false,
                        new String[] { "%message%", content },
                        new String[] { "%name%" , player.getName() },
                        new String[] { "%displayName%", player.getDisplayName() },
                        new String[] { "%uuid%", player.getUniqueId().toString() }
                    ));
                    if (blocking) {
                        future.join();
                    }
                } else {
                    CompletableFuture<Message> future = channel.sendMessage(content);
                    if (blocking) {
                        future.join();
                    }
                }
            });
        }
    }

    // Sends a message to the Discord server via a webhook
    private void sendDiscordWebhookMessage(String content, Player player) {
        if (!discordlink.getConfig().getBoolean("webhooks.enabled")) {
            return;
        }

        // Build Skin URL
        String skin = null;
        if (discordlink.getConfig().getBoolean("webhooks.use_avatar") && player != null) {
            skin = discordlink.getConfig().getString("webhooks.skins_url");
            if (skin != null) {
                skin = skin.replace("%uuid%", player.getUniqueId().toString());
                skin = skin.replace("%name%",player.getName());
            }
        }

        // Send messages
        for (String url : discordlink.getConfig().getStringList("webhooks.urls")) {
            if (player != null) {
                this.makeWebhookRequest(url, discordlink.getTranslation("messages.from_mc_webhook_format", false,
                    new String[] { "%message%", content },
                    new String[] { "%name%" , player.getName() },
                    new String[] { "%displayName%", player.getDisplayName() },
                    new String[] { "%uuid%", player.getUniqueId().toString() }
                ), player.getDisplayName(), skin);
            } else {
                this.makeWebhookRequest(url, content);
            }
        }
    }

    // Utility method to construct a webhook request
    private void makeWebhookRequest(String url, String content, String username, String skin) {
        try {
            // Make Connection
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
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

            // Mentions
            JsonObject allowedMentions = new JsonObject();
            JsonArray allowedMentionsTypes = new JsonArray();
            if (discordlink.getConfig().getBoolean("tagging.enable_user_tagging")) {
                allowedMentionsTypes.add("users");
            }
            if (discordlink.getConfig().getBoolean("tagging.enable_role_tagging")) {
                allowedMentionsTypes.add("roles");
            }
            if (discordlink.getConfig().getBoolean("tagging.enable_everyone_tagging")) {
                allowedMentionsTypes.add("everyone");
            }
            allowedMentions.add("parse", allowedMentionsTypes);
            object.add("allowed_mentions", allowedMentions);

            // Convert Object to Data
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
    private void makeWebhookRequest(String url, String content, String username) {
        makeWebhookRequest(url, content, username, null);
    }
    private void makeWebhookRequest(String url, String content) {
        makeWebhookRequest(url, content, null, null);
    }

    // Utility method to check for any user shortcut mentions
    private void modifyAddCheckMentions(AtomicReference<String> content, AtomicReference<ArrayList<String[]>> modifications, Player player) {
        // Check if connected
        if (!isBotConnected()) {
            return;
        }

        // Check if Enabled
        if (!discordlink.getConfig().getBoolean("tagging.enable_user_tagging_shortcut")) {
            return;
        }

        // Check for user permission
        if (player != null && !TrueDiscordLink.hasPermission(player, "truediscordlink.tagging")) {
            return;
        }

        // Check for matches
        List<String> matches = new ArrayList<>();
        Matcher matcher = DISCORD_MENTION_REGEX.matcher(content.get());
        while (matcher.find()) {
            matches.add(matcher.group());
        }

        // Loop through matches
        for (String match : matches) {
            // Ensure match does not have blank username
            String username = match.substring(1);
            if (username.isEmpty()) {
                continue;
            }

            // Ensure we have Discord servers
            List<Long> serverIds = discordlink.getConfig().getLongList("tagging.mention_servers");
            if (serverIds.size() == 0) {
                (new InvalidConfigurationException("The bot is enabled and mention parsing is enabled but there are no mention_servers!")).printStackTrace();
                return;
            }

            // Loop through Discord servers
            for (Long serverId : serverIds) {

                // Attempt to find server
                api.getServerById(serverId).ifPresent(server -> {

                    // Get partially matching users
                    ArrayList<User> users = getUsersPartiallyMatching(server, username, true);
                    if (users.size() > 0) {
                        User user = users.get(0);

                        content.set(content.get().replace(match, "<@" + user.getId() + ">"));
                        String translation = discordlink.getTranslation("tagging.minecraft_mention_color", true,
                            new String[] { "%name%", user.getName() },
                            new String[] { "%nickname%", user.getNickname(server).orElse(user.getName()) }
                        );
                        modifications.get().add(
                            new String[] { match, translation }
                        );
                    }
                });
            }
        }
    }

    public static ArrayList<User> getUsersPartiallyMatching(Server server, String partialUsername, boolean firstOnly) {
        Collection<User> users = server.getMembers();
        ArrayList<User> matchingUsers = new ArrayList<>();
        for (User user : users) {
            String name = user.getName();
            String nickname = user.getNickname(server).orElse("");
            boolean added = false;

            // Check for exact match
            if (partialUsername.equalsIgnoreCase(user.getIdAsString()) || partialUsername.equalsIgnoreCase(name) || partialUsername.equalsIgnoreCase(nickname)) {
                matchingUsers.add(user);
                added = true;

            // Check for partial match
            } else if (partialUsername.length() > 3 && (name.toLowerCase().startsWith(partialUsername.toLowerCase()) || nickname.toLowerCase().startsWith(partialUsername.toLowerCase()))) {
                matchingUsers.add(user);
                added = true;

            }

            if (added && firstOnly) {
                break;
            }
        }
        return matchingUsers;
    }

    // The loop to keep the bot's activity up to date
    public void activityLoop(Object parameters) {
        AtomicInteger position = (AtomicInteger) parameters;

        // Fetch list of activities
        List<String> activities = discordlink.getConfig().getStringList("bot.discord.activities");
        if (activities.size() == 0 || api == null) {
            discordlink.removeLoop("activity_loop");
            return;
        }

        // Reset position if we're at the end of the list
        if (position.get() > activities.size() - 1) {
            position.set(0);
        }

        // Get the activity string
        String activity = activities.get(position.get());

        // Handle Placeholder API Support
        if (discordlink.getPlaceholderApi() != null) {
            activity = PlaceholderAPI.setPlaceholders(null, activity);
            activity = TrueDiscordLink.stripColorCodes(activity);
        }

        // Update activity
        api.updateActivity(ActivityType.PLAYING, activity);

        // Increment
        position.incrementAndGet();
    }

    // The loop to update the channel topic
    public void channelTopicLoop() {
        // Fetch list of auto channel topic ids
        List<Long> channelIds = discordlink.getConfig().getLongList("bot.discord.auto_channel_topic_ids");
        if (channelIds.size() <= 0) {
            discordlink.removeLoop("channel_topic_loop");
            return;
        }

        // Prepare message
        AtomicReference<String> message = new AtomicReference<>(discordlink.getConfig().getString("bot.discord.auto_channel_topic_message"));

        // Find channels
        for (long channelId : channelIds) {
            api.getServerTextChannelById(channelId).ifPresent(channel -> {
                // Handle Placeholder API Support
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    message.set(PlaceholderAPI.setPlaceholders(null, message.get()));
                    message.set(TrueDiscordLink.stripColorCodes(message.get()));
                }

                // Update topic
                channel.updateTopic(message.get());
            });
        }
    }

    // Returns true if the bot is enabled and connected, otherwise returns false
    public boolean isBotConnected() {
        return discordlink.getConfig().getBoolean("bot.enabled") && api != null;
    }

    // Getters
    public DiscordApi getApi() {
        return api;
    }

}
