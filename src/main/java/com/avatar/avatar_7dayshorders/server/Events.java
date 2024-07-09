package com.avatar.avatar_7dayshorders.server;

import java.util.UUID;

import com.avatar.avatar_7dayshorders.GlobalConfig;
import com.avatar.avatar_7dayshorders.Main;
import com.avatar.avatar_7dayshorders.function.MobSpawnHandler;
import com.avatar.avatar_7dayshorders.function.ModCommands;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class Events {
    private static ServerLevel currentWorld;
    private static long currentTime = 0;
    private static int periodWeave = 0;
    private static MobSpawnHandler mobSpawnHandler = new MobSpawnHandler();
    private static boolean endState = false;

    public static boolean checkPeriod(double seconds) {
        double divisor = (double) (seconds * 20);
        return currentTime % divisor == 0;
    }

    @SubscribeEvent
    public static void ticksServer(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ServerLevel world = event.getServer().getLevel(Level.OVERWORLD);
            if (currentWorld == null) {
                periodWeave = GlobalConfig.loadPeriodWeave();
                GlobalConfig.getListMobs(1);
            }
            System.out.println("teste1");
            if (world != null) {
                currentWorld = world;
                long time = world.getDayTime();
                int timeDay = (int) (time % 24000);
                boolean isNight = timeDay >= 13000 && timeDay <= 23000;
                currentTime = time;
                int day = (int) (time / 24000);
                int weaveNumber = periodWeave == 0 ? 0 : (int) day / periodWeave;
                if (checkPeriod(15) && day > 0 && day % periodWeave == 0 && isNight) {
                    mobSpawnHandler.start(world, weaveNumber);
                    endState = true;
                } else if (checkPeriod(15) && endState) {
                    mobSpawnHandler.end(world);
                    mobSpawnHandler.save();
                    endState = false;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerShutdown(ServerStoppingEvent event) {
        mobSpawnHandler.save();
        System.out.println("Server is shutting down!");
    }

    @SubscribeEvent
    public static void OnLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob) {
            UUID mobId = event.getEntity().getUUID();
            if (currentWorld != null) {
                MobSpawnHandler.removeMob(mobId, currentWorld);
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("startwave")
                .requires(source -> source.hasPermission(2)) // Requires a permission level of 2 (cheats enabled)
                .executes(ModCommands::executeStartWave));
    }

}
