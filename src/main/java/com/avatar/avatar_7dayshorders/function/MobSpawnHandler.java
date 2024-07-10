package com.avatar.avatar_7dayshorders.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.avatar.avatar_7dayshorders.GlobalConfig;
import com.avatar.avatar_7dayshorders.Main;
import com.avatar.avatar_7dayshorders.animation.Animate;
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
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobSpawnHandler {

    private static final Map<String, List<UUID>> currentWaveMobsPerPlayer = new HashMap<>();

    public void message(ServerPlayer player, String message) {
        player.sendSystemMessage(
                Component.translatable(message));
    }

    public void sound(ServerPlayer player, ServerLevel world) {
        world.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE,
                SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    public void start(ServerLevel world, Integer weaverNumber) {
        List<MobWeaveDescripton> weaverNumberListMobs = GlobalConfig.getListMobs(weaverNumber);
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {
            String playerName = player.getName().getString();
            List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);
            if (currentWave == null) {
                currentWave = ServerConfig.loadPlayerMobs(playerName);
                currentWaveMobsPerPlayer.put(playerName, currentWave);
                message(player, "The night starts, the mobs are incoming! " + currentWave.size());
                sound(player, world);
            } else if (currentWave.isEmpty()) {
                int distant = 20 + world.random.nextInt(10);
                sound(player, world);
                PortalSpawnHandler.destroyPortal(world);
                PortalSpawnHandler.createPortal(world, player, distant);
                message(player, "Start new Weave! " + currentWave.size());
                for (MobWeaveDescripton mobsInfo : weaverNumberListMobs) {
                    List<UUID> create = MobCreate.spawnMobs(world, player, mobsInfo.getMobName(),
                            mobsInfo.getQuantity(), distant);
                    currentWave.addAll(create);
                }
                currentWaveMobsPerPlayer.put(playerName, currentWave);
            } else {
                mobCheck(player, world, currentWave);
                message(player, "Weave number mobs " + currentWave.size());
            }
        }
    }

    public void end(ServerLevel world) {
        PortalSpawnHandler.destroyPortal(world);
        currentWaveMobsPerPlayer.clear();
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {
            message(player, "End Weave!");
        }
    }

    private static void mobCheck(Player player, ServerLevel world, List<UUID> currentWave) {
        if (currentWave.size() > 0 && world != null) {
            for (int i = 0; i < currentWave.size(); i++) {
                UUID mobId = currentWave.get(i);
                Mob mob = (Mob) world.getEntity(mobId);
                if (mob != null) {
                    Boolean mobIsAlive = mob.isAlive();
                    if (mobIsAlive && mob.getTarget() == null) {
                        mob.setTarget(player);
                        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9999));
                    }
                    Animate.portal(world, mob.getX(), mob.getY(), mob.getZ());
                    MobTeleport.mobTeleport(mob, world, player);
                } else {
                    currentWave.remove(mobId);
                    currentWaveMobsPerPlayer.put(player.getName().getString(), currentWave);
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

    public void save() {
        ServerConfig.save(currentWaveMobsPerPlayer);
        System.out.println(" Data saved " + currentWaveMobsPerPlayer);
    }

}
