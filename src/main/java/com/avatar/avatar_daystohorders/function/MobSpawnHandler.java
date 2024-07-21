package com.avatar.avatar_daystohorders.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import org.joml.Random;

import com.avatar.avatar_daystohorders.GlobalConfig;
import com.avatar.avatar_daystohorders.Main;
import com.avatar.avatar_daystohorders.Client.StatusBarRenderer;
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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobSpawnHandler {

    private static final Map<String, List<UUID>> currentWaveMobsPerPlayer = new HashMap<>();

    public static void sendTitleMessage(ServerPlayer player, String title, int fadeIn, int stay,
            int fadeOut) {

        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
    }

    public static void message(ServerPlayer player, String message) {
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
            String playerName = player.getName().getString();
            List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);
            if (currentWave == null) {
                currentWave = ServerConfig.loadPlayerMobs(playerName);
                if (currentWave == null || currentWave.isEmpty()) {
                    sendTitleMessage(player, "The night starts", 5, 40, 10);
                    createWave(player, world, waverNumber, waverNumberListMobs, totalMobs, maxMobs);
                } else {
                    currentWaveMobsPerPlayer.put(playerName, currentWave);
                    StatusBarRenderer.updatePlayerStatus(player.getUUID(), currentWave.size(), maxMobs);
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

        int distant = 10 + (int) Math.ceil(Math.random() * 10);
        // distant = 0;
        int index = 4;

        sound(player, world, "bell_resonate");
        PortalSpawnHandler.recreatePortal(world);

        for (int i = 0; i < waverNumberListMobs.size(); i++) {
            MobWaveDescripton mobsInfo = waverNumberListMobs.get(i);
            int quantityWeight = (int) Math.ceil(maxMobs * mobsInfo.getQuantity() / totalMobs);
            if (quantityWeight == 0) {
                quantityWeight = 1;
            }
            if (currentWave.size() + quantityWeight > maxMobs) {
                quantityWeight = maxMobs - currentWave.size() + 1;
            }
            if (i == waverNumberListMobs.size() - 1) {
                quantityWeight = maxMobs - currentWave.size();
            }
            if (quantityWeight > 0) {
                List<UUID> create = MobCreate.spawnMobs(world, player, mobsInfo.getMobName(),
                        quantityWeight, distant, index);
                currentWave.addAll(create);
            }
            if (i == waverNumberListMobs.size() - 1 && currentWave.size() > 0) {
                PortalSpawnHandler.createPortal(world, player, distant, index);
            }

        }

        StatusBarRenderer.updatePlayerStatus(player.getUUID(), currentWave.size(), maxMobs);

        String playerName = player.getName().getString();
        currentWaveMobsPerPlayer.put(playerName, currentWave);

    }

    public void waveMobCheck(ServerPlayer player, ServerLevel world, Integer waverNumber, int maxMobs) {
        String playerName = player.getName().getString();
        List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);
        StatusBarRenderer.updatePlayerStatus(player.getUUID(), currentWave.size(), maxMobs);

        for (int i = 0; i < currentWave.size(); i++) {
            UUID mobId = currentWave.get(i);
            Mob mob = (Mob) world.getEntity(mobId);
            if (mob != null) {
                Boolean mobIsAlive = mob.isAlive();
                if (mobIsAlive) {
                    mob.setTarget(player);

                    // MobBlockPlaceHandler.onMobUpdate(world, player, mob);

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
                    giveRandomItem(player);
                }
            }
        }
    }

    private void giveRandomItem(ServerPlayer player) {
        List<ItemStack> rareItems = GlobalConfig.getRareItems();
        Random random = new Random();
        ItemStack randomItem = rareItems.get(random.nextInt(rareItems.size()));
        player.addItem(randomItem);
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
    }

}
