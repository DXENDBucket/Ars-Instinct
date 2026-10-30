package dev.arsinstinct.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/** Explicit installation; does not equip a mob, grant aggression or replace its target selector. */
public final class InstinctCasting {
    public static final TagKey<EntityType<?>> SPELLCASTERS=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.tryParse("ars_instinct:spellcasters"));
    public static boolean install(Mob actor,int priority,double movementSpeed) {return install(actor,priority,movementSpeed,new NativeSpellBackend());}
    public static boolean install(Mob actor,int priority,double movementSpeed,CombatBackend backend) {
        if(actor.level().isClientSide||priority<0||!Double.isFinite(movementSpeed)||movementSpeed<0)return false;
        if(actor.goalSelector.getAvailableGoals().stream().anyMatch(wrapped->wrapped.getGoal() instanceof SpellCombatGoal))return false;
        actor.goalSelector.addGoal(priority,new SpellCombatGoal(actor,movementSpeed,backend));return true;
    }
    private InstinctCasting() {}
}
