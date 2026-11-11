package net.armorweight.util;

import net.armorweight.ArmorWeightMod;
import net.armorweight.api.EncumbranceData;
import net.armorweight.api.IEncumbranceProvider;
import net.armorweight.config.WeightServerConfig;
import net.armorweight.registry.ModAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class WeightCalculator implements IEncumbranceProvider {

    public static final WeightCalculator INSTANCE = new WeightCalculator();

    public static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ArmorWeightMod.MOD_ID, "encumbrance_speed_penalty");

    public static final ResourceLocation JUMP_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ArmorWeightMod.MOD_ID, "encumbrance_jump_penalty");

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private WeightCalculator() {
    }

    @Override
    public EncumbranceData getEncumbrance(LivingEntity entity) {
        return calculate(entity);
    }

    @Override
    public EncumbranceData updateEncumbrance(LivingEntity entity) {
        EncumbranceData data = calculate(entity);
        applyToEntity(entity, data);
        return data;
    }

    public static EncumbranceData calculate(LivingEntity entity) {
        if (entity == null) {
            return EncumbranceData.ZERO;
        }

        double totalWeight = 0.0D;
        double totalArmor = 0.0D;
        double totalToughness = 0.0D;

        Map<ResourceLocation, Double> overrides = WeightServerConfig.getOverrides();
        double armorMultiplier = WeightServerConfig.ARMOR_WEIGHT_MULTIPLIER.get();
        double toughnessMultiplier = WeightServerConfig.TOUGHNESS_WEIGHT_MULTIPLIER.get();
        double basePieceWeight = WeightServerConfig.BASE_PIECE_WEIGHT.get();

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            Double overrideWeight = overrides.get(itemId);

            if (overrideWeight != null) {
                totalWeight += overrideWeight;
                continue;
            }

            double[] stats = extractArmorStats(stack, slot);
            double armor = stats[0];
            double toughness = stats[1];

            totalArmor += armor;
            totalToughness += toughness;

            double pieceWeight = (armor * armorMultiplier) + (toughness * toughnessMultiplier) + basePieceWeight;
            totalWeight += Math.max(0.0D, pieceWeight);
        }

        double speedPenaltyRate = WeightServerConfig.SPEED_PENALTY_PER_UNIT.get();
        double maxSpeedPenalty = WeightServerConfig.MAX_SPEED_PENALTY.get();
        double speedPenalty = Math.min(totalWeight * speedPenaltyRate, maxSpeedPenalty);

        double jumpPenaltyRate = WeightServerConfig.JUMP_PENALTY_PER_UNIT.get();
        double maxJumpPenalty = WeightServerConfig.MAX_JUMP_PENALTY.get();
        double jumpPenalty = Math.min(totalWeight * jumpPenaltyRate, maxJumpPenalty);

        return new EncumbranceData(totalWeight, speedPenalty, jumpPenalty, totalArmor, totalToughness);
    }

    public static void applyToEntity(LivingEntity entity, EncumbranceData data) {
        if (entity == null) {
            return;
        }

        AttributeInstance encumbranceInstance = entity.getAttribute(ModAttributes.ENCUMBRANCE);
        if (encumbranceInstance != null) {
            encumbranceInstance.setBaseValue(data.totalWeight());
        }

        AttributeInstance speedInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedInstance != null) {
            speedInstance.removeModifier(SPEED_MODIFIER_ID);
            if (data.speedPenaltyFraction() > 0.0001D) {
                speedInstance.addPermanentModifier(new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        -data.speedPenaltyFraction(),
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }

        AttributeInstance jumpInstance = entity.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpInstance != null) {
            jumpInstance.removeModifier(JUMP_MODIFIER_ID);
            if (data.jumpPenaltyFraction() > 0.0001D) {
                jumpInstance.addPermanentModifier(new AttributeModifier(
                        JUMP_MODIFIER_ID,
                        -data.jumpPenaltyFraction(),
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }
    }

    private static double[] extractArmorStats(ItemStack stack, EquipmentSlot slot) {
        double[] stats = new double[2];

        stack.forEachModifier(slot, (attributeHolder, modifier) -> {
            if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                if (attributeHolder.is(Attributes.ARMOR)) {
                    stats[0] += modifier.amount();
                } else if (attributeHolder.is(Attributes.ARMOR_TOUGHNESS)) {
                    stats[1] += modifier.amount();
                }
            }
        });

        if (stats[0] == 0.0D && stats[1] == 0.0D && stack.getItem() instanceof ArmorItem armorItem) {
            try {
                stats[0] = armorItem.getMaterial().value().getDefense(armorItem.getType());
                stats[1] = armorItem.getMaterial().value().toughness();
            } catch (Exception ignored) {
            }
        }

        return stats;
    }
}
