package com.norcode.bukkit.metalchat;

import org.bukkit.entity.Player;

public class PlayerPrefs {
    private MetalChat plugin;
    private String playerName;
    private String highlightName;
    private Chime pmChime = Chime.NONE;
    private Chime highlightChime = Chime.NONE;

    public static final String IS_HIGHLIGHT_NAME = "metalchat-highlight-name";
    public static final String IS_PM_CHIME = "metalchat-pm-chime";
    public static final String IS_HIGHLIGHT_CHIME = "metalchat-highlight-chime";


    public static PlayerPrefs forPlayer(MetalChat plugin, Player player) {
        PlayerPrefs pp = new PlayerPrefs(plugin, player.getName());
        pp.highlightName = plugin.vaultChat.getPlayerInfoString(player, IS_HIGHLIGHT_NAME, player.getName());
        pp.pmChime = Chime.valueOf(plugin.vaultChat.getPlayerInfoString(player, IS_PM_CHIME, "NONE"));
        pp.highlightChime = Chime.valueOf(plugin.vaultChat.getPlayerInfoString(player, IS_HIGHLIGHT_CHIME, "NONE"));
        return pp;
    }

    private PlayerPrefs(MetalChat plugin, String playerName) {
        this.plugin = plugin;
        this.playerName = playerName;
        this.highlightName = highlightName;
        this.pmChime = pmChime;
        this.highlightChime = highlightChime;
    }

    public String getHighlightName() {
        return highlightName;
    }

    public void setHighlightName(String highlightName) {
        this.highlightName = highlightName;
        plugin.vaultChat.setPlayerInfoString(plugin.getServer().getPlayer(playerName), IS_HIGHLIGHT_NAME, this.highlightName);
    }

    public Chime getPmChime() {
        return pmChime;
    }

    public void setPmChime(Chime pmChime) {
        this.pmChime = pmChime;
        Player p = plugin.getServer().getPlayer(playerName);
        plugin.getLogger().info("setPlayerInfoString(" + playerName + ", " + IS_PM_CHIME + ", " + this.pmChime.name());
        plugin.vaultChat.setPlayerInfoString(p, IS_PM_CHIME, this.pmChime.name());
    }

    public Chime getHighlightChime() {
        return highlightChime;
    }

    public void setHighlightChime(Chime highlightChime) {
        this.highlightChime = highlightChime;
        plugin.vaultChat.setPlayerInfoString(plugin.getServer().getPlayer(playerName), IS_HIGHLIGHT_CHIME, this.highlightChime.name());
    }

}
