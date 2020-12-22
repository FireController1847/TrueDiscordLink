package com.firecontroller1847.truediscordlink.tabcompleters;

import com.firecontroller1847.truediscordlink.TrueDiscordLink;
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
        { "reload", "truediscordlink.command.reload" },
        { "link", "truediscordlink.command.link" },
        { "unlink", "truediscordlink.command.unlink" }
    };

    // Define Secondary Arguments
    public static final String[][][] SECONDARY_ARGS = {
        { FIRST_ARGS[1], { "yes", "" } },
        { FIRST_ARGS[1], { "no", "" } }
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
        } else if (args.length == 2) {
            ArrayList<String> originals = new ArrayList<>();
            for (String[][] arguments : SECONDARY_ARGS) {
                if (arguments[0][0].equalsIgnoreCase(args[0]) && TrueDiscordLink.hasPermission(sender, arguments[0][1])) {
                    if (TrueDiscordLink.hasPermission(sender, arguments[1][1])) {
                        originals.add(arguments[1][0]);
                    }
                }
            }
            StringUtil.copyPartialMatches(args[1], originals, completions);
        }

        // Sort Completions & Return
        Collections.sort(completions);
        return completions;
    }

}
