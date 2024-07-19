package com.avatar.avatar_daystohorders.function;

import java.util.HashMap;
import java.util.Map;

import com.avatar.avatar_daystohorders.server.ServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalSpawnHandler {

    private static Map<BlockPos, BlockState> portal = new HashMap<>();

    static {
        portal = ServerConfig.loadPortalBlocks();
    }

    private static final int GRID_SIZE = 4;

    public static double calculateFallTime(double height) {
        final double gravity = 32;
        return Math.sqrt(2 * height / gravity);
    }

    private static void spawnFallingMagmaBlock(ServerLevel world, BlockPos portalPos) {
        // Create the BlockState for the falling block (in this case, Magma Block)
        int height = 30; // Example height in meters
        int BlockX = portalPos.getX();
        int BlockY = portalPos.getY() + height;
        int BlockZ = portalPos.getZ();
        BlockPos pos = new BlockPos(BlockX, BlockY, BlockZ);

        BlockState flameState = Blocks.FIRE.defaultBlockState();

        // Create a 4x4x4 grid of falling blocks
        for (int x = -GRID_SIZE; x <= GRID_SIZE; x++) {
            for (int y = -GRID_SIZE; y <= GRID_SIZE; y++) {
                for (int z = -GRID_SIZE; z <= GRID_SIZE; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= GRID_SIZE) {
                        BlockPos blockPos = pos.offset(x, y, z);
                        // Create a new FallingBlockEntity using the fall method
                        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(world, blockPos, flameState);

                        // Add the falling block entity to the world
                        world.addFreshEntity(fallingBlock);

                        // Add flame on top of the falling block
                        BlockPos flamePos = blockPos.above();
                        world.setBlock(flamePos, flameState, 3);
                    }
                }
            }
        }
    }

    public static void DestroyBlockConstruction(int index, BlockPos portalPos, ServerLevel world) {
        BlockState frameState = Blocks.NETHERRACK.defaultBlockState();
        BlockState airState = Blocks.AIR.defaultBlockState();
        for (int k = 0; k < index; k++) {
            int portalY = portalPos.getY() - k;
            for (int i = -index + k; i <= index - k; i++) {
                int portalX = portalPos.getX() + i;
                for (int j = -index + k; j <= index - k; j++) {
                    int portalZ = portalPos.getZ() + j;
                    BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
                    BlockState blockState = world.getBlockState(newPos);
                    if (blockState.getBlock() == Blocks.BEDROCK) {
                        break;
                    }
                    portal.put(newPos, blockState);
                    world.setBlock(newPos, airState, 3);
                }
            }
        }

        for (int k = 0; k < index + 3; k++) {
            int portalY = portalPos.getY() - k - 1;
            for (int i = -index + k; i <= index - k; i++) {
                int portalX = portalPos.getX() + i;
                for (int j = -index + k; j <= index - k; j++) {
                    if (i == -index + k || i == index - k) {
                        int portalZ = portalPos.getZ() + j;
                        BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
                        BlockState blockState = world.getBlockState(newPos);
                        if (blockState.getBlock() == Blocks.BEDROCK) {
                            break;
                        }
                        portal.put(newPos, blockState);
                        world.setBlock(newPos, frameState, 3);
                    }
                    if (j == -index + k || j == index - k) {
                        int portalZ = portalPos.getZ() + j;
                        BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
                        BlockState blockState = world.getBlockState(newPos);
                        if (blockState.getBlock() == Blocks.BEDROCK) {
                            break;
                        }
                        portal.put(newPos, blockState);
                        world.setBlock(newPos, frameState, 3);
                    }
                }
            }
        }

    }

    public static void createPortal(ServerLevel world, Player player, int distant, int index) {
        BlockPos pos = player.blockPosition().below();
        int x = pos.getX() - distant - (int) (index / 2);
        int z = pos.getZ() - distant;
        int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos portalPos = new BlockPos(x, y, z);
        BlockPos portalFloor = new BlockPos(x, y - 1, z);
        BlockState BlockPortalPos = world.getBlockState(portalFloor);
        if (BlockPortalPos.getBlock() != Blocks.WATER) {
            DestroyBlockConstruction(index, portalPos, world);
            spawnFallingMagmaBlock(world, portalPos);
            world.playSound(null, portalPos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

    }

    public static void recreatePortal(ServerLevel world) {
        if (portal.size() > 0) {
            for (BlockPos pos : portal.keySet()) {
                BlockState state = portal.get(pos);
                world.setBlock(pos, state, 3);
            }
        }
        portal.clear();
        ServerConfig.savePortalBlocks(portal);
    }

    public static void savePortalBlock() {
        ServerConfig.savePortalBlocks(portal);
    }
}
