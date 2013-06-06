package com.norcode.bukkit.metalchat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class PMCommand implements TabExecutor {
    MetalChat plugin;

    public PMCommand(MetalChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        List<CommandSender> matches = new ArrayList<CommandSender>();
        if (args[0].equalsIgnoreCase(plugin.getServer().getConsoleSender().getName())) {
            matches.add(plugin.getServer().getConsoleSender());
        } else {
            List<Player> playerMatches = plugin.getServer().matchPlayer(args[0]);
            if (playerMatches.size() != 1) {
                commandSender.sendMessage(plugin.getMsg("unknown-recipient", args[0]));
                plugin.getLogger().info("Matches: " + matches);
                return true;
            } else {
                matches.add(playerMatches.get(0));
            }
        }

        String message = "";
        for (int i=1;i<args.length;i++) {
            message += args[i] + " ";
        }
        if (message.endsWith(" ")) {
            message = message.substring(0, message.length()-1);
        }
        CommandSender target = matches.get(0);
        if (target.getName().equals(commandSender.getName())) {
            commandSender.sendMessage(plugin.getMsg("talking-to-yourself"));
            return true;
        }
        target.sendMessage(plugin.formatIncomingPrivateMessage(commandSender, target, message));
        commandSender.sendMessage(plugin.formatOutgoingPrivateMessage(commandSender, target, message));
        plugin.setMeta(target, MetaKeys.PM_REPLY_TO, commandSender.getName());
        plugin.setMeta(commandSender, MetaKeys.PM_REPLY_TO, target.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
