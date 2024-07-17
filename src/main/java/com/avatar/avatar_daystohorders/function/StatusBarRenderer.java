package com.avatar.avatar_daystohorders.function;

import com.avatar.avatar_daystohorders.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class StatusBarRenderer {

    private static int mobsMax = 0;
    private static int mobsLives = 0;
    private static int[] filledBarData = new int[mobsMax];

    public static void setMobsMax(int max) {
        mobsMax = max;
        filledBarData = new int[max];
    }

    public static void setMobsLives(int lives) {
        mobsLives = Math.min(lives, mobsMax);
        for (int i = 0; i < mobsMax; i++) {
            filledBarData[i] = (i < mobsLives) ? (200 / mobsMax) : 0;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (mobsLives > 0 && !event.isCanceled()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                renderStatusBar(mc.player, event.getGuiGraphics());
            }
        }
    }

    public static void renderStatusBar(Player player, GuiGraphics guiGraphics) {
        if (player == null || filledBarData == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barWidth = 120;
        int barHeight = 3;
        int barX = 5;
        int barY = 13; // Adjusted to leave space for the text

        // Draw Background Bar
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFFFFFFFF);

        // Draw Filled Bar
        for (int i = 0; i < mobsLives; i++) {
            int filledWidth = filledBarData[i];
            guiGraphics.fill(barX + (i * (barWidth / mobsMax)), barY, barX + (i * (barWidth / mobsMax)) + filledWidth,
                    barY + barHeight, 0xFFFF0000);
        }

        // Draw Text
        String text = "Wave mobs lives: " + mobsLives + "/" + mobsMax;
        int textX = barX + (barWidth - mc.font.width(text)) / 2;
        int textY = barY - 10; // Positioning the text above the bar
        guiGraphics.drawString(mc.font, text, textX, textY, 0xFFFFFFFF); // White color for the text
    }
}
