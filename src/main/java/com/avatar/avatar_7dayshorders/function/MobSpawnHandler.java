package com.avatar.avatar_7dayshorders.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.avatar.avatar_7dayshorders.Main;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobSpawnHandler {

    private static final List<LivingEntity> currentWaveMobs = new ArrayList<>();

    private static final Map<Integer, List<LivingEntity>> currentWaveMobsPerPlayer = new HashMap<>();

    public static void start(ServerLevel world) {
        if (currentWaveMobs.isEmpty()) {
            Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
            for (ServerPlayer player : players) {
                player.sendSystemMessage(
                        Component.translatable("The night starts, the mobs are incoming!"));
                List<LivingEntity> currentWave = new ArrayList<>();
                currentWave.addAll(MobCreate.spawnMobs(world, player, "minecraft:zombie", 1));
                currentWaveMobsPerPlayer.put(player.getId(), currentWaveMobs);
            }
        }
    }

    private static void targetMobsToPlayer(Player player) {
        for (LivingEntity mob : currentWaveMobs) {
            if (mob instanceof Mob) {
                Mob creature = (Mob) mob;
                creature.setTarget(player);
            }
        }
    }
}
