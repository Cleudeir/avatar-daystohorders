package com.avatar.avatar_7dayshorders.function;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.avatar.avatar_7dayshorders.Main;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    private static boolean waveSpawned = false;
    private static ScheduledExecutorService scheduler;

    public static int executeStartWave(CommandContext<CommandSourceStack> context) {
        if (!waveSpawned) {
            CommandSourceStack source = context.getSource();
            ServerLevel serverWorld = source.getLevel();
            Collection<ServerPlayer> players = serverWorld.getPlayers((Predicate<? super ServerPlayer>) p -> true);
            scheduler = Executors.newScheduledThreadPool(1);

        }
        return 0;
    }
}
