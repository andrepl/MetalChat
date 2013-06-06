package com.norcode.bukkit.metalchat.afk;

import com.norcode.bukkit.metalchat.MetaKeys;
import com.norcode.bukkit.metalchat.MetalChat;

import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.LinkedList;


public class AFKManager implements Listener {
    MetalChat plugin;
    HashMap<String, LinkedList<AFKState>> playerData = new HashMap<String, LinkedList<AFKState>>();
    HashMap<String, Long> afkTimes = new HashMap<String, Long>();

    public AFKManager(MetalChat plugin) {
        this.plugin = plugin;
    }

    public void recordActivity(Player player) {
        if (afkTimes.containsKey(player.getName())) {
            unAFK(player);

        }
        if (playerData.containsKey(player.getName())) {
            playerData.get(player.getName()).clear();
        }
    }

    private void AFK(Player player, String reason) {
        if (afkTimes.containsKey(player.getName())) {
            return;
        }
        player.setMetadata(MetaKeys.AFK_CACHED_DISPLAY_NAME, new FixedMetadataValue(plugin, player.getDisplayName()));
        player.setMetadata(MetaKeys.AFK_CACHED_LIST_NAME, new FixedMetadataValue(plugin, player.getPlayerListName()));

        String afkName = plugin.getConfig().getString("afk.name-prefix", "") + player.getDisplayName() + plugin.getConfig().getString("afk.name-suffix", "");
        player.setPlayerListName(afkName);
        player.setDisplayName(afkName);
        afkTimes.put(player.getName(), System.currentTimeMillis());
        plugin.getServer().broadcastMessage(plugin.getMsg("player-went-afk", player.getDisplayName()));
    }

    private void unAFK(Player player) {
        if (!afkTimes.containsKey(player.getName())) {
            return;
        }
        player.setDisplayName(player.getMetadata(MetaKeys.AFK_CACHED_DISPLAY_NAME).get(0).asString());
        player.setPlayerListName(player.getMetadata(MetaKeys.AFK_CACHED_LIST_NAME).get(0).asString());
        plugin.getServer().broadcastMessage(plugin.getMsg("player-is-no-longer-afk", player.getDisplayName()));
        afkTimes.remove(player.getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerData.put(event.getPlayer().getName(), new LinkedList<AFKState>());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                recordActivity(event.getPlayer());
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerData.remove(event.getPlayer().getName());
        afkTimes.remove(event.getPlayer().getName());
    }



}
