package com.firecontroller1847.truediscordlink.listeners.minecraft;

import com.firecontroller1847.truediscordlink.DiscordManager;
import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;

public class CommandListener implements Listener {

    // Variables
    private TrueDiscordLink discordlink;

    // Constructor
    public CommandListener(TrueDiscordLink discordlink) {
        this.discordlink = discordlink;
    }

    // Player Commands
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        this.onCommand(event.getMessage().substring(1), event.getPlayer());
    }

    // Non-Player Commands
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        this.onCommand(event.getCommand(), event.getSender());
    }

    // Event
    public void onCommand(String command, CommandSender sender) {
        // Relay Tellraw Messages
        if (discordlink.getConfig().getBoolean("events.relay_tellraw_messages") && command.startsWith("tellraw")) {

            // Check for communication method
            DiscordManager discordManager = discordlink.getDiscordManager();
            if (discordManager.canCommunicateWithDiscord()) {
                String[] args = command.split(" ");
                if (args.length >= 3) {
                    String targets = args[1];
                    String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (targets.contains("@a")) {
                        try {
                            MutableComponent components = Component.Serializer.fromJson(message);
                            String stringifiedTellraw = components.getString();
                            discordManager.sendDiscordMessage(stringifiedTellraw);
                        } catch (Exception e) {
                            // ...
                        }
                    }
                }
            }

        }
    }

}
