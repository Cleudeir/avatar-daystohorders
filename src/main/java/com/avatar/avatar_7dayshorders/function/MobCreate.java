package com.avatar.avatar_7dayshorders.function;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

public class MobCreate { 
    public static List<Integer> spawnMobs(ServerLevel world, Player player, String mobName, int quantity) {
        List<Integer> currentWave = new ArrayList<>();
        @Nullable
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobName));
        if (entityType != null) {
            for (int i = 0; i < quantity; i++) {
                Entity entity = entityType.create(world);
                if (entity instanceof Mob) {
                    Mob mob = (Mob) entity;
                    spawnAndTrack(world, mob, player);
                    currentWave.add(mob.getId());
                }
            }
        }
        return currentWave;
    }
    public static void spawnAndTrack(ServerLevel world, Mob entity, Player player) {
        int distante = 30;
        double x = player.getX() + world.random.nextInt(30) - distante;
        double z = player.getZ() + world.random.nextInt(30) - distante;
        double y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
        entity.setPos(x, y, z);
        Mob mob = (Mob) entity;
        mob.setTarget(player);
        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9999));       
        if (mob instanceof Skeleton) {         
            Skeleton skeleton = (Skeleton) mob;
            if (!skeleton.isHolding(Items.BOW)) {
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }
            if (skeleton.getOffhandItem().isEmpty() || skeleton.getOffhandItem().getItem() != Items.ARROW) {
                skeleton.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ARROW, 64));
            }
        }
        world.addFreshEntity(entity);
    }
}
