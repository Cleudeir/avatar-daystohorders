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

    private static final int GRID_SIZE = 3;

    public static double calculateFallTime(double height) {
        final double gravity = 32;
        return Math.sqrt(2 * height / gravity);
    }

    private static void spawnFallingMagmaBlock(ServerLevel world, BlockPos pos) {
        // Create the BlockState for the falling block (in this case, Magma Block)
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
        BlockState frameState = Blocks.AIR.defaultBlockState();
        for (int k = 0; k <= index; k++) {
            for (int i = -index + k; i <= index - k; i++) {
                for (int j = -index + k; j <= index - k; j++) {
                    int portalX = portalPos.getX() + i;
                    int portalY = portalPos.getY() - k;
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
        int height = 30; // Example height in meters
        int BlockX = portalPos.getX();
        int BlockY = portalPos.getY() + height;
        int BlockZ = portalPos.getZ();
        BlockPos portalPos2 = new BlockPos(BlockX, BlockY, BlockZ);
        spawnFallingMagmaBlock(world, portalPos2);
    }

    public static void createPortal(ServerLevel world, Player player, int distant, int index) {
        BlockPos pos = player.blockPosition().below();
        int x = pos.getX() - distant - (int) (index / 2);
        int z = pos.getZ() - distant;
        int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos portalPos = new BlockPos(x, y, z);
        DestroyBlockConstruction(index, portalPos, world);
        world.playSound(null, portalPos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
        BlockState state = Blocks.LIGHT.defaultBlockState();
        world.setBlock(portalPos, state, 3);
        world.playSound(null, portalPos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.0F);
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
