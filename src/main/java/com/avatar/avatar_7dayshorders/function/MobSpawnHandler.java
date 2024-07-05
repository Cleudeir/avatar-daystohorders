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
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobSpawnHandler {

    private static final Map<String, List<Integer>> currentWaveMobsPerPlayer = new HashMap<>();

    public static void start(ServerLevel world, Integer weaverNumber) {
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {
            String playerName = player.getName().getString();
            if (currentWaveMobsPerPlayer.isEmpty()) {
                List<Integer> currentWaveMobs = ServerConfig.getPlayerMobs(playerName);
                currentWaveMobsPerPlayer.put(playerName, currentWaveMobs);
                player.sendSystemMessage(
                        Component.translatable("The night starts, the mobs are incoming!"));
            } else {
                List<MobWeaveDescripton> weaverNumberListMobs = ServerConfig.getListMobs(weaverNumber);
                List<Integer> currentWave = currentWaveMobsPerPlayer.get(playerName);
                if (currentWave == null) {
                    currentWave = new ArrayList<>();
                }
                if(currentWave.isEmpty()){
                    for (MobWeaveDescripton mobsInfo : weaverNumberListMobs) {
                        player.sendSystemMessage(
                            Component.translatable("Weave started"));
                        List<Integer> create = MobCreate.spawnMobs(world, player, mobsInfo.getMobName(),
                                mobsInfo.getQuantity());
                        currentWave.addAll(create);
                    }
                }else{
                    player.sendSystemMessage(
                        Component.translatable("Weave number mobs " + currentWave.size() ));
                }
                currentWaveMobsPerPlayer.put(playerName, currentWave);
            }
        }
    }

    public static void end() {
        currentWaveMobsPerPlayer.clear();
        ServerConfig.save(currentWaveMobsPerPlayer);
    }

    public static void save() {
        ServerConfig.save(currentWaveMobsPerPlayer);
        System.out.println(" Data saved " + currentWaveMobsPerPlayer);
    }
}
