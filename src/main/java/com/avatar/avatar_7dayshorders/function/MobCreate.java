package com.avatar.avatar_7dayshorders.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

public class MobCreate {

    private static final Map<String, Integer> MOB_DIFFICULTY = Collections.unmodifiableMap(
            new HashMap<String, Integer>() {
                {
                    put("minecraft:zombie", 30);
                    put("minecraft:skeleton", 35);
                    put("minecraft:creeper", 40);
                    put("minecraft:spider", 25);
                    put("minecraft:enderman", 50);
                    put("minecraft:endermite", 5);
                    put("minecraft:cave_spider", 30);
                    put("minecraft:witch", 60);
                    put("minecraft:blaze", 55);
                    put("minecraft:ghast", 70);
                    put("minecraft:slime", 20);
                    put("minecraft:magma_cube", 45);
                    put("minecraft:phantom", 65);
                    put("minecraft:vindicator", 75);
                    put("minecraft:evoker", 80);
                    put("minecraft:ravager", 90);
                    put("minecraft:husk", 30);
                    put("minecraft:stray", 35);
                    put("minecraft:drowned", 40);
                    put("minecraft:guardian", 70);
                    put("minecraft:elder_guardian", 90);
                    put("minecraft:shulker", 50);
                    put("minecraft:illusioner", 75);
                    put("minecraft:pillager", 55);
                    put("minecraft:vex", 85);
                }
            });

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

        double x = player.getX() + world.random.nextInt(30) - 10;
        double z = player.getZ() + world.random.nextInt(30) - 10;
        double y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);

        entity.setPos(x, y, z);

        Mob mob = (Mob) entity;
        mob.setTarget(player);
        // mob.setHealth(100);
        // mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200));
        mob.isOnFire();

        if (mob instanceof Skeleton) {
            // Equip the skeleton with a bow and arrows
            Skeleton skeleton = (Skeleton) mob;

            // Check if the skeleton doesn't already have a bow (optional)
            if (!skeleton.isHolding(Items.BOW)) {
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW)); // Equip a bow
            }

            // Check if the skeleton doesn't already have arrows (optional)
            if (skeleton.getOffhandItem().isEmpty() || skeleton.getOffhandItem().getItem() != Items.ARROW) {
                skeleton.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ARROW, 64)); // Give arrows
            }
        }

        // Spawn the entity and track it
        world.addFreshEntity(entity);
        // currentWaveMobs.add(entity);
    }
}
