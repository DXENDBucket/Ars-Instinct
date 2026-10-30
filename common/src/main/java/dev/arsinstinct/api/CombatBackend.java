package dev.arsinstinct.api;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** One integration for a tool/resource system, not one AI implementation per creature. Server-thread only. */
public interface CombatBackend {
    record Program(int slot,Spell spell) {public Program {Objects.requireNonNull(spell);}}
    record Budget(boolean possible,boolean affordable) {
        public static final Budget UNKNOWN=new Budget(false,false);
    }
    List<Program> programs(LivingEntity actor,ItemStack tool);
    SpellAbility describe(LivingEntity actor,ItemStack tool,Spell spell);
    Budget budget(LivingEntity actor,ItemStack tool,Program program);
    boolean available(LivingEntity actor,ItemStack tool,Program program);
    boolean cast(LivingEntity actor,ItemStack tool,InteractionHand hand,Program program);
    default void select(LivingEntity actor,ItemStack tool,Program program) {}
    default boolean busy(LivingEntity actor) {return dev.arsconflux.api.casting.ActiveCasts.isChanting(actor)
            ||dev.arsconflux.api.execution.ExecutionSessions.isCasting(actor);}
    default long readyAt(LivingEntity actor,Program program) {return 0;}
    /** Native shared keys and durations, if known. Read-only; no forced generic cooldown. */
    default Map<ResourceLocation,Integer> cooldowns(LivingEntity actor,Program program) {return Map.of();}
    default LivingEntity recipient(LivingEntity actor,ItemStack tool,LivingEntity enemy,Program program,SpellAbility ability) {
        return CombatTargets.recipient(actor,enemy,program.spell(),ability.range(),e->useful(actor,e,program.spell()));
    }
    default boolean useful(LivingEntity actor,LivingEntity recipient,Spell spell) {return true;}
    default Vec3 aimOrigin(LivingEntity actor,ItemStack tool) {return actor.getEyePosition();}
}
