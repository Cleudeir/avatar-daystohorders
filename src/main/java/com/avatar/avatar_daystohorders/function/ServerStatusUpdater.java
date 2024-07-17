package com.avatar.avatar_daystohorders.function;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.avatar.avatar_daystohorders.network.NetworkHandler;
import com.avatar.avatar_daystohorders.network.PlayerStatusPacket;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class ServerStatusUpdater {
    private static final Map<UUID, PlayerStatusPacket> playerStatusMap = new HashMap<>();

    public static void setMobsMax(UUID playerUUID, int max) {
        PlayerStatusPacket status = playerStatusMap.getOrDefault(playerUUID,
                new PlayerStatusPacket(playerUUID, max, 0));
        status.mobsMax = max;
        status.filledBarData = new int[max];
        playerStatusMap.put(playerUUID, status);
        syncPlayerStatus(playerUUID);
    }

    public static void setMobsLives(UUID playerUUID, int lives) {
        PlayerStatusPacket status = playerStatusMap.get(playerUUID);
        if (status != null) {
            status.mobsLives = Math.min(lives, status.mobsMax);
            for (int i = 0; i < status.mobsMax; i++) {
                status.filledBarData[i] = (i < status.mobsLives) ? (200 / status.mobsMax) : 0;
            }
            syncPlayerStatus(playerUUID);
        }
    }

    private static void syncPlayerStatus(UUID playerUUID) {
        PlayerStatusPacket status = playerStatusMap.get(playerUUID);
        if (status != null) {
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                    new PlayerStatusPacket(playerUUID, status.mobsMax, status.mobsLives));
        }
    }
}
