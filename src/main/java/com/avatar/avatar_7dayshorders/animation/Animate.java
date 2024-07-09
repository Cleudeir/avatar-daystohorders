package com.avatar.avatar_7dayshorders.animation;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class Animate {
    public static void portal(ServerLevel world, double x, double y, double z) {
        world.sendParticles(ParticleTypes.PORTAL,
                x + 0.5,
                y + 0.5,
                z + 0.5,
                30,
                0.5,
                1.0,
                0.5,
                0.1);

    }
}
