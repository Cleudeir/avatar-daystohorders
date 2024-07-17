package com.avatar.avatar_daystohorders.function;

import com.avatar.avatar_daystohorders.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class StatusBarRenderer {

    private static final Map<UUID, PlayerStatus> playerStatusMap = new HashMap<>();

    public static void updatePlayerStatus(UUID playerUUID, int mobsMax, int mobsLives) {
        PlayerStatus status = playerStatusMap.getOrDefault(playerUUID, new PlayerStatus());
        status.mobsMax = mobsMax;
        status.mobsLives = mobsLives;
        status.filledBarData = new int[mobsMax];
        for (int i = 0; i < mobsMax; i++) {
            status.filledBarData[i] = (i < mobsLives) ? (200 / mobsMax) : 0;
        }
        playerStatusMap.put(playerUUID, status);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null && playerStatusMap.containsKey(player.getUUID()) && !event.isCanceled()) {
            PlayerStatus status = playerStatusMap.get(player.getUUID());
            if (status != null && status.mobsLives > 0) {
                renderStatusBar(player, event.getGuiGraphics(), status);
            }
        }
    }

    private static void renderStatusBar(Player player, GuiGraphics guiGraphics, PlayerStatus status) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barWidth = 120;
        int barHeight = 3;
        int barX = 5;
        int barY = 13;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFFFFFFFF);

        for (int i = 0; i < status.mobsLives; i++) {
            int filledWidth = status.filledBarData[i];
            guiGraphics.fill(barX + (i * (barWidth / status.mobsMax)), barY,
                    barX + (i * (barWidth / status.mobsMax)) + filledWidth,
                    barY + barHeight, 0xFFFF0000);
        }

        String text = "Wave mobs lives: " + status.mobsLives + "/" + status.mobsMax;
        int textX = barX + (barWidth - mc.font.width(text)) / 2;
        int textY = barY - 10;
        guiGraphics.drawString(mc.font, text, textX, textY, 0xFFFFFFFF);
    }

    private static class PlayerStatus {
        int mobsMax = 0;
        int mobsLives = 0;
        int[] filledBarData = new int[mobsMax];
    }
}
