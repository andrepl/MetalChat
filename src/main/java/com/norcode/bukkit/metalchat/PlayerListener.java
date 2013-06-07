package com.norcode.bukkit.metalchat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsPlayerUpdateEvent;

import java.util.List;


public class PlayerListener implements Listener {
    private MetalChat plugin;

    public PlayerListener(MetalChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPMTabComplete(PlayerChatTabCompleteEvent event) {
        if (!event.getChatMessage().equals(event.getLastToken())) {
             return; // Only kick in when we're on the first token.
        }
        if (!event.getChatMessage().startsWith("@")) {
            return; // only when the msg starts with @
        }
        String partial = event.getLastToken().substring(1);
        List<Player> results = plugin.getServer().matchPlayer(partial);
        event.getTabCompletions().clear();
        for (Player p: results) {
            if (event.getPlayer().canSee(p)) {
                event.getTabCompletions().add("@" + p.getName());
            }
        }
    }

    @EventHandler
    public void onZPermissionsPlayerUpdate(final ZPermissionsPlayerUpdateEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.assignChatMeta(event.getPlayer());
            }
        }, 0);
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.assignChatMeta(event.getPlayer());
            }
        }, 0);
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (event.getMessage().startsWith("@")) {
            // PRIVATE MESSAGE
            event.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!event.getPlayer().hasPermission("metalchat.pm")) {
                        event.getPlayer().sendMessage(plugin.getMsg("no-permission"));
                        return;
                    }
                    String msg = null;
                    if (event.getMessage().startsWith("@ ")) {
                        // replying.
                        msg = event.getMessage().substring(2);
                        plugin.getServer().getPluginCommand("reply").execute(event.getPlayer(), "@", msg.split(" "));
                    } else {
                        // should be @<playername>
                        String[] params = event.getMessage().substring(1).split(" ");
                        plugin.getServer().getPluginCommand("msg").execute(event.getPlayer(), "@", params);
                    }
                }
            }.runTask(plugin);
        } else {
            // CHAT
            event.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Player player = event.getPlayer();
                    if (!player.hasPermission("metalchat.global.chat")) {
                        return;
                    }
                    String message = event.getMessage();
                    String formatted = plugin.formatChatMessage(event.getPlayer(), message);
                    //
                    PlayerPrefs prefs;
                    for (Player p: plugin.getServer().getOnlinePlayers()) {
                        prefs = plugin.getPlayerPrefs(p);
                        String hln = prefs.getHighlightName();
                        if (hln != null && message.toLowerCase().contains(hln.toLowerCase())) {
                            prefs.getHighlightChime().play(plugin, p);
                            p.sendMessage(plugin.formatChatMessage(event.getPlayer(), plugin.highlight(message, hln)));
                        } else {
                            p.sendMessage(formatted);
                        }
                    }
                }
            }.runTask(plugin);
        }
    }

}
