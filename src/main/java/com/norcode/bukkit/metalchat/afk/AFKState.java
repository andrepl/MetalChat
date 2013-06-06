package com.norcode.bukkit.metalchat.afk;

import org.bukkit.entity.Player;

public class AFKState {
    long timestamp;
    float pitch;
    float yaw;
    double x;
    double y;
    double z;
    boolean inVehicle;
    boolean sprinting;

    public AFKState(Player player) {
        this.timestamp = System.currentTimeMillis();
        this.pitch = player.getLocation().getPitch();
        this.yaw = player.getLocation().getYaw();
        this.x = player.getLocation().getX();
        this.y = player.getLocation().getY();
        this.z = player.getLocation().getZ();
        this.inVehicle = player.isInsideVehicle();
        this.sprinting = player.isSprinting();
    }
}
