package net.armorweight.event;

import net.armorweight.ArmorWeightMod;
import net.armorweight.util.WeightCalculator;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ArmorWeightMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class PlayerLifecycleEventHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        syncPlayerEncumbrance(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncPlayerEncumbrance(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncPlayerEncumbrance(event.getEntity());
    }

    private static void syncPlayerEncumbrance(Player player) {
        if (!player.level().isClientSide()) {
            WeightCalculator.INSTANCE.updateEncumbrance(player);
        }
    }

    private PlayerLifecycleEventHandler() {
    }
}
