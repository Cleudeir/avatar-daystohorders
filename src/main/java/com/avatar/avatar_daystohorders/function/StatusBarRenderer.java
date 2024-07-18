package com.avatar.avatar_daystohorders.function;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.avatar.avatar_daystohorders.Main;
import com.avatar.avatar_daystohorders.network.StatusUpdatePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class StatusBarRenderer {

    private static final Map<UUID, String> playerStatusMap = new HashMap<>();

    public static void updatePlayerStatus(UUID playerUUID, int mobsMax, int mobsLives) {
        String status = mobsLives + "," + mobsMax;
        playerStatusMap.put(playerUUID, status);
    }

    public static void sendStatusUpdates() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (UUID playerUUID : playerStatusMap.keySet()) {
                ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerUUID);
                if (serverPlayer != null) {
                    String status = playerStatusMap.get(playerUUID);
                    System.err.println(
                            "Sending status updates " + serverPlayer.getName().getString() + " " + status);
                    Main.NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new StatusUpdatePacket(playerUUID.toString() + "," + status));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null && playerStatusMap.containsKey(player.getUUID()) && !event.isCanceled()) {
            String status = playerStatusMap.get(player.getUUID());
            if (status != null) {
                String[] parts = status.split(",");
                int mobsMax = Integer.parseInt(parts[0]);
                int mobsLives = Integer.parseInt(parts[1]);
                if (mobsLives > 0) {
                    renderStatusBar(event.getGuiGraphics(), mobsMax, mobsLives);
                }
            }
        }
    }

    private static void renderStatusBar(GuiGraphics guiGraphics, int mobsMax, int mobsLives) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        int barWidth = 120;
        int barHeight = 3;
        int barX = 5;
        int barY = 13;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFFFFFFFF);

        int fillAmount = barWidth / mobsMax;

        for (int i = 0; i < mobsLives; i++) {
            int segmentStartX = barX + (i * fillAmount);
            int segmentEndX = segmentStartX + fillAmount;
            guiGraphics.fill(segmentStartX, barY, segmentEndX, barY + barHeight, 0xFFFF0000);
        }

        String text = "Wave mobs lives: " + mobsLives + "/" + mobsMax;
        int textX = barX + (barWidth - mc.font.width(text)) / 2;
        int textY = barY - 10;
        guiGraphics.drawString(mc.font, text, textX, textY, 0xFFFFFFFF);
    }
}