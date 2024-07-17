package com.avatar.avatar_daystohorders.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import com.avatar.avatar_daystohorders.function.StatusBarRenderer;

public class PlayerStatusPacket {
    private final UUID playerUUID;
    public int mobsMax;
    public int mobsLives;
    public int[] filledBarData;

    public PlayerStatusPacket(UUID playerUUID, int mobsMax, int mobsLives) {
        this.playerUUID = playerUUID;
        this.mobsMax = mobsMax;
        this.mobsLives = mobsLives;
    }

    public PlayerStatusPacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.mobsMax = buf.readInt();
        this.mobsLives = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeInt(mobsMax);
        buf.writeInt(mobsLives);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            StatusBarRenderer.updatePlayerStatus(playerUUID, mobsMax, mobsLives);
        });
        ctx.get().setPacketHandled(true);
    }
}
