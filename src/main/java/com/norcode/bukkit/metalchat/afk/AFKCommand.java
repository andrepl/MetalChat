package com.norcode.bukkit.metalchat.afk;

import com.norcode.bukkit.metalchat.MetalChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 6/5/13
 * Time: 10:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class AFKCommand implements CommandExecutor {
    MetalChat plugin;
    public AFKCommand(MetalChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission("metalchat.afk")) {
            if (plugin.isAFK((Player) sender)) {
                plugin.getAFKManager().unAFK((Player) sender);
                return true;
            } else {
                String reason = "";
                for (int i=0;i<args.length;i++) {
                    reason += args[i] + " ";
                }
                if (reason.endsWith(" ")) {
                    reason = reason.substring(0,reason.length()-1);
                }
                plugin.getAFKManager().AFK((Player) sender, reason);
                return true;
            }
        }
        return false;
    }
}
