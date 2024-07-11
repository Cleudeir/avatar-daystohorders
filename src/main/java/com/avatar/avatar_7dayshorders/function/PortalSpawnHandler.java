package com.avatar.avatar_7dayshorders.function;

import java.util.HashMap;
import java.util.Map;

import com.avatar.avatar_7dayshorders.server.ServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalSpawnHandler {

    private static Map<BlockPos, BlockState> portal = new HashMap<>();

    static {
        portal = ServerConfig.loadPortalBlocks();
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
                    if (blockState.getBlock() != Blocks.AIR) {
                        portal.put(newPos, blockState);
                    }
                    world.setBlock(newPos, frameState, 3);
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
        DestroyBlockConstruction(index, portalPos, world);
        world.playSound(null, portalPos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
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
