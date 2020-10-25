package com.visualfiredev.truediscordlink;

import com.google.gson.JsonObject;
import com.vdurmont.emoji.EmojiParser;
import com.visualfiredev.truediscordlink.commands.CommandUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordManager {

    // Constants
    private static final Pattern MentionRegex = Pattern.compile("@[\\p{Alnum}\\p{Punct}]*");

    // Instance Variables
    private final TrueDiscordLink discordlink;
    protected Thread statusLoopThread;
    protected Thread channelTopicLoopThread;

    // Constructor
    public DiscordManager(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    // Sends a message to the Minecraft server
    public void sendMinecraftMessage(Message message, boolean edit) {
        // Check if Bot Is Enabled
        List<Long> channelIds = discordlink.getConfig().getLongList("bot.receive_channels");
        if (channelIds.size() > 0) {

            // Filter colors
            String rawContent = message.getReadableContent();
            if (!discordlink.getConfig().getBoolean("bot.allow_colors")) {
                rawContent = TrueDiscordLink.stripHexCodes(rawContent);
            }
            String content = EmojiParser.parseToAliases(rawContent);

            // Alert Users who might have been tagged & append asterisk for edits
            if (discordlink.getConfig().getBoolean("tagging.mention_minecraft_users")) {
                for (Player player : discordlink.getServer().getOnlinePlayers()) {
                    if (content.contains(player.getName()) || content.contains(player.getDisplayName())) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 2, 1);
                        content = content.replace(player.getName(), "§a" + player.getName() + "§r");
                        content = content.replace(player.getDisplayName(), "§a" + player.getDisplayName() + "§r");
                    }
                }
            }
            if (edit) {
                content = content + "*";
            }

            // Check for Channel & Send Message
            int index = channelIds.indexOf(message.getChannel().getId());
            if (index != -1) {
                if (message.getAttachments().size() > 0) {
                    // Fetch Attachment Format & Get URL from FIRST & FIRST ONLY attachment
                    String receiveAttachmentFormat = discordlink.getLangString("messages.receive_attachment_format", false,
                            new String[]{"%username", message.getAuthor().getName()},
                            new String[]{"%nickname", message.getAuthor().getDisplayName()},
                            new String[]{"%discriminator", message.getAuthor().getDiscriminator().toString()},
                            new String[]{"%id", message.getAuthor().getIdAsString()}
                    );
                    String attachmentUrl = message.getAttachments().get(0).getProxyUrl().toString();
                    String messageContent;
                    if (message.getContent().isEmpty()) {
                        messageContent = discordlink.getLangString("messages.receive_attachment_placeholder", false);
                    } else {
                        messageContent = content;
                    }

                    // Split At Message
                    int messageIndex = receiveAttachmentFormat.indexOf("%message");
                    String part1 = receiveAttachmentFormat.substring(0, messageIndex);
                    String part2 = receiveAttachmentFormat.substring(messageIndex + "message".length() + 1);

                    // Make Part 1 Component
                    TextComponent txtMessage = new TextComponent(part1);

                    // Make Content Clickable & Hoverable
                    TextComponent txtContent = new TextComponent(messageContent);
                    txtContent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, attachmentUrl));

                    // Make Hover Clickable & Colored
                    TextComponent txtHover = new TextComponent(attachmentUrl);
                    txtHover.setItalic(true);
                    txtHover.setColor(net.md_5.bungee.api.ChatColor.BLUE);
                    txtContent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(txtHover).create()));

                    // Make Part 2 Component
                    TextComponent txtFurther = new TextComponent(part2);

                    // Add Components
                    txtMessage.addExtra(txtContent);
                    txtMessage.addExtra(txtFurther);

                    // Send Message
                    discordlink.getServer().spigot().broadcast(txtMessage);
                } else {
                    discordlink.getServer().broadcastMessage(discordlink.getLangString("messages.receive_format", true,
                            new String[]{"%username", message.getAuthor().getName()},
                            new String[]{"%nickname", message.getAuthor().getDisplayName()},
                            new String[]{"%discriminator", message.getAuthor().getDiscriminator().toString()},
                            new String[]{"%id", message.getAuthor().getIdAsString()},
                            new String[]{"%message", content }
                    ));
                }
            }

        }
    }
    public void sendMinecraftMessage(Message message) {
        sendMinecraftMessage(message, false);
    }

    // Sends a message to the Discord server
    public ArrayList<String[]> sendDiscordMessage(String content, boolean blocking, Player player) {
        // Create Message Modifications
        AtomicReference<String> atomicContent = new AtomicReference<>(content);
        AtomicReference<ArrayList<String[]>> atomicModifications = new AtomicReference<>(new ArrayList<>());

        // Run Checks
        this.modifyRemoveTags(atomicContent, atomicModifications);
        this.modifyCheckMentions(atomicContent, atomicModifications, player);

        // Send messages
        sendBotMessage(atomicContent.get(), blocking, player);
        sendWebhookMessage(atomicContent.get(), player); // Webhooks are always blocking because of HTTP requests... Is this able to be changed?

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

    // Sends a message to the Discord server via a bot
    private void sendBotMessage(final String content, boolean blocking, Player player) {
        if (discordlink.getConfig().getBoolean("bot.enabled")) {

            // Find Channel & Send Message
            for (long channelId : discordlink.getConfig().getLongList("bot.relay_channels")) {
                discordlink.getDiscord().getTextChannelById(channelId).ifPresent(channel -> {
                    if (player != null) {
                        CompletableFuture<Message> future = channel.sendMessage(discordlink.getLangString("messages.bot_relay_format", true,
                            new String[] { "%name", player.getName() },
                            new String[] { "%displayname", player.getDisplayName() },
                            new String[] { "%uuid", player.getUniqueId().toString() },
                            new String[] { "%message", content }
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
    }

    // Sends a message to the Discord server via a webhook
    private void sendWebhookMessage(final String content, Player player) {
        if (discordlink.getConfig().getBoolean("webhooks.enabled")) {

            // Build Skin URL
            String skin = null;
            if (discordlink.getConfig().getBoolean("webhooks.use_avatar") && player != null) {
                skin = Objects.requireNonNull(discordlink.getConfig().getString("webhooks.skins_url"))
                        .replace("%uuid", player.getUniqueId().toString())
                        .replace("%name", player.getName());
            }

            // Send Message
            for (String url : discordlink.getConfig().getStringList("webhooks.urls")) {
                if (player != null) {
                    this.makeWebhookRequest(url, discordlink.getLangString("messages.webhook_relay_format", false,
                        new String[] { "%name", player.getName() },
                        new String[] { "%displayname", player.getDisplayName() },
                        new String[] { "%uuid", player.getUniqueId().toString() },
                        new String[] { "%message", content }
                    ), player.getName(), skin);
                } else {
                    this.makeWebhookRequest(url, content);
                }
            }

        }
    }

    // Makes a request to a webhook
    private void makeWebhookRequest(String webhookUrl, String content, String username, String skin) {
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
    private void makeWebhookRequest(String webhookUrl, String content, String username) {
        this.makeWebhookRequest(webhookUrl, content, username, null);
    }
    private void makeWebhookRequest(String webhookUrl, String content) {
        this.makeWebhookRequest(webhookUrl, content, null, null);
    }

    // Message checks
    private void modifyRemoveTags(AtomicReference<String> content, AtomicReference<ArrayList<String[]>> modifications) {
        if (!discordlink.getConfig().getBoolean("tagging.enable_tagging")) {
            content.set(content.get().replace("@", "@ "));
        } else {
            if (!discordlink.getConfig().getBoolean("tagging.enable_everyone_tagging")) {
                content.set(content.get().replace("@here", "@ here").replace("@everyone", "@ everyone"));
            }
            if (!discordlink.getConfig().getBoolean("tagging.enable_role_tagging")) {
                content.set(content.get().replace("@&", "@& "));
            }
        }
    }
    private void modifyCheckMentions(AtomicReference<String> content, AtomicReference<ArrayList<String[]>> modifications, Player player) {
        // Check for Mentions
        if (discordlink.getConfig().getBoolean("tagging.mention_discord_users")) {
            // Check for User Permission
            if (player != null && !CommandUtil.hasPermission(player, "truediscordlink.tagging")) {
                return;
            }

            // Check for Matches
            List<String> matches = new ArrayList<String>();
            Matcher matcher = MentionRegex.matcher(content.get());
            while (matcher.find()) {
                matches.add(matcher.group());
            }

            // Loop Through Matches
            for (String match : matches) {
                String username = match.substring(1);
                if (username.isEmpty()) {
                    continue;
                }

                // Loop Through Each Discord Server
                for (Long serverId : discordlink.getConfig().getLongList("tagging.mention_servers")) {
                    discordlink.getDiscord().getServerById(serverId).ifPresent(server -> {

                        // Search for User in Members
                        Collection<User> users = server.getMembers();
                        for (User user : users) {
                            String name = user.getName();
                            String nickname = user.getNickname(server).orElse(null);

                            // Check for Exact Match
                            boolean isMatch = false;
                            if (username.equalsIgnoreCase(name) || username.equalsIgnoreCase(nickname)) {
                                isMatch = true;

                            // Check for Partial Match (min len 3)
                            } else if (username.length() > 3 && (name.toLowerCase().startsWith(username.toLowerCase()) || (nickname != null && nickname.toLowerCase().startsWith(username.toLowerCase())))) {
                                isMatch = true;
                            }

                            // Replace First Occurance
                            if (isMatch) {
                                content.set(content.get().replace(match, "<@" + user.getId() + ">"));
                                modifications.get().add(new String[] {
                                    match,
                                    discordlink.getLangString("tagging.minecraft_mention_color",
                                        new String[] { "%name", (nickname != null ? nickname : name) }
                                    )
                                });
                                break;
                            }
                        }

                    });
                }
            }
        }
    }

    // Status Loop
    public void statusLoop(final int position) {
        if (statusLoopThread != null && Thread.currentThread() != statusLoopThread && statusLoopThread.isAlive()) {
            statusLoopThread.interrupt();
        }

        statusLoopThread = new Thread(() -> {
            AtomicInteger newPosition = new AtomicInteger(position);

            // Fetch Activity
            List<String> activities = discordlink.getConfig().getStringList("bot.discord.activities");
            if (activities.size() <= 0 || discordlink.getDiscord() == null) {
                return;
            }
            if (newPosition.get() > activities.size() - 1) {
                newPosition.set(0);
            }

            // Set Activity
            String activity = activities.get(newPosition.get());
            discordlink.getDiscord().updateActivity(ActivityType.PLAYING, activity);

            // Begin Next Loop
            int time = discordlink.getConfig().getInt("bot.discord.activity_cycle_speed");
            if (time < 15) {
                time = 15;
            }

            // Sleep for Time & Return if Interrupted
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                return;
            }

            // Call Next Loop
            statusLoop(newPosition.incrementAndGet());
        });
        statusLoopThread.start();
    }

    // Channel Topic Loop
    public void channelTopicLoop() {
        if (channelTopicLoopThread != null && Thread.currentThread() != channelTopicLoopThread && channelTopicLoopThread.isAlive()) {
            channelTopicLoopThread.interrupt();
        }

        channelTopicLoopThread = new Thread(() -> {
            // Fetch Auto-Channel-Topic-Message & Auto-Channel-Topic-Ids
            List<Long> channelIds = discordlink.getConfig().getLongList("bot.discord.auto_channel_topic_ids");
            if (channelIds.size() <= 0) {
                return;
            }
            AtomicReference<String> message = new AtomicReference<>(discordlink.getConfig().getString("bot.discord.auto_channel_topic_message"));

            // Find Channels
            for (long channelId : channelIds) {
                discordlink.getDiscord().getServerTextChannelById(channelId).ifPresent(channel -> {
                    // Handle Playerholder API Support
                    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                        message.set(PlaceholderAPI.setPlaceholders(null, message.get()));
                        message.set(ChatColor.stripColor(message.get()));
                    }

                    // Update Topics
                    channel.updateTopic(message.get());
                });
            }

            // Begin Next Loop
            int time = discordlink.getConfig().getInt("bot.discord.auto_channel_topic_update_rate");
            if (time == -1) {
                return;
            } else if (time < 300) {
                time = 300;
            }

            // Sleep for Time & Return if Interrupted
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                return;
            }

            // Call Next Loop
            channelTopicLoop();
        });
        channelTopicLoopThread.start();
    }

}