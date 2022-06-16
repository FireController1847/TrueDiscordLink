package com.firecontroller1847.truediscordlink.listeners.minecraft;

import com.firecontroller1847.truediscordlink.DiscordManager;
import com.firecontroller1847.truediscordlink.TrueDiscordLink;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.SelectorComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        this.onCommand(event.getMessage(), event.getPlayer());
    }

    // Non-Player Commands
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        this.onCommand(event.getCommand(), event.getSender());
    }

    // Event
    public void onCommand(String command, CommandSender sender) {
        // Check for starting slash
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

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
                            BaseComponent[] components = ComponentSerializer.parse(message);
                            if (message.contains("@a") || message.contains("@e") || message.contains("@r") || message.contains("@s") || message.contains("@p")) {
                                List<Entity> entities = sender.getServer().selectEntities(sender, targets);
                                if (!entities.isEmpty()) {
                                    for (int i = 0; i < components.length; i++) {
                                        BaseComponent component = components[i];
                                        if (component instanceof SelectorComponent) {
                                            List<Entity> entitiesInSelector = sender.getServer().selectEntities(sender, ((SelectorComponent) component).getSelector());
                                            components[i] = new TextComponent(entitiesInSelector.stream().map(CommandSender::getName).collect(Collectors.joining(", ")));
                                        }
                                    }
                                }
                            }
                            String stringifiedTellraw = Arrays.stream(components).map(c -> c.toPlainText()).collect(Collectors.joining());
                            if (stringifiedTellraw.length() >= 1995) {
                                stringifiedTellraw = stringifiedTellraw.substring(0, 1994) + "...";
                            }
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
