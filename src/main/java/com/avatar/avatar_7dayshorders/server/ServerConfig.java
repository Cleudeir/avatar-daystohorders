package com.avatar.avatar_7dayshorders.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;
  
    public static ForgeConfigSpec.ConfigValue<List<String>> CURRENT_WAVE_MOBS_PER_PLAYER;

    static {
        setupConfig();
    }

    private static void setupConfig() {
      
        BUILDER.comment("Current wave mobs per player").push("currentWaveMobsPerPlayer");
        CURRENT_WAVE_MOBS_PER_PLAYER = BUILDER
                .comment("Current wave mobs per player")
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

    public static void save(Map<String, List<UUID>> currentWaveMobsPerPlayer) {
        if (!CONFIG.isLoaded()) {
            return;
        }
        CURRENT_WAVE_MOBS_PER_PLAYER.set(serializeCurrentWaveMobsPerPlayer(currentWaveMobsPerPlayer));
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

   
}
