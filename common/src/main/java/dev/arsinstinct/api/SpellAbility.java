package dev.arsinstinct.api;

/** Simple spatial hints from the tool/provider. Numeric spell formulas stay with their owner. */
public record SpellAbility(boolean closeCombat, double range, double projectileSpeed, double gravity) {
    public SpellAbility {
        if(!Double.isFinite(range)||range<0||!Double.isFinite(projectileSpeed)||projectileSpeed<0
                ||!Double.isFinite(gravity)||gravity<0)throw new IllegalArgumentException("Invalid spell geometry");
    }
}
