package net.armorweight.event;

import net.armorweight.ArmorWeightMod;
import net.armorweight.util.WeightCalculator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

@EventBusSubscriber(modid = ArmorWeightMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class EquipmentChangeEventHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (event.getSlot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            WeightCalculator.INSTANCE.updateEncumbrance(player);
        }
    }

    private EquipmentChangeEventHandler() {
    }
}
