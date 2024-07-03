package com.avatar.avatar_7dayshorders.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.avatar.avatar_7dayshorders.Main;
import com.avatar.avatar_7dayshorders.server.ServerConfig;

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

    private static final Map<Integer, List<Integer>> currentWaveMobsPerPlayer = null;

    public static void start(ServerLevel world, Integer weaverNumber) {
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {
            if (currentWaveMobsPerPlayer == null) {
                currentWaveMobsPerPlayer = ServerConfig.getPlayerMobs(player);
                player.sendSystemMessage(
                        Component.translatable("The night starts, the mobs are incoming!"));
            } else {
                List<Integer> currentWave = new ArrayList<>();
                List<MobWeaveDescripton> weaverNumberListMobs = ServerConfig.getListMobs(weaverNumber);
                for (int i = 0; i < weaverNumberListMobs.size(); i++) {
                    MobWeaveDescripton mobsInfo = weaverNumberListMobs.get(i);
                    currentWave
                            .addAll(MobCreate.spawnMobs(world, player, mobsInfo.getMobName(), mobsInfo.getQuantity()));
                }
                currentWaveMobsPerPlayer.put(player.getId(), currentWave);
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
