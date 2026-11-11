package net.armorweight.api;

/**
 * Immutable snapshot representing an entity's encumbrance metrics.
 *
 * @param totalWeight           Aggregated weight of all equipped items.
 * @param speedPenaltyFraction  Normalized reduction applied to movement speed [0.0, 1.0].
 * @param jumpPenaltyFraction   Normalized reduction applied to jump strength [0.0, 1.0].
 * @param rawArmor              Sum of defense points from equipped items.
 * @param rawToughness          Sum of armor toughness points from equipped items.
 */
public record EncumbranceData(
        double totalWeight,
        double speedPenaltyFraction,
        double jumpPenaltyFraction,
        double rawArmor,
        double rawToughness
) {
    public static final EncumbranceData ZERO = new EncumbranceData(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
}
