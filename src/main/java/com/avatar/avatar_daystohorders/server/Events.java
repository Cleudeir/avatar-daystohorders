package com.avatar.avatar_daystohorders.server;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

import com.avatar.avatar_daystohorders.GlobalConfig;
import com.avatar.avatar_daystohorders.Main;
import com.avatar.avatar_daystohorders.Client.StatusBarRenderer;
import com.avatar.avatar_daystohorders.function.MobSpawnHandler;
import com.avatar.avatar_daystohorders.function.PortalSpawnHandler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class Events {
    private static ServerLevel currentWorld;
    private static long currentTime = 0;
    private static int periodWave = 0;
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
                periodWave = GlobalConfig.loadPeriodwave();
                GlobalConfig.getListMobs(1);
            }
            if (world != null) {
                currentWorld = world;
                long time = world.getDayTime();
                int timeDay = (int) (time % 24000);

                boolean isNight = timeDay >= 13000 && timeDay <= 23000;
                currentTime = time;
                int day = (int) (time / 24000) + 1;
                int waveNumber = periodWave == 0 ? 0 : (int) day / periodWave;
                if (checkPeriod(10) && day > 0 && day % periodWave == 0 && isNight) {
                    mobSpawnHandler.startWave(world, waveNumber);
                    endState = true;
                } else if (checkPeriod(15) && endState && !isNight) {
                    mobSpawnHandler.end(world);
                    mobSpawnHandler.save();
                    endState = false;
                }
                if (checkPeriod(1) && isNight) {
                    StatusBarRenderer.sendStatusUpdates();
                } else if (checkPeriod(60) && !isNight) {
                    StatusBarRenderer.resetPlayerStatus();
                    StatusBarRenderer.sendStatusUpdates();
                }

                if (timeDay == 12500) {
                    Collection<ServerPlayer> players = world.getPlayers((Predicate<? super ServerPlayer>) p -> true);
                    for (ServerPlayer player : players) {
                        int timeToWave = periodWave - (day % periodWave);
                        String text = "";
                        if (timeToWave == 4 && day != 0) {
                            text = "This night will be end!";
                        } else {
                            text = timeToWave + 1 + " Day to wave!";
                        }
                        MobSpawnHandler.sendTitleMessage(player, text, 70, 7,
                                30);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerShutdown(ServerStoppingEvent event) {
        mobSpawnHandler.save();
        PortalSpawnHandler.savePortalBlock();
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

}
