package net.armorweight;

import com.mojang.logging.LogUtils;
import net.armorweight.config.WeightClientConfig;
import net.armorweight.config.WeightServerConfig;
import net.armorweight.registry.ModAttributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(ArmorWeightMod.MOD_ID)
public final class ArmorWeightMod {

    public static final String MOD_ID = "armor_weight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ArmorWeightMod(IEventBus modEventBus, ModContainer modContainer) {
        ModAttributes.ATTRIBUTES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, WeightServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, WeightClientConfig.SPEC);

        LOGGER.info("Armor Weight module initialized successfully.");
    }
}
