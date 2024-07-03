package com.avatar.avatar_7dayshorders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class GlobalConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;

    // Define your config values here
    public static ForgeConfigSpec.ConfigValue<List<String>> BROKEN_BLOCKS;
    public static ForgeConfigSpec.ConfigValue<List<String>> AROUND_BLOCKS;
    public static ForgeConfigSpec.ConfigValue<List<String>> PERIMETER_BLOCKS;

    public static ForgeConfigSpec.ConfigValue<String> PERIOD_WEAVE;
    // Initialize config values without static initializer block
    static {
        setupConfig();
    }

    private static void setupConfig() {
        // PERIOD_WEAVE
        BUILDER.comment("Period Weave").push("periodWeave");
        PERIOD_WEAVE = BUILDER
                .comment("Default period weave")
                .define("default", "-1");
        CONFIG = BUILDER.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
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

    // Method to save data
    public static void savePeriodWeave(int periodWeave) {
        String periodWeaveString = String.valueOf(periodWeave);
        PERIOD_WEAVE.set(periodWeaveString);
        CONFIG.save();
    }

    public static int loadPeriodWeave() {
        int data = 7;
        if (CONFIG.isLoaded()) {
            int remember = Integer.valueOf(PERIOD_WEAVE.get());
            if (remember == -1) {
                savePeriodWeave(data);
            } else {
                data = remember;
            }
        }
        return data;
    }
}
