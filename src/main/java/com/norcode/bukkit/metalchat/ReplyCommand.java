package com.norcode.bukkit.metalchat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReplyCommand implements CommandExecutor {
    MetalChat plugin;

    public ReplyCommand(MetalChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        String targetName = plugin.getMetaString(commandSender, MetaKeys.PM_REPLY_TO);
        if (targetName == null) {
            commandSender.sendMessage(plugin.getMsg("no-reply-recipient"));
            return true;
        }
        CommandSender target;
        if (targetName.equals(plugin.getServer().getConsoleSender().getName())) {
            target = plugin.getServer().getConsoleSender();
        } else {
            target = plugin.getServer().getPlayerExact(targetName);
            if (target == null || !((Player) target).isOnline()) {
                commandSender.sendMessage(plugin.getMsg("no-reply-recipient"));
                return true;
            }
        }

        String message = "";
        for (int i=0;i<args.length;i++) {
            message += args[i] + " ";
        }

        if (message.endsWith(" ")) {
            message = message.substring(0, message.length()-1);
        }
        target.sendMessage(plugin.formatIncomingPrivateMessage(commandSender, target, message));
        commandSender.sendMessage(plugin.formatOutgoingPrivateMessage(commandSender, target, message));
        plugin.setMeta(target, MetaKeys.PM_REPLY_TO, commandSender);
        plugin.setMeta(commandSender, MetaKeys.PM_REPLY_TO, target);
        return true;

    }
}
