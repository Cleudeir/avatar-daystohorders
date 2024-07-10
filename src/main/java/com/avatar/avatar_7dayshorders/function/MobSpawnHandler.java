package com.avatar.avatar_7dayshorders.function;

import java.util.ArrayList;
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

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobSpawnHandler {

    private static final Map<String, List<UUID>> currentWaveMobsPerPlayer = new HashMap<>();
    private static List<BlockPos> portal = new ArrayList<>();

    public void start(ServerLevel world, Integer weaverNumber) {
        List<MobWeaveDescripton> weaverNumberListMobs = GlobalConfig.getListMobs(weaverNumber);
        Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
        for (ServerPlayer player : players) {
            String playerName = player.getName().getString();
            List<UUID> currentWave = currentWaveMobsPerPlayer.get(playerName);
            if (currentWave == null) {
                currentWave = ServerConfig.loadPlayerMobs(playerName);
                currentWaveMobsPerPlayer.put(playerName, currentWave);
                player.sendSystemMessage(
                        Component.translatable("The night starts, the mobs are incoming!"));
                world.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE,
                        SoundSource.HOSTILE, 1.0F, 1.0F);
            } else if (currentWave.isEmpty()) {
                int distant = 20 + world.random.nextInt(10);
                if (portal.size() > 0) {
                    destroyNetherPortal(world);
                }
                portal = createCastle(world, player, distant);
                player.sendSystemMessage(
                        Component.translatable("Start new Weave!"));
                world.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE,
                        SoundSource.HOSTILE, 1.0F, 1.0F);
                for (MobWeaveDescripton mobsInfo : weaverNumberListMobs) {
                    System.out.println("mobsInfo >>>>>>> " + mobsInfo.getMobName() + " " + mobsInfo.getQuantity());
                    List<UUID> create = MobCreate.spawnMobs(world, player, mobsInfo.getMobName(),
                            mobsInfo.getQuantity(), distant);
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
        if (portal.size() > 0) {
            destroyNetherPortal(world);
        }
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
                    Boolean mobIsAlive = mob.isAlive();
                    if (mobIsAlive && mob.getTarget() == null) {
                        mob.setTarget(player);
                        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9999));
                    }
                    Animate.portal(world, mob.getX(), mob.getY(), mob.getZ());
                    mobTeleport(mob, world, player);
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

    private static List<BlockPos> blockConstruction(int add, BlockPos portalPos, ServerLevel world, int index) {
        List<BlockPos> frame = new ArrayList<>();
        BlockState portalState = Blocks.STONE_BRICKS.defaultBlockState();
        for (int i = 0; i <= add; i++) {
            int portalX = portalPos.getX() + i;
            int portalY = portalPos.getY();
            int portalZ = portalPos.getZ() + index;
            BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
            world.setBlock(newPos, portalState, 3);
            frame.add(newPos);
        }
        for (int i = 0; i <= add; i++) {
            int portalX = portalPos.getX() + add;
            int portalY = portalPos.getY() + i;
            int portalZ = portalPos.getZ() + index;
            BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
            world.setBlock(newPos, portalState, 3);
            frame.add(newPos);
        }
        for (int i = 0; i <= add; i++) {
            int portalX = portalPos.getX();
            int portalY = portalPos.getY() + i;
            int portalZ = portalPos.getZ() + index;
            BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
            world.setBlock(newPos, portalState, 3);
            frame.add(newPos);
        }
        for (int i = 0; i <= add; i++) {
            int portalX = portalPos.getX() + i;
            int portalY = portalPos.getY() + add;
            int portalZ = portalPos.getZ() + index;
            BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
            world.setBlock(newPos, portalState, 3);
            frame.add(newPos);
        }
        // Create the portal frame
        BlockState frameState = Blocks.LIGHT.defaultBlockState();
        for (int i = 1; i < add; i++) {
            for (int j = 1; j < add; j++) {
                int portalX = portalPos.getX() + i;
                int portalY = portalPos.getY() + j;
                int portalZ = portalPos.getZ() + index;
                BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
                world.setBlock(newPos, frameState, 3);
                frame.add(newPos);
            }
        }
        return frame;
    }

    private static List<BlockPos> createCastle(ServerLevel world, Player player, int distant) {
        // Create the Nether portal
        BlockPos pos = player.blockPosition().below(); // Get the block position below the player
        int add = 4;
        int x = pos.getX() - (int) distant - (add / 2);
        int z = pos.getZ() - distant;
        int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        // Create the portal frame
        BlockPos portalPos = new BlockPos(x, y, z);
        List<BlockPos> frame = new ArrayList<>();
        for (int i = 0; i <= add; i++) {
            frame.addAll(blockConstruction(add, portalPos, world, i));
        }
        world.playSound(null, portalPos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return frame;
    }

    private static void destroyNetherPortal(ServerLevel world) {
        for (BlockPos pos : portal) {
            world.destroyBlock(pos, false);
        }
        portal.clear();
    }
}
