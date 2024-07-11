package com.avatar.avatar_7dayshorders.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.avatar.avatar_7dayshorders.GlobalConfig;
import com.avatar.avatar_7dayshorders.Main;
import com.avatar.avatar_7dayshorders.Object.MobWeaveDescripton;
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
import java.util.stream.Collectors;

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
        int totalMobs = 0;
        int maxMobs = GlobalConfig.loadMaxMobsPerPlayer();
        for (MobWeaveDescripton mobsInfo : weaverNumberListMobs) {
            totalMobs += mobsInfo.getQuantity();
        }
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {

            String playerName = player.getName().getString();
            List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);

            if (currentWave == null) {

                currentWave = ServerConfig.loadPlayerMobs(playerName);
                currentWaveMobsPerPlayer.put(playerName, currentWave);

                message(player, currentWave.size() + " Mobs are still alive!");
                message(player, "The night starts, the mobs are incoming! ");
                sound(player, world);

            } else if (currentWave.isEmpty()) {

                int distant = 15 + world.random.nextInt(15);
                // distant = 0;
                int index = 6;

                sound(player, world);
                PortalSpawnHandler.recreatePortal(world);
                PortalSpawnHandler.createPortal(world, player, distant, index);

                message(player, currentWave.size() + " Mobs are still alive!");
                message(player, "Start new Weave!");

                for (MobWeaveDescripton mobsInfo : weaverNumberListMobs) {
                    int quantityWeight = (int) Math.floor(maxMobs * mobsInfo.getQuantity() / totalMobs);
                    if (quantityWeight == 0) {
                        quantityWeight = 1;
                    }
                    message(player,
                            mobsInfo.getMobName() + " qnt: " + mobsInfo.getQuantity() + " weight:" + quantityWeight);

                    List<UUID> create = MobCreate.spawnMobs(world, player, mobsInfo.getMobName(),
                            quantityWeight, distant, index);
                    currentWave.addAll(create);
                }

                currentWaveMobsPerPlayer.put(playerName, currentWave);

            } else {

                mobCheck(player, world, currentWave);
                message(player, currentWave.size() + " Mobs are still alive!");

            }
        }
    }

    public void end(ServerLevel world) {
        PortalSpawnHandler.recreatePortal(world);
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
        ServerConfig.saveCurrentWaveMobsPerPlayer(currentWaveMobsPerPlayer);
        System.out.println(" Data saved " + currentWaveMobsPerPlayer);
    }

}
