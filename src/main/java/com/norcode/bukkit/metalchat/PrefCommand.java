package com.norcode.bukkit.metalchat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class PrefCommand implements TabExecutor {

    private final MetalChat plugin;

    public PrefCommand(MetalChat plugin) {
        this.plugin = plugin;
    }

    private static List<String> vars = new ArrayList<String>();
    static {
        vars.add("highlight_name");
        vars.add("highlight_chime");
        vars.add("pm_chime");
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] params) {
        LinkedList<String> args = new LinkedList<String>();
        args.addAll(Arrays.asList(params));
        if (args.size() == 0) {
            return false;
        }
        String action = args.pop();
        PlayerPrefs prefs = plugin.getPlayerPrefs((Player) commandSender);
        if (action.equalsIgnoreCase("list")) {
            List<String> msgs = new LinkedList<String>();

            msgs.add(ChatColor.WHITE + "Highlight Name: " + ChatColor.GOLD + prefs.getHighlightName());
            msgs.add(ChatColor.WHITE + "Highlight Chime: " + ChatColor.GOLD + prefs.getHighlightChime().name());
            msgs.add(ChatColor.WHITE + "PM Chime: " + ChatColor.GOLD + prefs.getPmChime().name());
            commandSender.sendMessage(msgs.toArray(new String[0]));
            return true;
        } else if (action.equalsIgnoreCase("get")) {
            if (args.size() == 0) {
                return false;
            }
            String var = args.pop().toLowerCase();
            if (!vars.contains(var)) {
                String msg = "Expected one of ";
                for (String v: vars) {
                    msg += v + ", ";
                }
                if (msg.endsWith(", ")) {
                    msg = msg.substring(0, msg.length()-2);
                }
                commandSender.sendMessage(msg);
                return true;
            }
            if (var.equals("highlight_name")) {
                commandSender.sendMessage("highlight_name=" + prefs.getHighlightName());
            } else if (var.equals("highlight_chime")) {
                commandSender.sendMessage("highlight_chime=" + prefs.getHighlightChime().name());
            } else if (var.equals("highlight_chime")) {
                commandSender.sendMessage("pm_chime=" + prefs.getPmChime().name());
            }
            return true;
        } else if (action.equalsIgnoreCase("set")) {
            String var = args.pop().toLowerCase();
            if (!vars.contains(var)) {
                String msg = "Expected one of ";
                for (String v: vars) {
                    msg += v + ", ";
                }
                if (msg.endsWith(", ")) {
                    msg = msg.substring(0, msg.length()-2);
                }
                commandSender.sendMessage(msg);
                return true;
            }
            String value = null;
            if (args.size() > 0) {
                value = "";
                for (String arg: args) { value += arg + " "; }
                if (value.endsWith(" ")) { value = value.substring(0, value.length()-1); }
            }
            if (var.endsWith("chime")) {
                Chime chime = null;
                if (value != null) {
                    chime = Chime.valueOf(value.toUpperCase());
                    if (chime == null) {
                        commandSender.sendMessage(plugin.getMsg("Chime must be one of: " + Chime.valuesString()));
                        return true;
                    }
                } else {
                    chime = Chime.NONE;
                }

                if (var.equals("highlight_chime")) {
                    prefs.setHighlightChime(chime);
                } else {
                    prefs.setPmChime(chime);
                }
                commandSender.sendMessage(var + " was set to " + chime.name());
            } else {
                if (var.equalsIgnoreCase("highlight_name")) {
                    prefs.setHighlightName(value);
                }
                commandSender.sendMessage(var + " was set to " + (value == null ? "''" : value));
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> results = new LinkedList<String>();
        if (args.length == 1) {
            String a = args[0].toLowerCase();
            if ("list".startsWith(a)) {
                results.add("list");
            }
            if ("set".startsWith(a)) {
                results.add("set");
            }
            if ("get".startsWith(a)) {
                results.add("get");
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("get"))) {
            String v = args[1].toLowerCase();
            for (String var: vars) {
                if (var.startsWith(v)) {
                    results.add(var);
                }
            }
        } else if (args.length == 2) {
            return results;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set") && args[1].endsWith("chime")) {
            String c = args[2].toLowerCase();
            for (Chime chime: Chime.values()) {
                if (chime.name().toLowerCase().startsWith(c)) {
                    results.add(chime.name());
                }
            }
        }
        return results;
    }
}
