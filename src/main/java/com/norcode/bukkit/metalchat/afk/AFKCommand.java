package com.norcode.bukkit.metalchat.afk;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 6/5/13
 * Time: 10:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class AFKCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender.hasPermission("metalchat.commands.")) {

        }
        return false;
    }
}
