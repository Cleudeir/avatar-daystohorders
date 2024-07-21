package com.avatar.avatar_daystohorders.function;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;

public class MobTeleport {
    public static void mobTeleport(Mob mob, ServerLevel world, Player player) {
        int distant = 10 + (int) Math.ceil(Math.random() * 10);
        double playerPosX = player.getX();
        double playerPosY = player.getY();
        double firstMobPosX = mob.getX();
        double firstMobPosY = mob.getY();
        double distance = Math
                .sqrt(Math.pow(playerPosX - firstMobPosX, 2) + Math.pow(playerPosY - firstMobPosY, 2));
        if (distance > 40) {
            System.out.println("Distance: " + distance + ' ' + mob.getName().getString());
            double x = playerPosX - distant;
            double z = playerPosY - distant;
            double y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            mob.teleportTo(x, y, z);
            mob.setTarget(player);
        }
    }

}
