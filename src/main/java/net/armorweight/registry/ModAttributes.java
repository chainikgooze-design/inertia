package net.armorweight.registry;

import net.armorweight.ArmorWeightMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, ArmorWeightMod.MOD_ID);

    /**
     * Entity encumbrance attribute, measured in weight units (e.g., kg).
     * Synchronized to client (setSyncable(true)) to enable zero-overhead client-side
     * movement inertia simulation and responsive HUD rendering without per-tick inventory polling.
     */
    public static final DeferredHolder<Attribute, Attribute> ENCUMBRANCE = ATTRIBUTES.register(
            "encumbrance",
            () -> new RangedAttribute("attribute.name.armor_weight.encumbrance", 0.0D, 0.0D, 2048.0D)
                    .setSyncable(true)
    );

    private ModAttributes() {
    }
}
