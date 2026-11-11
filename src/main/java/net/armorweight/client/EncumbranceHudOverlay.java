package net.armorweight.client;

import net.armorweight.config.WeightClientConfig;
import net.armorweight.config.WeightServerConfig;
import net.armorweight.registry.ModAttributes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.Locale;

public final class EncumbranceHudOverlay {

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!WeightClientConfig.ENABLE_HUD.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        AttributeInstance encumbranceAttribute = player.getAttribute(ModAttributes.ENCUMBRANCE);
        if (encumbranceAttribute == null) {
            return;
        }

        double encumbrance = encumbranceAttribute.getValue();
        if (encumbrance <= 0.0001D && !WeightClientConfig.SHOW_WHEN_ZERO.get()) {
            return;
        }

        double penaltyRate = WeightServerConfig.SPEED_PENALTY_PER_UNIT.get();
        double maxPenalty = WeightServerConfig.MAX_SPEED_PENALTY.get();
        double speedPenaltyPercent = Math.min(encumbrance * penaltyRate, maxPenalty) * 100.0D;

        String unit = WeightClientConfig.DISPLAY_UNIT.get();
        Component text;
        if (WeightClientConfig.SHOW_PENALTY_IN_HUD.get() && speedPenaltyPercent > 0.05D) {
            text = Component.translatable(
                    "hud.armor_weight.display",
                    String.format(Locale.ROOT, "%.1f", encumbrance),
                    unit,
                    String.format(Locale.ROOT, "%.0f", speedPenaltyPercent)
            );
        } else {
            text = Component.translatable(
                    "hud.armor_weight.display_no_penalty",
                    String.format(Locale.ROOT, "%.1f", encumbrance),
                    unit
            );
        }

        Font font = mc.font;
        int textWidth = font.width(text);
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int x = (screenWidth - textWidth) / 2 + WeightClientConfig.HUD_X_OFFSET.get();
        int y = screenHeight + WeightClientConfig.HUD_Y_OFFSET.get();

        int color = (speedPenaltyPercent >= 25.0D)
                ? WeightClientConfig.HEAVY_TEXT_COLOR.get()
                : WeightClientConfig.TEXT_COLOR.get();

        guiGraphics.drawString(font, text, x, y, color, true);
    }

    private EncumbranceHudOverlay() {
    }
}
