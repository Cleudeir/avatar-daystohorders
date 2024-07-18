package com.avatar.avatar_daystohorders.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.avatar.avatar_daystohorders.GlobalConfig;
import com.avatar.avatar_daystohorders.Main;
import com.avatar.avatar_daystohorders.animation.Animate;
import com.avatar.avatar_daystohorders.object.MobWaveDescripton;
import com.avatar.avatar_daystohorders.server.ServerConfig;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobSpawnHandler {

    private static final Map<String, List<UUID>> currentWaveMobsPerPlayer = new HashMap<>();

    public static void sendTitleMessage(ServerPlayer player, String title, int fadeIn, int stay,
            int fadeOut) {
        // Send the title
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        // Set the animation times
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
    }

    public void message(ServerPlayer player, String message) {
        player.sendSystemMessage(
                Component.translatable(message));
    }

    public void sound(ServerPlayer player, ServerLevel world, String effect) {
        SoundEvent soundEvent;
        switch (effect.toLowerCase()) {
            case "bell_resonate":
                soundEvent = SoundEvents.BELL_RESONATE;
                break;
            case "entity_creeper_hurt":
                soundEvent = SoundEvents.CREEPER_HURT;
                break;

            default:
                soundEvent = SoundEvents.BELL_RESONATE;
                break;
        }
        world.playSound(null, player.blockPosition(), soundEvent, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    public void startWave(ServerLevel world, Integer waverNumber) {
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        List<MobWaveDescripton> waverNumberListMobs = GlobalConfig.getListMobs(waverNumber);
        int totalMobs = 0;
        int maxMobs = GlobalConfig.loadMaxMobsPerPlayer();
        for (MobWaveDescripton mobsInfo : waverNumberListMobs) {
            totalMobs += mobsInfo.getQuantity();
        }
        for (ServerPlayer player : players) {
            // StatusBarRenderer.updatePlayerStatus(player.getUUID(), 0, maxMobs - 1);
            String playerName = player.getName().getString();
            List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);
            if (currentWave == null) {
                currentWave = ServerConfig.loadPlayerMobs(playerName);
                if (currentWave == null || currentWave.isEmpty()) {
                    sendTitleMessage(player, "The night starts", 5, 40, 10);
                    createWave(player, world, waverNumber, waverNumberListMobs, totalMobs, maxMobs);
                } else {
                    currentWaveMobsPerPlayer.put(playerName, currentWave);
                    StatusBarRenderer.updatePlayerStatus(player.getUUID(), currentWave.size(), maxMobs - 1);
                }

                sound(player, world, "bell_resonate");
            } else if (currentWave.isEmpty()) {
                createWave(player, world, waverNumber, waverNumberListMobs, totalMobs, maxMobs);
                sound(player, world, "bell_resonate");
            } else {
                waveMobCheck(player, world, waverNumber, maxMobs);
            }
        }
    }

    public void createWave(ServerPlayer player,
            ServerLevel world, Integer waverNumber,
            List<MobWaveDescripton> waverNumberListMobs,
            int totalMobs, int maxMobs) {

        List<UUID> currentWave = new ArrayList<>();

        int distant = 10 + world.random.nextInt(10);
        int index = 6;

        sound(player, world, "bell_resonate");
        PortalSpawnHandler.recreatePortal(world);
        PortalSpawnHandler.createPortal(world, player, distant, index);

        for (MobWaveDescripton mobsInfo : waverNumberListMobs) {
            int quantityWeight = (int) Math.floor(maxMobs * mobsInfo.getQuantity() / totalMobs);
            if (quantityWeight == 0) {
                quantityWeight = 1;
            }

            List<UUID> create = MobCreate.spawnMobs(world, player, mobsInfo.getMobName(),
                    quantityWeight, distant, index);
            currentWave.addAll(create);
        }
        StatusBarRenderer.updatePlayerStatus(player.getUUID(), currentWave.size(), maxMobs - 1);

        String playerName = player.getName().getString();
        currentWaveMobsPerPlayer.put(playerName, currentWave);

    }

    public void waveMobCheck(ServerPlayer player, ServerLevel world, Integer waverNumber, int maxMobs) {
        String playerName = player.getName().getString();
        List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);
        message(player, currentWave.size() + " mobs");
        System.out.println("Wave mobs: " + currentWave.size());
        StatusBarRenderer.updatePlayerStatus(player.getUUID(), currentWave.size(), maxMobs - 1);

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

    public void end(ServerLevel world) {
        if (world != null) {
            PortalSpawnHandler.recreatePortal(world);
            if (currentWaveMobsPerPlayer != null) {
                currentWaveMobsPerPlayer.clear();
                Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
                for (ServerPlayer player : players) {
                    StatusBarRenderer.updatePlayerStatus(player.getUUID(), 0, 0);
                    sendTitleMessage(player, "The night ends", 5, 40, 10);
                    sound(player, world, "entity_creeper_hurt");
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
