package com.visualfiredev.truediscordlink.listeners;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.List;

public class DiscordChatListener implements MessageCreateListener {

    // Instance Variables
    private final TrueDiscordLink discordLink;
    private List<Long> channelIds;
    private boolean initialized;

    // Constructor
    public DiscordChatListener(TrueDiscordLink discordLink) {
        this.discordLink = discordLink;
    }

    // Event Handler
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        // Exclude Non-Server Messages, System Messages, Bot Messages, & Webhook Messages
        if (!event.isServerMessage() || event.getMessageAuthor() == null || event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()) {
            return;
        }

        // Load Configuration
        if (!initialized && !initialize()) {
            return;
        }

        // Bot
        if (channelIds != null && channelIds.size() > 0) {

            // Alert Users who might have been tagged
            String content = event.getMessageContent();
            if (discordLink.getConfig().getBoolean("tagging.mention_minecraft_users")) {
                for (Player player : discordLink.getServer().getOnlinePlayers()) {
                    if (content.contains(player.getName()) || content.contains(player.getDisplayName())) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 2, 1);
                        content = content.replace(player.getName(), "&a" + player.getName() + "&r");
                        content = content.replace(player.getDisplayName(), "&a" + player.getDisplayName() + "&r");
                    }
                }
            }

            // Check for Channel & Send Message
            int index = channelIds.indexOf(event.getChannel().getId());
            if (index != -1) {
                if (event.getMessageAttachments().size() > 0) {
                    // Fetch Attachment Format & Get URL from FIRST & FIRST ONLY attachment
                    String receiveAttachmentFormat = ChatColor.translateAlternateColorCodes('&',
                            discordLink.getLangString("messages.receive_attachment_format",
                                    new String[]{"%username", event.getMessageAuthor().getName()},
                                    new String[]{"%nickname", event.getMessageAuthor().getDisplayName()},
                                    new String[]{"%discriminator", event.getMessageAuthor().getDiscriminator().toString()},
                                    new String[]{"%id", event.getMessageAuthor().getIdAsString()}
                            )
                    );
                    String attachmentUrl = event.getMessageAttachments().get(0).getProxyUrl().toString();
                    String messageContent;
                    if (event.getMessageContent().isEmpty()) {
                        messageContent = ChatColor.translateAlternateColorCodes('&', discordLink.getLangString("messages.receive_attachment_placeholder"));
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
                    discordLink.getServer().spigot().broadcast(txtMessage);
                } else {
                    discordLink.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        discordLink.getLangString("messages.receive_format",
                                new String[]{"%username", event.getMessageAuthor().getName()},
                                new String[]{"%nickname", event.getMessageAuthor().getDisplayName()},
                                new String[]{"%discriminator", event.getMessageAuthor().getDiscriminator().toString()},
                                new String[]{"%id", event.getMessageAuthor().getIdAsString()},
                                new String[]{"%message", content }
                        )
                    ));
                }
            }

        }
    }

    // Configuration Cache Initialization
    private boolean initialize() {
        FileConfiguration config = discordLink.getConfig();

        // Bot Configuration
        if (config.getBoolean("bot.enabled")) {
            channelIds = config.getLongList("bot.receive_channels");
        }

        initialized = true;
        return true;
    }

    // Configuration Cache Reset
    public void reset() {
        channelIds = null;
        initialized = false;
    }

}
