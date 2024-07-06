package com.avatar.avatar_7dayshorders.function;

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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobSpawnHandler {

    private static final Map<String, List<Integer>> currentWaveMobsPerPlayer = new HashMap<>();

    public void start(ServerLevel world, Integer weaverNumber) {
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        List<MobWeaveDescripton> weaverNumberListMobs = ServerConfig.getListMobs(weaverNumber);
        for (ServerPlayer player : players) {
            String playerName = player.getName().getString();
            System.out.println("currentWaveMobsPerPlayer " + currentWaveMobsPerPlayer);
            List<Integer> currentWave = currentWaveMobsPerPlayer.get(playerName);
            System.out.println("currentWave " + currentWave);
            if (currentWave == null) {
                List<Integer> currentWaveMobs = ServerConfig.getPlayerMobs(playerName);
                currentWaveMobsPerPlayer.put(playerName, currentWaveMobs);
                System.out.println("currentWaveMobsPerPlayer " + currentWaveMobsPerPlayer);
                player.sendSystemMessage(
                        Component.translatable("The night starts, the mobs are incoming!"));
            } else if (currentWave.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("Start new Weave!"));
                for (MobWeaveDescripton mobsInfo : weaverNumberListMobs) {
                    List<Integer> create = MobCreate.spawnMobs(world, player, mobsInfo.getMobName(),
                            mobsInfo.getQuantity());
                    currentWave.addAll(create);
                }
                currentWaveMobsPerPlayer.put(playerName, currentWave);
            } else {
                mobCheck(player, world, currentWave);
                player.sendSystemMessage(
                        Component.translatable("Weave number mobs " + currentWave.size()));
            }
        }
    }

    public void end() {
        currentWaveMobsPerPlayer.clear();
    }
    private static void mobCheck(Player player, ServerLevel world,  List<Integer> currentWave) {
        String playerName = player.getName().getString();
        if ( currentWave.size() > 0 && world != null) {
            for (int i = 0; i < currentWave.size(); i++) {
                int mobId = currentWave.get(i);
                Mob mob = (Mob) world.getEntity(mobId);
                System.out.println(mob);
                if (mob != null) {
                    System.out.println("mob " + mob.getName().getString());
                    Boolean mobIsAlive = mob.isAlive();
                    if (!mobIsAlive) {
                        currentWave.remove(i);
                        currentWaveMobsPerPlayer.put(playerName, currentWave);
                    }
                    if (mobIsAlive && mob.getTarget() == null) {
                        mob.setTarget(player);
                    }                    
                    mobTeleport(mob, world, player);
                }
            }
        }
    }

    public static void mobTeleport(Mob mob, ServerLevel world, Player player) {
        int distante = 30;
        double playerPosX = player.getX();
        double playerPosY = player.getY();
        double firstMobPosX = mob.getX();
        double firstMobPosY = mob.getY();
        double distance = Math
                .sqrt(Math.pow(playerPosX - firstMobPosX, 2) + Math.pow(playerPosY - firstMobPosY, 2));
        System.out.println("Distance: " + distance + ' ' + mob.getName().getString());
        if (distance > 5) {
            double x = playerPosX + world.random.nextInt(20) - distante;
            double z = playerPosY + world.random.nextInt(20) - distante;
            double y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            mob.teleportTo(x, y, z);
            mob.setTarget(player);
        }
    }

    public void save() {
        ServerConfig.save(currentWaveMobsPerPlayer);
        System.out.println(" Data saved " + currentWaveMobsPerPlayer);
    }
}
