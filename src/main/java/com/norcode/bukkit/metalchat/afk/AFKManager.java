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

import java.util.ArrayList;
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
            @Override
            public void run() {
                Player[] players = plugin.getServer().getOnlinePlayers();
                ArrayList<Player> eligible = new ArrayList<Player>();
                for (int i=0;i<players.length;i++) {
                    if (afkTimes.containsKey(players[i].getName())) continue;
                    if (players[i].hasPermission("metalchat.afk.noauto")) continue;
                    eligible.add(players[i]);
                }
                if (eligible.size() > 0) {
                    Player p = eligible.get(random.nextInt(eligible.size()));
                    saveState(p);
                    analyzeAFKStates(p);
                }
            }
        }, 5, 5);

    }

    public void onDisable() {
        task.cancel();
    }

    private void analyzeAFKStates(Player p) {
        if (playerData.get(p.getName()).size() < 3) {
            return;
        }
        LinkedList<AFKState> states = playerData.get(p.getName());
        long start = states.peekFirst().timestamp;
        long end = states.peekLast().timestamp;
        long stateSpan = end-start;
        long autoAFKTime = plugin.getConfig().getLong("afk.auto-afk-time", 120) * 1000;

        if (stateSpan < autoAFKTime) {
            return;
        }
        // He/She has 2 mins worth of afk states. examine them for
        // afk-like behaviour
        AFKState prev;
        AFKState curr;
        int i = 0;
        boolean afk = true;
        boolean isMoving = false;
        for (i=1;i<states.size();i++) {
            curr = states.get(i);
            prev = states.get(i-1);
            if (prev.x != curr.x || prev.y != curr.y || prev.z != curr.z) {
                isMoving = true;
            }
            if (curr.yaw != prev.yaw) {
                afk = false;
            }
        }
        if (afk) {
            AFK(p, isMoving, " [auto]");
        }
    }

    public void recordActivity(Player player) {
        if (afkTimes.containsKey(player.getName())) {
            unAFK(player);

        }
        if (playerData.containsKey(player.getName())) {
            playerData.get(player.getName()).clear();
        }
    }

    public void AFK(Player player, boolean moving, String reason) {
        if (afkTimes.containsKey(player.getName())) {
            return;
        }
        if (reason == null) {
            reason = plugin.getMsg("no-afk-reason");
        }
        plugin.getServer().broadcastMessage(plugin.getMsg("player-went-afk", player.getDisplayName()));
        player.setMetadata(MetaKeys.AFK_CACHED_DISPLAY_NAME, new FixedMetadataValue(plugin, player.getDisplayName()));
        player.setMetadata(MetaKeys.AFK_CACHED_LIST_NAME, new FixedMetadataValue(plugin, player.getPlayerListName()));
        player.setMetadata(MetaKeys.AFK_MOVING, new FixedMetadataValue(plugin, true));
        player.setMetadata(MetaKeys.AFK_REASON, new FixedMetadataValue(plugin, reason));
        String afkName = plugin.getConfig().getString("afk.name-prefix", "") + player.getDisplayName() + plugin.getConfig().getString("afk.name-suffix", "");
        if (afkName.length() > 16) {
            afkName = afkName.substring(0,15);
        }
        player.setPlayerListName(afkName);

        player.setDisplayName(afkName);
        afkTimes.put(player.getName(), System.currentTimeMillis());
    }

    public void unAFK(Player player) {
        if (!afkTimes.containsKey(player.getName())) {
            return;
        }
        if (playerData.containsKey(player.getName())) {
            playerData.get(player.getName()).clear();
        }
        player.setDisplayName(player.getMetadata(MetaKeys.AFK_CACHED_DISPLAY_NAME).get(0).asString());
        player.setPlayerListName(player.getMetadata(MetaKeys.AFK_CACHED_LIST_NAME).get(0).asString());
        player.removeMetadata(MetaKeys.AFK_CACHED_DISPLAY_NAME, plugin);
        player.removeMetadata(MetaKeys.AFK_CACHED_LIST_NAME, plugin);
        player.removeMetadata(MetaKeys.AFK_MOVING, plugin);
        player.removeMetadata(MetaKeys.AFK_REASON, plugin);
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
            if (!event.getPlayer().hasMetadata(MetaKeys.AFK_MOVING) || event.getFrom().getYaw() != event.getTo().getYaw()) {
                recordActivity(event.getPlayer());
            }
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
        if (System.currentTimeMillis() - state.timestamp > 10000) {
            states.add(new AFKState(player));
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
