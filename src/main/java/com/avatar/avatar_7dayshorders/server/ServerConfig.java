package com.avatar.avatar_7dayshorders.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;

    // Define your config values here
    public static ForgeConfigSpec.ConfigValue<List<String>> BROKEN_BLOCKS;
    public static ForgeConfigSpec.ConfigValue<List<String>> AROUND_BLOCKS;
    public static ForgeConfigSpec.ConfigValue<List<String>> PERIMETER_BLOCKS;
    public static ForgeConfigSpec.ConfigValue<String> MAIN_BLOCK_POS;
    // Initialize config values without static initializer block
    static {
        setupConfig();
    }

    private static void setupConfig() {
        BUILDER.comment("Broken Blocks Data").push("brokenBlocks");
        BROKEN_BLOCKS = BUILDER
                .comment("Default broken blocks data")
                .define("default", new ArrayList<String>());
        BUILDER.pop();

        BUILDER.comment("Around Blocks MainBlock Data").push("aroundBlocksMainBlock");
        AROUND_BLOCKS = BUILDER
                .comment("Default around blocks table data")
                .define("default", new ArrayList<String>());
        BUILDER.pop();

        BUILDER.comment("Perimeter Blocks MainBlock Data").push("perimeterBlocksMainBlock");
        PERIMETER_BLOCKS = BUILDER
                .comment("Default perimeter blocks table data")
                .define("default", new ArrayList<String>());
        BUILDER.pop();

        BUILDER.comment("Main Block Position").push("mainBlockPos");
        MAIN_BLOCK_POS = BUILDER
                .comment("Default main block position")
                .define("default", "0,0,0");
        BUILDER.pop();

        CONFIG = BUILDER.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG);
    }

    public static List<String> serializeBlockPosMap(Map<BlockPos, BlockState> list) {
        List<String> ListBlockPos = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> entry : list.entrySet()) {
            BlockPos blockPos = entry.getKey();
            String stringBlockPos = blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
            ListBlockPos.add(stringBlockPos);
        }
        return ListBlockPos;
    }

    private static Map<BlockPos, BlockState> deserializeBlockPosMap(List<String> MapBlockPos, ServerLevel world) {
        Map<BlockPos, BlockState> map = new HashMap<>();
        for (String entry : MapBlockPos) {
            String[] split = entry.split(",");
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);
            int z = Integer.parseInt(split[2]);
            BlockPos blockPos = new BlockPos(x, y, z);
            BlockState blockState = world.getBlockState(blockPos);
            map.put(blockPos, blockState);
        }
        return map;
    }

    private static BlockPos deserializeBlockPos(String BlockPos) {
        String[] split = BlockPos.split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        int z = Integer.parseInt(split[2]);
        BlockPos blockPos = new BlockPos(x, y, z);
        return blockPos;
    }

    // Method to save data
    public static void save(Map<BlockPos, BlockState> brokenBlocks,
            Map<BlockPos, BlockState> aroundBlocksMainBlock,
            Map<BlockPos, BlockState> perimeterBlocksMainBlock,
            BlockPos mainBlockPos) {
        List<String> brokenBlocksString = serializeBlockPosMap(brokenBlocks);
        List<String> aroundBlocksMainBlockString = serializeBlockPosMap(aroundBlocksMainBlock);
        List<String> perimeterBlocksMainBlockString = serializeBlockPosMap(perimeterBlocksMainBlock);
        String mainBlockPosString = mainBlockPos.getX() + "," + mainBlockPos.getY() + "," + mainBlockPos.getZ();
        BROKEN_BLOCKS.set(brokenBlocksString);
        AROUND_BLOCKS.set(aroundBlocksMainBlockString);
        PERIMETER_BLOCKS.set(perimeterBlocksMainBlockString);
        MAIN_BLOCK_POS.set(mainBlockPosString);
        CONFIG.save();
    }

    // Method to load data
    public static Map<BlockPos, BlockState> loadBrokenBlocks(ServerLevel world) {
        // Load the config if not already loaded
        Map<BlockPos, BlockState> brokenBlocksGet = new HashMap<>();
        if (CONFIG.isLoaded()) {
            // Retrieve data from config
            brokenBlocksGet = deserializeBlockPosMap(BROKEN_BLOCKS.get(), world);
            System.out.println("Data loaded from config");
        }
        return brokenBlocksGet;
    }

    public static Map<BlockPos, BlockState> loadAroundMainBlock(ServerLevel world) {
        // Load the config if not already loaded
        Map<BlockPos, BlockState> aroundBlocksMainBlockGet = new HashMap<>();
        if (CONFIG.isLoaded()) {
            // Retrieve data from config
            aroundBlocksMainBlockGet = deserializeBlockPosMap(AROUND_BLOCKS.get(), world);
            System.out.println("Data loaded from config");
        }
        return aroundBlocksMainBlockGet;
    }

    public static Map<BlockPos, BlockState> loadPerimeterMainBlock(ServerLevel world) {
        // Load the config if not already loaded
        Map<BlockPos, BlockState> perimeterBlocks = new HashMap<>();
        if (CONFIG.isLoaded()) {
            // Retrieve data from config
            perimeterBlocks = deserializeBlockPosMap(PERIMETER_BLOCKS.get(), world);
            System.out.println("Data loaded from config");
        }
        return perimeterBlocks;
    }

    public static BlockPos loadMainBlockPos(ServerLevel world) {
        // Load the config if not already loaded
        BlockPos mainBlock = new BlockPos(0, 0, 0);
        if (CONFIG.isLoaded()) {
            // Retrieve data from config
            mainBlock = deserializeBlockPos(MAIN_BLOCK_POS.get());
            System.out.println("Data loaded from config");
        }
        return mainBlock;
    }

}
