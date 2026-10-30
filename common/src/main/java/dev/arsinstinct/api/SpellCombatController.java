package dev.arsinstinct.api;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Caller-owned state. Reusable for ground mobs, flying mobs or externally managed actors. */
public final class SpellCombatController {
    public record Policy(double firstSlotWeight,double repeatedWeight,int maximumSavingTicks) {
        public static final Policy DEFAULT=new Policy(3,.2,200);
        public Policy {if(!Double.isFinite(firstSlotWeight)||firstSlotWeight<=0||!Double.isFinite(repeatedWeight)
                ||repeatedWeight<=0||maximumSavingTicks<0)throw new IllegalArgumentException("Invalid AI policy");}
    }
    public record Plan(CombatBackend.Program program,LivingEntity recipient,SpellAbility ability) {}
    private final CombatBackend backend;
    private final Policy policy;
    private Plan plan;
    private ItemStack toolSnapshot=ItemStack.EMPTY;
    private long savingSince;
    private LivingEntity enemy;
    private int lastSlot=-1;
    public SpellCombatController(CombatBackend backend) {this(backend,Policy.DEFAULT);}
    public SpellCombatController(CombatBackend backend,Policy policy) {this.backend=Objects.requireNonNull(backend);this.policy=Objects.requireNonNull(policy);}
    public Plan current() {return plan;}
    public void clear() {plan=null;enemy=null;toolSnapshot=ItemStack.EMPTY;}
    private static long score(Map<?,Integer> durations) {
        long max=durations.values().stream().mapToLong(v->Math.max(0,v)).max().orElse(0);
        return max*100000L+durations.values().stream().mapToLong(v->Math.max(0,v)).sum();
    }
    private boolean delays(LivingEntity actor,Plan shorter,Plan longer) {
        var a=backend.cooldowns(actor,shorter.program());var b=backend.cooldowns(actor,longer.program());
        if(score(b)<=score(a))return false;
        long remaining=Math.max(0,backend.readyAt(actor,longer.program())-actor.level().getGameTime());
        return a.entrySet().stream().anyMatch(e->b.containsKey(e.getKey())&&e.getValue()>remaining);
    }
    public Plan select(LivingEntity actor,ItemStack tool,LivingEntity enemy) {
        if(actor.level().isClientSide||!actor.isAlive()||backend.busy(actor))return null;
        if(!ItemStack.matches(tool,toolSnapshot))clear();
        long now=actor.level().getGameTime();
        var candidates=new ArrayList<Plan>();
        for(var program:backend.programs(actor,tool)) {
            Spell spell=program.spell();var ability=backend.describe(actor,tool,spell);
            if(ability==null||ArsSpellAccess.parts(spell).stream().anyMatch(p->!p.isEnabled()))continue;
            var target=backend.recipient(actor,tool,enemy,program,ability);
            if(target==null)continue;
            var budget=backend.budget(actor,tool,program);
            if(!budget.possible())continue;
            candidates.add(new Plan(program,target,ability));
        }
        Plan chosen=null;
        if(plan!=null&&now-savingSince<=policy.maximumSavingTicks())for(var candidate:candidates)
            if(candidate.program().equals(plan.program())&&backend.readyAt(actor,candidate.program())<=now){chosen=candidate;break;}
        if(chosen==null) {
            double total=0;
            for(var candidate:candidates) {
                if(backend.readyAt(actor,candidate.program())>now||candidates.stream().anyMatch(other->other!=candidate&&delays(actor,candidate,other)))continue;
                double weight=candidate.program().slot()==0?policy.firstSlotWeight():1;
                if(lastSlot==candidate.program().slot())weight*=policy.repeatedWeight();
                total+=weight;
                if(actor.getRandom().nextDouble()*total<weight)chosen=candidate;
            }
            savingSince=now;
        }
        if(chosen==null){clear();return null;}
        plan=chosen;this.enemy=enemy;backend.select(actor,tool,chosen.program());toolSnapshot=tool.copy();
        if(!backend.budget(actor,tool,chosen.program()).affordable())return null;
        if(!backend.available(actor,tool,chosen.program())){clear();return null;}
        return chosen;
    }
    public boolean cast(LivingEntity actor,ItemStack tool,InteractionHand hand) {
        if(plan==null||actor.level().isClientSide||!actor.isAlive()||actor.getItemInHand(hand)!=tool
                ||!ItemStack.matches(tool,toolSnapshot)||backend.busy(actor))return false;
        var actual=backend.programs(actor,tool).stream().anyMatch(p->p.equals(plan.program()));
        if(!actual||backend.recipient(actor,tool,enemy,plan.program(),plan.ability())!=plan.recipient()||!plan.recipient().isAlive()||plan.recipient().level()!=actor.level()
                ||!backend.budget(actor,tool,plan.program()).affordable()||!backend.available(actor,tool,plan.program()))return false;
        if(!backend.cast(actor,tool,hand,plan.program()))return false;
        lastSlot=plan.program().slot();clear();return true;
    }
}
