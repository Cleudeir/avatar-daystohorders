package com.avatar.avatar_7dayshorders.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.avatar.avatar_7dayshorders.GlobalConfig;
import com.avatar.avatar_7dayshorders.Main;
import com.avatar.avatar_7dayshorders.server.ServerConfig;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobSpawnHandler {

    private static final Map<String, List<UUID>> currentWaveMobsPerPlayer = new HashMap<>();

    public void start(ServerLevel world, Integer weaverNumber) {
        List<MobWeaveDescripton> weaverNumberListMobs = GlobalConfig.getListMobs(weaverNumber);
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {
            String playerName = player.getName().getString();
            List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);
            System.out.println("currentWave " + currentWave);
            if (currentWave == null) {
                currentWave = ServerConfig.loadPlayerMobs(playerName);
                currentWaveMobsPerPlayer.put(playerName, currentWave);
                player.sendSystemMessage(
                        Component.translatable("The night starts, the mobs are incoming!"));
                world.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE,
                        SoundSource.HOSTILE, 1.0F, 1.0F);
            } else if (currentWave.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("Start new Weave!"));

                System.out.println("mobsInfo >>>>>>> " + weaverNumberListMobs);
                for (MobWeaveDescripton mobsInfo : weaverNumberListMobs) {
                    System.out.println("mobsInfo >>>>>>> " + mobsInfo.getMobName() + " " + mobsInfo.getQuantity());
                    List<UUID> create = MobCreate.spawnMobs(world, player, mobsInfo.getMobName(),
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

    public void end(ServerLevel world) {
        currentWaveMobsPerPlayer.clear();
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {
            player.sendSystemMessage(
                    Component.translatable("End Weave!"));
        }
    }

    private static void mobCheck(Player player, ServerLevel world, List<UUID> currentWave) {
        if (currentWave.size() > 0 && world != null) {
            for (int i = 0; i < currentWave.size(); i++) {
                UUID mobId = currentWave.get(i);
                Mob mob = (Mob) world.getEntity(mobId);
                if (mob != null) {
                    System.out.println("mob " + mob.getName().getString());
                    Boolean mobIsAlive = mob.isAlive();
                    if (mobIsAlive && mob.getTarget() == null) {
                        mob.setTarget(player);
                        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9999));
                    }
                    mobTeleport(mob, world, player);
                }
            }
        }
    }

    public static void removeMob(UUID mobId, ServerLevel world) {
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {
            String playerName = player.getName().getString();
            List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);
            if (currentWave != null && currentWave.contains(mobId)) {
                currentWave.remove(mobId);
                currentWaveMobsPerPlayer.put(playerName, currentWave);
                break;
            }
        }
    }

    public static void mobTeleport(Mob mob, ServerLevel world, Player player) {
        int distant = 20;
        double playerPosX = player.getX();
        double playerPosY = player.getY();
        double firstMobPosX = mob.getX();
        double firstMobPosY = mob.getY();
        double distance = Math
                .sqrt(Math.pow(playerPosX - firstMobPosX, 2) + Math.pow(playerPosY - firstMobPosY, 2));
        if (distance > 40) {
            System.out.println("Distance: " + distance + ' ' + mob.getName().getString());
            double x = playerPosX + world.random.nextInt(20) - distant;
            double z = playerPosY + world.random.nextInt(20) - distant;
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
