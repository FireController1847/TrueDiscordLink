package com.visualfiredev.truediscordlink.tabcompleters;

import com.visualfiredev.truediscordlink.TrueDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleterTrueDiscordLink implements TabCompleter {

    // Define Primary Arguments
    public static final String[][] FIRST_ARGS = {
        { "reload", "truediscordlink.command.reload" }
    };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Completions
        ArrayList<String> completions = new ArrayList();

        // First-Level Arguments
        if (args.length == 1) {
            ArrayList<String> originals = new ArrayList<>();
            for (String[] argument : FIRST_ARGS) {
                if (TrueDiscordLink.hasPermission(sender, argument[1])) {
                    originals.add(argument[0]);
                }
            }
            StringUtil.copyPartialMatches(args[0], originals, completions);
        }

        // Sort Completions & Return
        Collections.sort(completions);
        return completions;
    }

}
