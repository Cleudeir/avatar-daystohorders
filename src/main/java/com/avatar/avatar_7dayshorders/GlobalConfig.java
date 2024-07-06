package com.avatar.avatar_7dayshorders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.avatar.avatar_7dayshorders.function.MobWeaveDescripton;

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
    public static ForgeConfigSpec.ConfigValue<List<String>> LIST_MOBS_PER_WEAVE;

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

        BUILDER.comment("Mobs Per Weave").push("mobsPerWeave");
        LIST_MOBS_PER_WEAVE = BUILDER.comment(
                "Default mobs per weave table data, example: \"minecraft:zombie,1,0,0\" = \"mobName,quantity,startWeave,endWeave,endWeave\" if 0 = infinity")
                .define("default", new ArrayList<String>());

        BUILDER.pop();
        CONFIG = BUILDER.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
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

    public static List<String> serializeMobsList(List<MobWeaveDescripton> list) {
        List<String> ListSerialized = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MobWeaveDescripton mobsInfo = list.get(i);
            String serialized = mobsInfo.getMobName() + "," + mobsInfo.getQuantity() + ","
                    + mobsInfo.getStartWeave()
                    + "," + mobsInfo.getEndWeave();
            ListSerialized.add(serialized);
        }
        return ListSerialized;
    }

    public static List<MobWeaveDescripton> deserializeMobsList(List<String> ListSerialized) {
        List<MobWeaveDescripton> list = new ArrayList<>();
        for (String entry : ListSerialized) {
            String[] split = entry.split(",");
            String mobName = split[0];
            int quantity = Integer.parseInt(split[1]);
            int startWeave = Integer.parseInt(split[2]);
            int endWeave = Integer.parseInt(split[3]);
            MobWeaveDescripton mobsInfo = new MobWeaveDescripton(mobName, quantity, startWeave, endWeave);
            list.add(mobsInfo);
        }
        return list;
    }

    public static List<MobWeaveDescripton> getListMobs(Integer weaveNumber) {
        List<MobWeaveDescripton> data = new ArrayList<>();
        if (CONFIG.isLoaded()) {
            data = deserializeMobsList(LIST_MOBS_PER_WEAVE.get());
            if (data.isEmpty()) {
                ArrayList<String> defaultMobsPerWeave = new ArrayList<String>() {
                    {
                        add("minecraft:zombie,1,2,0");
                        add("minecraft:skeleton,1,2,0");
                        add("minecraft:creeper,1,2,0");
                        add("minecraft:spider,1,3,0");
                        add("minecraft:enderman,1,4,0");
                        add("minecraft:endermite,1,5,0");
                        add("minecraft:cave_spider,1,6,0");
                        add("minecraft:witch,1,7,0");
                        add("minecraft:blaze,1,8,0");
                        add("minecraft:ghast,1,9,0");
                        add("minecraft:slime,1,10,0");
                        add("minecraft:magma_cube,1,11,0");
                        add("minecraft:phantom,1,12,0");
                        add("minecraft:vindicator,1,13,0");
                        add("minecraft:evoker,1,1,0");
                        add("minecraft:ravager,1,15,0");
                        add("minecraft:husk,1,16,0");
                        add("minecraft:stray,1,17,0");
                        add("minecraft:drowned,18,2,0");
                        add("minecraft:guardian,1,19,0");
                        add("minecraft:elder_guardian,1,20,0");
                        add("minecraft:shulker,1,21,0");
                        add("minecraft:illusioner,1,22,0");
                        add("minecraft:pillager,1,23,0");
                        add("minecraft:vex,1,24,0");
                    };
                };
                LIST_MOBS_PER_WEAVE.set(defaultMobsPerWeave);
                CONFIG.save();
            }
            data = data.stream()
                    .filter(x -> x.getStartWeave() <= weaveNumber
                            && (x.getEndWeave() >= weaveNumber || x.getEndWeave() == 0))
                    .collect(Collectors.toList());
            System.out.println("Data loaded from config");
        }
        return data;
    }
}
