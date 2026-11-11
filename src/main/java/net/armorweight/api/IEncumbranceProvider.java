package net.armorweight.api;

import net.minecraft.world.entity.LivingEntity;

/**
 * Service interface for querying and extending entity encumbrance calculations.
 */
public interface IEncumbranceProvider {

    /**
     * Resolves the current encumbrance metrics for the specified living entity.
     *
     * @param entity Target living entity.
     * @return Encumbrance state metrics.
     */
    EncumbranceData getEncumbrance(LivingEntity entity);

    /**
     * Recomputes and applies encumbrance and associated mobility modifiers to the entity.
     *
     * @param entity Target living entity.
     * @return The updated encumbrance data.
     */
    EncumbranceData updateEncumbrance(LivingEntity entity);
}
