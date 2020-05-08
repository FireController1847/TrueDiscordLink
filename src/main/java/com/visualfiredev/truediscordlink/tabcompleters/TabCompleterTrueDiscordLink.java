package com.visualfiredev.truediscordlink.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabCompleterTrueDiscordLink implements TabCompleter {

    private static final String[] FIRST_ARGS = { "reload" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Completions
        final List<String> completions = new ArrayList<>();

        // Check for Base Command
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(FIRST_ARGS), completions);
        }

        // Sort Completions & Return
        Collections.sort(completions);
        return completions;
    }

}
