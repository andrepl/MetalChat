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
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;


public class AFKManager implements Listener {
    public MetalChat plugin;
    HashMap<String, LinkedList<AFKState>> playerData = new HashMap<String, LinkedList<AFKState>>();
    HashMap<String, Long> afkTimes = new HashMap<String, Long>();
    BukkitTask task = null;
    private Random random = new Random();
    public AFKManager(final MetalChat plugin) {
        this.plugin = plugin;

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            private int uncheckedRunCount = 0;
            @Override
            public void run() {
                Player[] players = plugin.getServer().getOnlinePlayers();
                Player p = players[random.nextInt(players.length)];
                saveState(p);
                analyzeAFKStates(p);
            }
        }, 5, 5);

    }

    private void analyzeAFKStates(Player p) {
        // TODO: Examine the list of AFKStates for this player
        // and determine if he's cheating.

    }

    public void recordActivity(Player player) {
        if (afkTimes.containsKey(player.getName())) {
            unAFK(player);

        }
        if (playerData.containsKey(player.getName())) {
            playerData.get(player.getName()).clear();
        }
    }

    public void AFK(Player player, String reason) {
        if (afkTimes.containsKey(player.getName())) {
            return;
        }
        plugin.getServer().broadcastMessage(plugin.getMsg("player-went-afk", player.getDisplayName()));
        player.setMetadata(MetaKeys.AFK_CACHED_DISPLAY_NAME, new FixedMetadataValue(plugin, player.getDisplayName()));
        player.setMetadata(MetaKeys.AFK_CACHED_LIST_NAME, new FixedMetadataValue(plugin, player.getPlayerListName()));

        String afkName = plugin.getConfig().getString("afk.name-prefix", "") + player.getDisplayName() + plugin.getConfig().getString("afk.name-suffix", "");
        player.setPlayerListName(afkName);
        player.setDisplayName(afkName);
        afkTimes.put(player.getName(), System.currentTimeMillis());
    }

    public void unAFK(Player player) {
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
        if (!event.getMessage().contains("afk")) {
            recordActivity(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                recordActivity(event.getPlayer());
            }
        }, 0);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (afkTimes.containsKey(event.getPlayer().getName())) {
            recordActivity(event.getPlayer());
        } else {
            saveState(event.getPlayer());
        }
    }

    private void saveState(Player player) {
        LinkedList<AFKState> states = playerData.get(player.getName());
        if (states.size() == 0) {
            states.add(new AFKState(player));
            return;
        }
        AFKState state = states.peekLast();
        if (System.currentTimeMillis() - state.timestamp > 5000) {
            states.add(new AFKState(player));
            while (states.size() > 5) {
                states.pollFirst();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerData.remove(event.getPlayer().getName());
        afkTimes.remove(event.getPlayer().getName());
    }


    public boolean isAFK(Player player) {
        return afkTimes.containsKey(player.getName());
    }
}
