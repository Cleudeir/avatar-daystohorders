package com.avatar.avatar_7dayshorders.animation;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class Animate {
    public static void portal(ServerLevel world, Entity animationPos) {
        if (animationPos != null) {
            world.sendParticles(ParticleTypes.EXPLOSION,
                    animationPos.getX() + 0.5,
                    animationPos.getY() + 0.5,
                    animationPos.getZ() + 0.5,
                    10,
                    0.2,
                    0.2,
                    0.2,
                    0.5);
        }
    }
}
