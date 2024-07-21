package com.avatar.avatar_daystohorders.server;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.avatar.avatar_daystohorders.GlobalConfig;
import com.avatar.avatar_daystohorders.Main;
import com.avatar.avatar_daystohorders.Client.StatusBarRenderer;
import com.avatar.avatar_daystohorders.function.MobSpawnHandler;
import com.avatar.avatar_daystohorders.function.PortalSpawnHandler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
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
                if (checkPeriod(1) && day > 0 && day % periodWave == 0 && isNight) {
                    mobSpawnHandler.startWave(world, waveNumber);
                    endState = true;
                } else if (checkPeriod(15) && endState && !isNight) {
                    mobSpawnHandler.end(world);
                    mobSpawnHandler.save();
                    endState = false;
                }
                if (checkPeriod(10) && day > 0 && day % periodWave == 0 && isNight) {
                    mobSpawnHandler.startWave(world, waveNumber);
                    endState = true;
                }
                List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
                if (checkPeriod(5)) {
                    if (players == null)
                        return;
                    Iterable<Entity> allUnits = world.getAllEntities();
                    AtomicInteger total = new AtomicInteger(0);
                    AtomicInteger monster = new AtomicInteger(0);
                    AtomicInteger ambientCreature = new AtomicInteger(0);
                    AtomicInteger animal = new AtomicInteger(0);
                    AtomicInteger item = new AtomicInteger(0);
                    AtomicInteger npcCount = new AtomicInteger(0);
                    AtomicInteger playerCount = new AtomicInteger(0);

                    allUnits.forEach(entity -> {
                        if (entity instanceof ItemEntity) {
                            item.incrementAndGet();
                            total.incrementAndGet();
                        } else if (entity instanceof AmbientCreature) {
                            ambientCreature.incrementAndGet();
                            total.incrementAndGet();
                        } else if (entity instanceof Animal) {
                            animal.incrementAndGet();
                            total.incrementAndGet();
                        } else if (entity instanceof Npc) {
                            npcCount.incrementAndGet();
                            total.incrementAndGet();
                        } else if (entity instanceof ServerPlayer) {
                            playerCount.incrementAndGet();
                            total.incrementAndGet();
                        } else if (entity instanceof Mob) {
                            monster.incrementAndGet();
                            total.incrementAndGet();
                        } else {
                            System.out.println("Entity type: " + entity.getClass().getName().toString());
                        }

                    });
                    int totalCount = total.get();
                    int totalMonsters = monster.get();
                    int totalAmbientCreatures = ambientCreature.get();
                    int totalAnimals = animal.get();
                    int totalItems = item.get();
                    int totalNpcs = npcCount.get();
                    int totalPlayers = playerCount.get();

                    for (ServerPlayer player : players) {
                        MobSpawnHandler.message(player, "Total mobs: " + totalCount);
                        MobSpawnHandler.message(player, "Monsters: " + totalMonsters);
                        MobSpawnHandler.message(player, "Ambient Creatures: " + totalAmbientCreatures);
                        MobSpawnHandler.message(player, "Animals: " + totalAnimals);
                        MobSpawnHandler.message(player, "Items: " + totalItems);
                        MobSpawnHandler.message(player, "Npcs: " + totalNpcs);
                        MobSpawnHandler.message(player, "Players: " + totalPlayers);
                        break;
                    }
                }
                if (checkPeriod(1) && isNight) {
                    StatusBarRenderer.sendStatusUpdates();
                } else if (checkPeriod(30) && !isNight) {
                    StatusBarRenderer.resetPlayerStatus();
                }
                if (timeDay == 12500) {
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
    public static void onLivingCheckSpawn(MobSpawnEvent event) {
        Entity entity = event.getEntity();
        ServerLevel world = (ServerLevel) entity.level();
        int distant = 30;

        // Check if the mob has a custom tag indicating it was already spawned
        List<ServerPlayer> players = world.players();
        if (entity.getPersistentData().getBoolean("wasRespawned")) {
            // Check if the mob is out of range and despawn it if necessary

            boolean playerNearby = false;
            for (ServerPlayer player : players) {
                if (player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) <= distant * distant &&
                        Math.abs(player.getY() - entity.getY()) <= distant / 2) {
                    playerNearby = true;
                }
            }

            if (!playerNearby && checkPeriod(120)) {
                entity.discard(); // Despawn the entity if no players are nearby
            }

            return; // Exit the method if the mob was already respawned
        }

        boolean playerNearby = false;
        for (ServerPlayer player : players) {
            if (player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) <= distant * distant &&
                    Math.abs(player.getY() - entity.getY()) <= distant / 2) {
                playerNearby = true;
            }
        }

        // If no player is nearby, deny the spawn
        if (!playerNearby) {
            event.setResult(MobSpawnEvent.Result.DENY);
        } else {
            // If a player is nearby, teleport the entity and mark it as respawned
            for (ServerPlayer player : players) {
                double x = player.getX() + 5;
                double y = player.getY() + 3;
                double z = player.getZ() + 5;

                // entity.teleportTo(x, y, z);
                // entity.discard(); // Despawn the entity
                entity.getPersistentData().putBoolean("wasRespawned", true);
            }
        }

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
