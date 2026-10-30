package dev.arsinstinct.api;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import dev.arsconflux.api.casting.*;
import dev.arsconflux.api.glyph.*;
import dev.arsconflux.api.resource.*;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsinstinct.platform.NativePrograms;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Native Ars tools using the real native Ars pool. Does not create a second pool or fake player. */
public class NativeSpellBackend implements CombatBackend {
    @Override public List<Program> programs(LivingEntity actor,ItemStack tool) {return NativePrograms.programs(tool);}
    @Override public void select(LivingEntity actor,ItemStack tool,Program program) {NativePrograms.select(tool,program.slot());}
    @Override public SpellAbility describe(LivingEntity actor,ItemStack tool,Spell spell) {
        if(!ProgramIntent.of(spell).known())return null;
        var method=spell.getCastMethod();
        if(method==MethodTouch.INSTANCE)return new SpellAbility(true,3,0,0);
        if(method==MethodSelf.INSTANCE)return new SpellAbility(true,12,0,0);
        if(method==MethodProjectile.INSTANCE||CarrierRegistry.of(method)!=null)return new SpellAbility(false,48,0,0);
        return null;
    }
    protected SpellContext context(LivingEntity actor,ItemStack tool,Spell spell) {return SpellContext.fromEntity(spell,actor,tool);}
    @Override public boolean useful(LivingEntity actor,LivingEntity recipient,Spell spell) {
        return GlyphConditions.useful(ProgramIntent.of(spell).primary(),actor,recipient);
    }
    @Override public Budget budget(LivingEntity actor,ItemStack tool,Program program) {
        var quote=SpellBudgets.estimate(context(actor,tool,program.spell()));
        return new Budget(quote.possible(),quote.affordable());
    }
    @Override public Map<net.minecraft.resources.ResourceLocation,Integer> cooldowns(LivingEntity actor,Program program) {
        var result=new LinkedHashMap<net.minecraft.resources.ResourceLocation,Integer>();
        for(var quote:ActiveCasts.cooldowns(program.spell(),actor,ActiveCastSource.CASTER_TOOL))result.merge(quote.key(),quote.duration(),Math::max);
        return Map.copyOf(result);
    }
    @Override public long readyAt(LivingEntity actor,Program program) {
        var rejection=ActiveCasts.rejection(program.spell(),actor,ActiveCastSource.CASTER_TOOL);
        return rejection==null?0:actor.level().getGameTime()+Math.max(1,rejection.remainingTicks());
    }
    @Override public boolean available(LivingEntity actor,ItemStack tool,Program program) {
        return ActiveCasts.rejection(program.spell(),actor,ActiveCastSource.CASTER_TOOL)==null
                &&new SpellResolver(context(actor,tool,program.spell())).withSilent(true).canCast(actor);
    }
    @Override public boolean cast(LivingEntity actor,ItemStack tool,InteractionHand hand,Program program) {
        if(actor.level().isClientSide||actor.getItemInHand(hand)!=tool)return false;
        return ActiveCasts.perform(program.spell(),actor,hand,ActiveCastSource.CASTER_TOOL,
                ()->new SpellResolver(context(actor,tool,program.spell())).withSilent(true).onCast(tool,actor.level()),false,true);
    }
}
