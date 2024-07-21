package com.avatar.avatar_daystohorders.function;

import com.avatar.avatar_daystohorders.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class MobBlockPlaceHandler {

    private static final int PLACE_RADIUS = 5; // Radius around the player where blocks can be placed

    public static void onMobUpdate(ServerLevel world, ServerPlayer player, Mob mob) {
        System.out.println("Mob is close to player" + mob.distanceTo(player));
        if (mob.distanceTo(player) > PLACE_RADIUS) {
            BlockPos playerPos = player.blockPosition();
            BlockPos placePos = findSuitablePlacePosition(world, playerPos, mob);
            BlockPos floorPos = placePos.below();
            BlockState blockState = Blocks.COBBLESTONE.defaultBlockState();
            BlockState checkBlockState = world.getBlockState(placePos);
            BlockState checkBlockStateFloor = world.getBlockState(floorPos);
            boolean entityExists = false;
            VoxelShape shape = checkBlockState.getShape(world, placePos);
            VoxelShape shapeFloor = checkBlockStateFloor.getShape(world, floorPos);
            if (shape != null && !shape.isEmpty() && shapeFloor != null && !shapeFloor.isEmpty()) {
                entityExists = world.getEntities(null, shape.bounds().move(placePos)).size() > 0;
                entityExists = world.getEntities(null, shapeFloor.bounds().move(floorPos)).size() > 0;
            }
            if (checkBlockState.isAir() && !entityExists) {
                world.setBlockAndUpdate(placePos, blockState);
            }
        }
    }

    private static BlockPos findSuitablePlacePosition(ServerLevel world, BlockPos playerPos, Mob mob) {
        int playerX = (int) playerPos.getX();
        int playerY = (int) playerPos.getY();
        int playerZ = (int) playerPos.getZ();
        // position mob
        int mobX = (int) mob.getX();
        int mobY = (int) mob.getY();
        int mobZ = (int) mob.getZ();
        // difference
        int xDiff = playerX - mobX;
        int yDiff = playerY - mobY;
        int zDiff = playerZ - mobZ;
        // block break
        int blockX = mobX;
        int blockY = mobY;
        int blockZ = mobZ;

        if (Math.abs(xDiff) > Math.abs(yDiff) && Math.abs(xDiff) > Math.abs(zDiff)) {
            blockX += xDiff > 0 ? 1 : -1;
        } else if (Math.abs(yDiff) > Math.abs(xDiff) && Math.abs(yDiff) > Math.abs(zDiff)) {
            blockY += yDiff > 0 ? 1 : -1;
        } else if (Math.abs(zDiff) > Math.abs(xDiff) && Math.abs(zDiff) > Math.abs(yDiff)) {
            blockZ += zDiff > 0 ? 1 : -1;
        }

        return new BlockPos(blockX, blockY, blockZ);
    }

}
