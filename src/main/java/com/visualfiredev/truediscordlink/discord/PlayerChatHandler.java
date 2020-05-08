package com.visualfiredev.truediscordlink.discord;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PlayerChatHandler implements Listener {

    // Instance Variables
    private TrueDiscordLink discordlink;
    private List<String> webhooks;
    private String skinsUrl;

    // Constructor
    public PlayerChatHandler() {
        this.discordlink = TrueDiscordLink.getInstance();
    }

    // Event Handler for Player Chat
    @EventHandler
    public void playerChat(AsyncPlayerChatEvent event) {
        // Check if Chat Relay is Enabled
        if (!discordlink.getConfig().getBoolean("messaging.enable_chat_relay")) {
            return;
        }

        // The player & its username
        Player player = event.getPlayer();
        String username = player.getName();

        // Check if Webhooks Are Enabled & Do That
        if (discordlink.getConfig().getBoolean("messaging.use_webhooks")) {
            // Load the required URLs for webhooks
            if ((webhooks == null || skinsUrl == null) && !loadUrls()) {
                return;
            }

            // Create Player Skin
            String skin = skinsUrl.replace("%uuid", player.getUniqueId().toString());

            // Send Web Request
            for (String webhook : webhooks) {
                sendWebhookMessage(event.getMessage(), username, skin, webhook);
            }
        }
    }

    // Sends a message via a webhook
    private void sendWebhookMessage(String content, String username, String skin, String webhook) {
        try {
            // Make Connection
            HttpsURLConnection connection = (HttpsURLConnection) new URL(webhook).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Make JSON Data
            byte[] data = ("{\"content\": \"" + content.replace("\"", "\\\"") + "\", \"username\": \"" + username + "\", \"avatar_url\": \"" + skin + "\"}").getBytes();
            int length = data.length;

            // Add Data & Make Request
            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
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

    // Parses the URLs from Configuration
    // Returns whether or not it worked
    public boolean loadUrls() {
        // Load Skins URL
        skinsUrl = discordlink.getConfig().getString("messaging.skins_url");
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

        // Load Webhooks
        webhooks = discordlink.getConfig().getStringList("messaging.relay_webhooks");
        if (webhooks.size() == 0 && discordlink.getConfig().getBoolean("messaging.use_webhooks")) {
            (new InvalidConfigurationException("There are no webhooks but use_webhooks is enabled!")).printStackTrace();
            return false;
        } else {
            for (String hook : webhooks) {
                try {
                    new URL(hook);
                } catch (MalformedURLException e) {
                    (new MalformedURLException("Invalid Webhook! " + hook)).printStackTrace();
                    webhooks = null;
                    return false;
                }
            }
        }

        // Everything looks good! :)
        return true;
    }

    // Resets all of the nessecary information for a configuration reload
    public void reset() {
        webhooks = null;
        skinsUrl = null;
    }

}
