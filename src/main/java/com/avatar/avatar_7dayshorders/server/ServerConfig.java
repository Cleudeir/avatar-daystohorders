package com.avatar.avatar_7dayshorders.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.avatar.avatar_7dayshorders.function.MobWeaveDescripton;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;

    // Define your config values here
    public static ForgeConfigSpec.ConfigValue<List<String>> LIST_MOBS_PER_WEAVE;
    public static ForgeConfigSpec.ConfigValue<List<String>> CURRENT_WAVE_MOBS_PER_PLAYER;

    // Initialize config values without static initializer block
    static {
        setupConfig();
    }

    private static void setupConfig() {
        BUILDER.comment("Mobs Per Weave").push("mobsPerWeave");
        ArrayList<String> defaultMobsPerWeave = new ArrayList<String>() {
            {
                add("[minecraft:zombie,1,0,0]");
                add("[minecraft:skeleton,2,0,0]");
                add("[minecraft:creeper,3,0,0]");
                add("[minecraft:spider,4,0,0]");
                add("[minecraft:enderman,1,0,0]");
                add("[minecraft:endermite,1,0,0]");
                add("[minecraft:cave_spider,1,0,0]");
                add("[minecraft:witch,1,0,0]");
                add("[minecraft:blaze,1,0,0]");
                add("[minecraft:ghast,1,0,0]");
                add("[minecraft:slime,1,0,0]");
                add("[minecraft:magma_cube,1,0,0]");
                add("[minecraft:phantom,1,0,0]");
                add("[minecraft:vindicator,1,0,0]");
                add("[minecraft:evoker,1,0,0]");
                add("[minecraft:ravager,1,0,0]");
                add("[minecraft:husk,1,0,0]");
                add("[minecraft:stray,1,0,0]");
                add("[minecraft:drowned,1,0,0]");
                add("[minecraft:guardian,1,0,0]");
                add("[minecraft:elder_guardian,1,0,0]");
                add("[minecraft:shulker,1,0,0]");
                add("[minecraft:illusioner,1,0,0]");
                add("[minecraft:pillager,1,0,0]");
                add("[minecraft:vex,1,0,0]");
            }
        };
        LIST_MOBS_PER_WEAVE = BUILDER.comment(
                "Default mobs per weave table data, example: [minecraft:zombie,1,0,0] = [mobName,quantity,startWeave,endWeave], endWeave if 0 = infinity")
                .define("mobsPerWeave", defaultMobsPerWeave);

        BUILDER.pop();

        BUILDER.comment("Current wave mobs per player").push("currentWaveMobsPerPlayer");
        CURRENT_WAVE_MOBS_PER_PLAYER = BUILDER
                .comment("Current wave mobs per player")
                .define("currentWaveMobsPerPlayer", new ArrayList<String>());
        BUILDER.pop();

        CONFIG = BUILDER.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG);
    }

    public static List<String> serializeMobsList(List<MobWeaveDescripton> list) {
        List<String> ListSerialized = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MobWeaveDescripton mobsInfo = list.get(i);
            String serialized = "[" + mobsInfo.getMobName() + "," + mobsInfo.getQuantity() + ","
                    + mobsInfo.getStartWeave()
                    + "," + mobsInfo.getEndWeave() + "]";
            ListSerialized.add(serialized);
        }
        return ListSerialized;
    }

    public static List<MobWeaveDescripton> deserializeMobsList(List<String> ListSerialized) {
        List<MobWeaveDescripton> list = new ArrayList<>();
        for (String entry : ListSerialized) {
            String[] split = entry.replace("[", "").replace("]", "").split(",");
            String mobName = split[0];
            int quantity = Integer.parseInt(split[1]);
            int startWeave = Integer.parseInt(split[2]);
            int endWeave = Integer.parseInt(split[3]);
            MobWeaveDescripton mobsInfo = new MobWeaveDescripton(mobName, quantity, startWeave, endWeave);
            list.add(mobsInfo);
        }
        return list;
    }

    public static List<String> serializeCurrentWaveMobsPerPlayer(Map<Integer, List<Integer>> currentWaveMobsPerPlayer) {
        List<String> ListSerialized = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : currentWaveMobsPerPlayer.entrySet()) {
            Integer key = entry.getKey();
            List<Integer> value = entry.getValue();
            String numberString = value.stream().map(String::valueOf)
                    .collect(Collectors.joining(","));
            String serialized = key + ":[" + numberString + "]";
            ListSerialized.add(serialized);
        }
        return ListSerialized;
    }

    public static Map<Integer, List<Integer>> deserializeCurrentWaveMobsPerPlayer(List<String> ListSerialized) {
        Map<Integer, List<Integer>> currentWaveMobsPerPlayer = new HashMap<>();
        for (String entry : ListSerialized) {
            if (entry.isEmpty()) {
                continue; // Skip empty entries
            }
            String[] split = entry.split(":");
            if (split.length < 2) {
                continue; // Skip invalid entries
            }
            int key = Integer.parseInt(split[0]);
            String[] array = split[1].replace("[", "").replace("]", "").split(",");
            List<Integer> value = Arrays.stream(array)
                    .filter(x -> !x.isEmpty()) // Filter out empty strings
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            currentWaveMobsPerPlayer.put(key, value);
        }
        return currentWaveMobsPerPlayer;
    }

    // Method to save data
    public static void save(
            Map<Integer, List<Integer>> currentWaveMobsPerPlayer) {
        CURRENT_WAVE_MOBS_PER_PLAYER.set(serializeCurrentWaveMobsPerPlayer(currentWaveMobsPerPlayer));
        CONFIG.save();
    }

    public static List<Integer> getPlayerMobs(int playerId) {
        Map<Integer, List<Integer>> data = new HashMap<>();
        if (CONFIG.isLoaded()) {
            data = deserializeCurrentWaveMobsPerPlayer(CURRENT_WAVE_MOBS_PER_PLAYER.get());
            System.out.println("Data loaded from config");
        }
        if (!data.containsKey(playerId)) {
            return new ArrayList<>();
        }
        return data.get(playerId);
    }

    public static List<MobWeaveDescripton> getListMobs(Integer weaveNumber) {
        List<MobWeaveDescripton> data = new ArrayList<>();
        if (CONFIG.isLoaded()) {
            data = deserializeMobsList(LIST_MOBS_PER_WEAVE.get());
            data = data.stream()
                    .filter(x -> x.getStartWeave() <= weaveNumber
                            && (x.getEndWeave() >= weaveNumber || x.getEndWeave() == 0))
                    .collect(Collectors.toList());
            System.out.println("Data loaded from config");
        }
        return data;
    }
}
