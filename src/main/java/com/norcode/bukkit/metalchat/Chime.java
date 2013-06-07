package com.norcode.bukkit.metalchat;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum Chime {
    NONE() {
        @Override
        public void play(final MetalChat plugin, final Player p) {}
    },

    DING_DONG() {
        @Override
        public void play(final MetalChat plugin, final Player p) {
            p.playSound(p.getLocation(), Sound.NOTE_PIANO, 10.0f, 1.414214f);
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override public void run() {
                    p.playSound(p.getLocation(), Sound.NOTE_PIANO, 10.0f, 0.943874f);
                }
            }, 10);
        }
    };

    public abstract void play(final MetalChat plugin, final Player p);

    public static String valuesString() {
        String s = "";
        for (Chime c: values()) {
            s += c.name() + ", ";
        }
        if (s.endsWith(", ")) {
            s = s.substring(0, s.length() -2);
        }
        return s;
    }
}

