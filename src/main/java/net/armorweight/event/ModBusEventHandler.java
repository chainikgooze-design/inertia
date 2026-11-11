package net.armorweight.event;

import net.armorweight.ArmorWeightMod;
import net.armorweight.config.WeightServerConfig;
import net.armorweight.registry.ModAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber(modid = ArmorWeightMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModBusEventHandler {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModAttributes.ENCUMBRANCE);

        if (!event.has(EntityType.PLAYER, Attributes.JUMP_STRENGTH)) {
            event.add(EntityType.PLAYER, Attributes.JUMP_STRENGTH);
        }
    }

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == WeightServerConfig.SPEC) {
            WeightServerConfig.refreshCache();
        }
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == WeightServerConfig.SPEC) {
            WeightServerConfig.refreshCache();
        }
    }

    private ModBusEventHandler() {
    }
}
