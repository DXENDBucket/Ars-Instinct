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

/** Native Ars tools using an existing real Ars pool. Does not create a second pool or fake player. */
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
    @Override public Budget budget(LivingEntity actor,ItemStack tool,Program program) {
        var context=context(actor,tool,program.spell());
        var quote=SpellResourceQuotes.estimate(context);
        if(!quote.complete())return Budget.UNKNOWN;
        var ars=new ArsManaAccount(context.getCaster());
        record Total(ResourceAccount account,long amount) {}
        var totals=new HashMap<Object,Total>();
        totals.put(ars.reservationKey(),new Total(ars,Math.max(0,new SpellResolver(context).getResolveCost())));
        for(var entry:quote.entries()) {
            var charge=entry.charge();var key=charge.account().reservationKey();var old=totals.get(key);
            totals.put(key,new Total(charge.account(),charge.amount()+(old==null?0:old.amount())));
        }
        boolean possible=true,affordable=true;
        for(var total:totals.values()) {
            if(total.account().free())continue;
            if(total.account().reservationKey().equals(ars.reservationKey()))
                possible&=NativePrograms.maximumMana(actor)>=total.amount();
            affordable&=ResourceTransactions.canSpend(total.account(),total.amount());
        }
        return new Budget(possible,affordable);
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
