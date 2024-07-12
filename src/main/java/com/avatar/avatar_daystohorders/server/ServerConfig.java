package com.avatar.avatar_daystohorders.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class ServerConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.ConfigValue<List<String>> CURRENT_WAVE_MOBS_PER_PLAYER;
    public static ForgeConfigSpec.ConfigValue<List<String>> PORTAL_BLOCKS;

    static {
        setupConfig();
    }

    private static void setupConfig() {

        BUILDER.comment("Current wave mobs per player").push("currentWaveMobsPerPlayer");
        CURRENT_WAVE_MOBS_PER_PLAYER = BUILDER
                .comment("Current wave mobs per player")
                .define("default", new ArrayList<String>());
        BUILDER.pop();
        BUILDER.comment("Portal blocks destroyed when create new wave").push("portalBlocks");
        PORTAL_BLOCKS = BUILDER
                .comment("Portal blocks destroyed when create new wave")
                .define("default", new ArrayList<String>());
        BUILDER.pop();

        CONFIG = BUILDER.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG);
    }

    public static List<String> serializeCurrentWaveMobsPerPlayer(Map<String, List<UUID>> currentWaveMobsPerPlayer) {
        List<String> ListSerialized = new ArrayList<>();
        for (Map.Entry<String, List<UUID>> entry : currentWaveMobsPerPlayer.entrySet()) {
            String key = entry.getKey();
            List<UUID> value = entry.getValue();
            String numberString = value.stream().map(String::valueOf)
                    .collect(Collectors.joining(","));
            String serialized = key + ":" + numberString;
            ListSerialized.add(serialized);
        }
        return ListSerialized;
    }

    public static Map<String, List<UUID>> deserializeCurrentWaveMobsPerPlayer(List<String> ListSerialized) {
        Map<String, List<UUID>> currentWaveMobsPerPlayer = new HashMap<>();
        for (String entry : ListSerialized) {
            if (entry.isEmpty()) {
                continue;
            }
            String[] split = entry.split(":");
            if (split.length < 2) {
                continue;
            }
            String key = split[0];
            String[] array = split[1].split(",");
            List<UUID> value = Arrays.stream(array)
                    .filter(x -> !x.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            currentWaveMobsPerPlayer.put(key, value);
        }
        return currentWaveMobsPerPlayer;
    }

    public static List<String> serializeBlockPosMap(Map<BlockPos, BlockState> map) {
        List<String> listBlockPos = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> entry : map.entrySet()) {
            BlockPos blockPos = entry.getKey();
            BlockState state = entry.getValue();
            String blockName = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
            String stringBlockPos = blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ() + "," + blockName;
            listBlockPos.add(stringBlockPos);
        }
        return listBlockPos;
    }

    private static Map<BlockPos, BlockState> deserializeBlockPosMap(List<String> mapBlockPos) {
        Map<BlockPos, BlockState> map = new HashMap<>();
        for (String entry : mapBlockPos) {
            try {
                String[] split = entry.split(",");
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                int z = Integer.parseInt(split[2]);
                String blockName = split[3];

                @Nullable
                BlockState blockState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName))
                        .defaultBlockState();
                if (blockState == null) {
                    throw new IllegalArgumentException("Invalid block name: " + blockName);
                }

                System.out.println(blockState);
                BlockPos blockPos = new BlockPos(x, y, z);
                map.put(blockPos, blockState);
            } catch (Exception e) {
                System.err.println("Error processing entry: " + entry);
                e.printStackTrace();
            }
        }
        return map;
    }

    public static void saveCurrentWaveMobsPerPlayer(Map<String, List<UUID>> currentWaveMobsPerPlayer) {
        if (!CONFIG.isLoaded()) {
            return;
        }
        CURRENT_WAVE_MOBS_PER_PLAYER.set(serializeCurrentWaveMobsPerPlayer(currentWaveMobsPerPlayer));
        CONFIG.save();
    }

    public static void savePortalBlocks(Map<BlockPos, BlockState> portalBlocks) {
        if (!CONFIG.isLoaded()) {
            return;
        }
        PORTAL_BLOCKS.set(serializeBlockPosMap(portalBlocks));
        CONFIG.save();
    }

    public static List<UUID> loadPlayerMobs(String PlayerName) {
        Map<String, List<UUID>> data = new HashMap<>();
        if (CONFIG.isLoaded()) {
            data = deserializeCurrentWaveMobsPerPlayer(CURRENT_WAVE_MOBS_PER_PLAYER.get());
            System.out.println("Data loaded from config");
        }
        if (!data.containsKey(PlayerName)) {
            return new ArrayList<>();
        }
        return data.get(PlayerName);
    }

    public static Map<BlockPos, BlockState> loadPortalBlocks() {
        // Load the config if not already loaded
        Map<BlockPos, BlockState> portalBlocks = new HashMap<>();
        if (CONFIG.isLoaded()) {
            // Retrieve data from config
            portalBlocks = deserializeBlockPosMap(PORTAL_BLOCKS.get());
            System.out.println("Data loaded from config");
        }
        return portalBlocks;
    }

}
