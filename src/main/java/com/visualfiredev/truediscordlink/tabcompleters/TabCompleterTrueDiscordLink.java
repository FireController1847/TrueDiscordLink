package com.visualfiredev.truediscordlink.tabcompleters;

import com.visualfiredev.truediscordlink.commands.CommandUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleterTrueDiscordLink implements TabCompleter {

    private static final String[][] FIRST_ARGS = {
        { "reload", "truediscordlink.reload" }
    };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Completions
        final List<String> completions = new ArrayList<>();

        // Check for Base Command
        if (args.length == 1) {
            final List<String> originals = new ArrayList<>();
            for (final String[] ARG : FIRST_ARGS) {
                if (CommandUtil.hasPermission(sender, ARG[1])) {
                    originals.add(ARG[0]);
                }
            }
            StringUtil.copyPartialMatches(args[0], originals, completions);
        }

        // Sort Completions & Return
        Collections.sort(completions);
        return completions;
    }

}
