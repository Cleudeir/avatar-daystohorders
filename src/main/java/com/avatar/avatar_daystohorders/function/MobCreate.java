package com.avatar.avatar_daystohorders.function;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

public class MobCreate {
    public static List<UUID> spawnMobs(ServerLevel world, Player player, String mobName, int quantity, int distant,
            int index) {
        List<UUID> currentWave = new ArrayList<>();
        @Nullable
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobName));
        if (entityType != null) {
            for (int i = 0; i < quantity; i++) {
                Entity entity = entityType.create(world);

                if (entity instanceof Mob) {
                    Mob mob = (Mob) entity;
                    double x = player.getX() - distant - (int) (index / 2);
                    double z = player.getZ() - distant;
                    double y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z) + 3;
                    double height = mob.getBbHeight();
                    BlockPos blockPos = new BlockPos((int) x, (int) y, (int) z);
                    BlockPos blockPosHeight = new BlockPos((int) x, (int) y + (int) height, (int) z);
                    BlockPos blockFloor = new BlockPos((int) x, (int) y - 4, (int) z);
                    BlockState blockState = world.getBlockState(blockPos);
                    BlockState blockStateHeight = world.getBlockState(blockPosHeight);
                    BlockState blockFloorState = world.getBlockState(blockFloor);
                    System.out.println(blockState + " " + blockStateHeight);
                    if (blockState.isAir() && blockStateHeight.isAir() && blockState.getBlock() != Blocks.WATER
                            && blockFloorState.getBlock() != Blocks.WATER) {
                        mob.setPos(x, y, z);
                        mob.setTarget(player);
                        mob.addTag("avatar_daystohorders_mob");
                        mob.canSprint();
                        mob.setPersistenceRequired();
                        mob.setSwimming(true);
                        // mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 9999, 1));
                        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9999, 4));
                        addItem(mob);
                        world.addFreshEntity(mob);
                        currentWave.add(mob.getUUID());
                    }
                }
            }
        }
        return currentWave;
    }

    private static void addItem(Mob mob) {
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        if (mob instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) mob;
            if (!skeleton.isHolding(Items.BOW)) {
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }
            if (skeleton.getOffhandItem().isEmpty() || skeleton.getOffhandItem().getItem() != Items.ARROW) {
                skeleton.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ARROW, 64));
            }
        }
    }

}
