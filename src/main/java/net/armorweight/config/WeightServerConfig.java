package net.armorweight.config;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WeightServerConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue ARMOR_WEIGHT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue TOUGHNESS_WEIGHT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue BASE_PIECE_WEIGHT;

    public static final ModConfigSpec.DoubleValue SPEED_PENALTY_PER_UNIT;
    public static final ModConfigSpec.DoubleValue MAX_SPEED_PENALTY;

    public static final ModConfigSpec.DoubleValue JUMP_PENALTY_PER_UNIT;
    public static final ModConfigSpec.DoubleValue MAX_JUMP_PENALTY;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_OVERRIDES;

    private static volatile Map<ResourceLocation, Double> cachedOverrides = Collections.emptyMap();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Equipment encumbrance weight calculation parameters").push("calculation");

        ARMOR_WEIGHT_MULTIPLIER = builder
                .comment("Weight added per point of Armor defense.")
                .defineInRange("armorWeightMultiplier", 1.5D, 0.0D, 100.0D);

        TOUGHNESS_WEIGHT_MULTIPLIER = builder
                .comment("Weight added per point of Armor Toughness.")
                .defineInRange("toughnessWeightMultiplier", 3.0D, 0.0D, 100.0D);

        BASE_PIECE_WEIGHT = builder
                .comment("Flat base weight added for each non-empty armor piece equipped.")
                .defineInRange("basePieceWeight", 0.5D, 0.0D, 100.0D);

        builder.pop();

        builder.comment("Mobility penalties applied from total encumbrance").push("penalties");

        SPEED_PENALTY_PER_UNIT = builder
                .comment("Fractional movement speed penalty per encumbrance unit (e.g. 0.005 = 0.5% reduction per 1 kg).")
                .defineInRange("speedPenaltyPerUnit", 0.005D, 0.0D, 1.0D);

        MAX_SPEED_PENALTY = builder
                .comment("Maximum fraction of movement speed that can be deducted by encumbrance (0.70 = 70% maximum penalty).")
                .defineInRange("maxSpeedPenalty", 0.70D, 0.0D, 0.95D);

        JUMP_PENALTY_PER_UNIT = builder
                .comment("Fractional jump strength penalty per encumbrance unit (e.g. 0.003 = 0.3% reduction per 1 kg).")
                .defineInRange("jumpPenaltyPerUnit", 0.003D, 0.0D, 1.0D);

        MAX_JUMP_PENALTY = builder
                .comment("Maximum fraction of jump strength that can be deducted by encumbrance (0.50 = 50% maximum penalty).")
                .defineInRange("maxJumpPenalty", 0.50D, 0.0D, 0.95D);

        builder.pop();

        builder.comment("Item weight overrides (format: \"namespace:item_id=weight\")").push("overrides");

        ITEM_OVERRIDES = builder
                .comment("Overrides auto-calculated weight for specific items.",
                        "Example: [\"minecraft:netherite_chestplate=35.0\", \"minecraft:leather_boots=1.5\"]")
                .defineListAllowEmpty(
                        "itemOverrides",
                        List.of(
                                "minecraft:leather_helmet=1.0",
                                "minecraft:leather_chestplate=3.0",
                                "minecraft:leather_leggings=2.0",
                                "minecraft:leather_boots=1.0"
                        ),
                        element -> element instanceof String str && str.contains("=")
                );

        builder.pop();

        SPEC = builder.build();
    }

    public static Map<ResourceLocation, Double> getOverrides() {
        return cachedOverrides;
    }

    public static void refreshCache() {
        Map<ResourceLocation, Double> map = new HashMap<>();
        List<? extends String> rawList = ITEM_OVERRIDES.get();
        for (String entry : rawList) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2) {
                LOGGER.warn("Invalid weight override entry '{}', expected format 'modid:item_name=value'", entry);
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
            if (id == null) {
                LOGGER.warn("Invalid ResourceLocation '{}' in weight overrides", parts[0]);
                continue;
            }
            try {
                double weight = Double.parseDouble(parts[1].trim());
                map.put(id, Math.max(0.0D, weight));
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid numerical weight '{}' for item '{}'", parts[1], id);
            }
        }
        cachedOverrides = Collections.unmodifiableMap(map);
    }

    private WeightServerConfig() {
    }
}
