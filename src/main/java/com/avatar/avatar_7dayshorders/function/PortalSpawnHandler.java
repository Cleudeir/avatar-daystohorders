package com.avatar.avatar_7dayshorders.function;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalSpawnHandler {

    private static List<BlockPos> portal = new ArrayList<>();

    public static void blockConstruction(int index, BlockPos portalPos, ServerLevel world) {
        BlockState frameState = Blocks.POWDER_SNOW.defaultBlockState();
        for (int i = 1; i < index * 2; i++) {
            for (int j = -index; j < index; j++) {
                int portalX = portalPos.getX() + i;
                int portalY = portalPos.getY() + index;
                int portalZ = portalPos.getZ() + j;
                BlockPos newPos = new BlockPos(portalX, portalY, portalZ);
                portal.add(newPos);
                world.setBlock(newPos, frameState, 3);
            }
        }
    }

    public static void createPortal(ServerLevel world, Player player, int distant) {
        BlockPos pos = player.blockPosition().below();
        int index = 4;
        int x = pos.getX() - distant - (int) (index / 2);
        int z = pos.getZ() - distant;
        int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos portalPos = new BlockPos(x, y, z);
        blockConstruction(index, portalPos, world);
        world.playSound(null, portalPos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void destroyPortal(ServerLevel world) {
        if (portal.size() > 0) {
            for (BlockPos pos : portal) {
                world.destroyBlock(pos, false);
            }
        }
        portal.clear();
    }
}
