package com.avatar.avatar_daystohorders.network;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import com.avatar.avatar_daystohorders.function.StatusBarRenderer;

public class StatusUpdatePacket {
    private final String statusUpdate;

    public StatusUpdatePacket(String statusUpdate) {
        this.statusUpdate = statusUpdate;
    }

    public StatusUpdatePacket(FriendlyByteBuf buf) {
        this.statusUpdate = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(statusUpdate);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            String[] parts = statusUpdate.split(",");
            UUID playerUUID = UUID.fromString(parts[0]);
            int mobsLives = Integer.parseInt(parts[1]);
            int mobsMax = Integer.parseInt(parts[2]);
            StatusBarRenderer.updatePlayerStatus(playerUUID, mobsMax, mobsLives);
        });
        return true;
    }
}