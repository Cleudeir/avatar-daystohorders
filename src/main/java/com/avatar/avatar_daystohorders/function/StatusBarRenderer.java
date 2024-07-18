package com.avatar.avatar_daystohorders.function;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.avatar.avatar_daystohorders.Main;
import com.avatar.avatar_daystohorders.network.PlayerStatusPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class StatusBarRenderer {

    private static final Map<UUID, PlayerStatusPacket> playerStatusMap = new HashMap<>();
    private static final int UPDATE_INTERVAL_TICKS = 20 * 10; // 1 second at 20 TPS
    private static int tickCount = 0;

    public static void updatePlayerStatus(UUID playerUUID, int mobsMax, int mobsLives) {
        PlayerStatusPacket status = playerStatusMap.getOrDefault(playerUUID,
                new PlayerStatusPacket(playerUUID, mobsMax, mobsLives));
        playerStatusMap.put(playerUUID, status);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCount++;
            if (tickCount >= UPDATE_INTERVAL_TICKS) {
                tickCount = 0;
                sendStatusUpdates();
            }
        }
    }

    private static void sendStatusUpdates() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (UUID playerUUID : playerStatusMap.keySet()) {
                ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerUUID);
                if (serverPlayer != null) {
                    PlayerStatusPacket status = playerStatusMap.get(playerUUID);
                    Main.NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new PlayerStatusPacket(playerUUID, status.mobsMax, status.mobsLives));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null && playerStatusMap.containsKey(player.getUUID()) && !event.isCanceled()) {
            PlayerStatusPacket status = playerStatusMap.get(player.getUUID());
            if (status != null && status.mobsLives > 0) {
                renderStatusBar(event.getGuiGraphics(), status);
            }
        }
    }

    private static void renderStatusBar(GuiGraphics guiGraphics, PlayerStatusPacket status) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        int barWidth = 120;
        int barHeight = 3;
        int barX = 5;
        int barY = 13;

        // Draw the background bar (white)
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFFFFFFFF);

        // Draw the filled segments (red)
        int mobsMax = status.mobsMax;
        int mobsLives = status.mobsLives;
        int fillAmount = barWidth / mobsMax;

        for (int i = 0; i < mobsLives; i++) {
            int segmentStartX = barX + (i * fillAmount);
            int segmentEndX = segmentStartX + fillAmount;
            guiGraphics.fill(segmentStartX, barY, segmentEndX, barY + barHeight, 0xFFFF0000);
        }

        // Draw the text
        String text = "Wave mobs lives: " + mobsLives + "/" + mobsMax;
        int textX = barX + (barWidth - mc.font.width(text)) / 2;
        int textY = barY - 10;
        guiGraphics.drawString(mc.font, text, textX, textY, 0xFFFFFFFF);
    }
}
