package com.avatar.avatar_7dayshorders.server;

import com.avatar.avatar_7dayshorders.GlobalConfig;
import com.avatar.avatar_7dayshorders.Main;
import com.avatar.avatar_7dayshorders.function.MobSpawnHandler;
import com.avatar.avatar_7dayshorders.function.ModCommands;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class Events {
    private static ServerLevel currentWorld;
    private static long currentTime = 0;
    private static int periodWeave = 0;

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
            }
            if (world != null) {
                currentWorld = world;
                long time = world.getDayTime();
                int timeDay = (int) (time % 24000);
                boolean isNight = timeDay >= 13000 && timeDay <= 23000;
                currentTime = time;
                int day = (int) (time / 24000);
                if (checkPeriod(10) && day % periodWeave == 0 && isNight) {
                    MobSpawnHandler.start(world);
                }
            }

        }
    }

    @SubscribeEvent
    public static void onPutMainBlock(BlockEvent.EntityPlaceEvent event) {
        BlockState getPlacedBlock = event.getPlacedBlock();

    }

    @SubscribeEvent
    public static void onServerShutdown(ServerStoppingEvent event) {
        System.out.println("Server is shutting down!");
    }

    @SubscribeEvent
    public void onPlayerBreak(BreakEvent event) {
        BlockState state = event.getState();
        Player player = event.getPlayer();
        BlockPos position = event.getPos();

        if (player != null) {

        }

    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob) {
            Mob mob = (Mob) event.getEntity();
        }
    }

    @SubscribeEvent
    public static void onLiving(PlayerRespawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ServerLevel world = (ServerLevel) player.level();
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("startwave")
                .requires(source -> source.hasPermission(2)) // Requires a permission level of 2 (cheats enabled)
                .executes(ModCommands::executeStartWave));
    }

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Detonate event) {

    }

}
