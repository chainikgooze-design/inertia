package net.armorweight.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class WeightClientConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_HUD;
    public static final ModConfigSpec.IntValue HUD_X_OFFSET;
    public static final ModConfigSpec.IntValue HUD_Y_OFFSET;
    public static final ModConfigSpec.ConfigValue<String> DISPLAY_UNIT;
    public static final ModConfigSpec.BooleanValue SHOW_PENALTY_IN_HUD;
    public static final ModConfigSpec.BooleanValue SHOW_WHEN_ZERO;
    public static final ModConfigSpec.IntValue TEXT_COLOR;
    public static final ModConfigSpec.IntValue HEAVY_TEXT_COLOR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Client-side Encumbrance HUD settings").push("hud");

        ENABLE_HUD = builder
                .comment("Enable or disable the encumbrance HUD overlay above the hotbar.")
                .define("enableHud", true);

        HUD_X_OFFSET = builder
                .comment("Horizontal offset relative to screen center.")
                .defineInRange("hudXOffset", 0, -2000, 2000);

        HUD_Y_OFFSET = builder
                .comment("Vertical offset relative to the bottom of the screen (default -52 places it above experience/hotbar).")
                .defineInRange("hudYOffset", -52, -2000, 2000);

        DISPLAY_UNIT = builder
                .comment("Display unit suffix for weight (e.g., 'kg', 'lbs', 'u').")
                .define("displayUnit", "kg");

        SHOW_PENALTY_IN_HUD = builder
                .comment("Show speed penalty percentage alongside weight in the HUD.")
                .define("showPenaltyInHud", true);

        SHOW_WHEN_ZERO = builder
                .comment("Whether to render the HUD overlay when encumbrance is 0.")
                .define("showWhenZero", false);

        TEXT_COLOR = builder
                .comment("Default hex color for HUD text (ARGB).")
                .defineInRange("textColor", 0xFFE0E0E0, Integer.MIN_VALUE, Integer.MAX_VALUE);

        HEAVY_TEXT_COLOR = builder
                .comment("Hex color used when mobility penalty reaches 25% or higher (ARGB).")
                .defineInRange("heavyTextColor", 0xFFFF5555, Integer.MIN_VALUE, Integer.MAX_VALUE);

        builder.pop();

        SPEC = builder.build();
    }

    private WeightClientConfig() {
    }
}
