package com.avatar.avatar_7dayshorders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.avatar.avatar_7dayshorders.object.MobWeaveDescripton;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class GlobalConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;

    // Define your config values here
    public static ForgeConfigSpec.ConfigValue<List<String>> LIST_MOBS_PER_WEAVE;
    public static ForgeConfigSpec.ConfigValue<String> MAX_MOBS_PER_PLAYER;
    public static ForgeConfigSpec.ConfigValue<String> PERIOD_WEAVE;

    // Initialize config values without static initializer block
    static {
        setupConfig();
    }

    private static void setupConfig() {
        BUILDER.comment("Max mobs per player").push("maxMobsPerPlayer");
        MAX_MOBS_PER_PLAYER = BUILDER
                .comment("Max mobs per player")
                .define("default", "15");
        BUILDER.pop();

        BUILDER.comment("Period Weave").push("periodWeave");
        PERIOD_WEAVE = BUILDER
                .comment("Default period weave")
                .define("default", "7");
        BUILDER.pop();

        BUILDER.comment("Mobs Per Weave").push("mobsPerWeave");
        LIST_MOBS_PER_WEAVE = BUILDER.comment(
                "Default mobs per weave table data, example: \"minecraft:zombie,1,0,0\" = \"mobName,startWeave,endWeave,endWeave,weightOfMob\"\n if endWeave = 0 = infinity\n Quantity mob type per weave per player:\n A= Max mobs\nB= Weight of mob\n C= Sum of all Weight mobs this weave\n Calc: A * B / C")
                .define("default", new ArrayList<String>());

        BUILDER.pop();

        CONFIG = BUILDER.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
    }

    public static int loadPeriodWeave() {
        int data = 7;
        if (CONFIG.isLoaded()) {
            int remember = Integer.valueOf(PERIOD_WEAVE.get());
            data = remember;
        }
        return data;
    }

    public static int loadMaxMobsPerPlayer() {
        int data = 15;
        if (CONFIG.isLoaded()) {
            int remember = Integer.valueOf(MAX_MOBS_PER_PLAYER.get());
            data = remember;
        }
        return data;
    }

    public static List<String> serializeMobsList(List<MobWeaveDescripton> list) {
        List<String> ListSerialized = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MobWeaveDescripton mobsInfo = list.get(i);
            String serialized = mobsInfo.getMobName()
                    + "," + mobsInfo.getStartWeave()
                    + "," + mobsInfo.getEndWeave()
                    + "," + mobsInfo.getQuantity();
            ListSerialized.add(serialized);
        }
        return ListSerialized;
    }

    public static List<MobWeaveDescripton> deserializeMobsList(List<String> ListSerialized) {
        List<MobWeaveDescripton> list = new ArrayList<>();
        for (String entry : ListSerialized) {
            String[] split = entry.split(",");
            String mobName = split[0];
            int startWeave = Integer.parseInt(split[1]);
            int endWeave = Integer.parseInt(split[2]);
            int quantity = Integer.parseInt(split[3]);
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
                        add("minecraft:zombie,1,0,10");
                        add("minecraft:skeleton,2,0,4");
                        add("minecraft:spider,3,0,4");
                        add("minecraft:creeper,4,0,4");
                        add("minecraft:cave_spider,5,0,3");
                        add("minecraft:slime,6,0,3");
                        add("minecraft:endermite,7,0,3");
                        add("minecraft:husk,8,0,2");
                        add("minecraft:stray,9,0,2");
                        add("minecraft:drowned,0,19,2");
                        add("minecraft:witch,11,0,2");
                        add("minecraft:phantom,12,0,2");
                        add("minecraft:vindicator,13,0,2");
                        add("minecraft:evoker,14,0,1");
                        add("minecraft:illusioner,15,0,1");
                        add("minecraft:pillager,16,0,1");
                        add("minecraft:vex,17,0,1");
                        add("minecraft:blaze,18,0,1");
                        add("minecraft:magma_cube,19,0,1");
                        add("minecraft:guardian,20,0,1");
                        add("minecraft:ghast,21,0,1");
                        add("minecraft:shulker,22,0,1");
                        add("minecraft:ravager,23,0,1");
                        add("minecraft:enderman,24,0,1");
                        add("minecraft:elder_guardian,25,0,1");
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
