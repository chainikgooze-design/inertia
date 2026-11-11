package net.armorweight.client;

import net.armorweight.ArmorWeightMod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ArmorWeightMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    public static final ResourceLocation HUD_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(ArmorWeightMod.MOD_ID, "encumbrance_hud");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, HUD_LAYER_ID, EncumbranceHudOverlay::render);
    }

    private ClientModEvents() {
    }
}
