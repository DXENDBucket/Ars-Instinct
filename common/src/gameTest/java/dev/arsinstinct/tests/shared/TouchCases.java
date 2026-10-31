package dev.arsinstinct.tests.shared;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.arsconflux.api.resource.*;
import dev.arsinstinct.api.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Real native touch resolution: both billing modes, empty aim, an occluder and an attached rocket. */
public final class TouchCases {
    public static void nativeInteractions(GameTestHelper h,boolean triggerBilling) {
        for(int x=0;x<=4;x++)for(int y=7;y<=15;y++)for(int z=0;z<=7;z++)h.setBlock(x,y,z,Blocks.AIR);
        var actor=h.spawn(EntityType.COW,2,8,2);actor.setNoAi(true);actor.setNoGravity(true);
        var target=h.spawn(EntityType.COW,2,8,4);target.setNoAi(true);target.setNoGravity(true);
        MobMana.configure(actor,new MobMana.Profile(2000,0));var pool=ArsManaPools.of(actor);pool.set(2000);
        var backend=new NativeSpellBackend(){
            @Override protected SpellContext context(LivingEntity caster,ItemStack tool,Spell spell) {
                var c=super.context(caster,tool,spell);c.tag.putBoolean("ars_conflux_trigger_billing",triggerBilling);return c;
            }
        };
        var program=new CombatBackend.Program(0,new Spell(MethodTouch.INSTANCE,EffectHarm.INSTANCE));
        var hand=InteractionHand.MAIN_HAND;var tool=actor.getItemInHand(hand);
        var rocket=new FireworkRocketEntity(h.getLevel(),new ItemStack(Items.FIREWORK_ROCKET),actor);
        h.getLevel().addFreshEntity(rocket);
        SpellCombatGoal.aim(actor,actor.getEyePosition(),target.getEyePosition(),null);
        float before=target.getHealth();double balance=pool.current();
        h.assertTrue(backend.cast(actor,tool,hand,program),"Touch uses native entity interaction, not failing generic onCast");
        h.assertTrue(target.getHealth()<before&&pool.current()<balance,"Native damage and real mana expenditure both occur");
        h.assertTrue(target.getLastHurtByMob()==actor,"Damage belongs to actual caster, not a synthetic player");
        rocket.discard();
        target.invulnerableTime=0;before=target.getHealth();
        h.setBlock(2,8,3,Blocks.STONE);h.setBlock(2,9,3,Blocks.STONE);
        h.assertTrue(backend.cast(actor,tool,hand,program),"Block interaction is a valid native touch resolution");
        h.assertTrue(target.getHealth()==before,"A wall stops entity interaction instead of auto-targeting through it");
        actor.setXRot(-90);balance=pool.current();
        h.assertTrue(!backend.cast(actor,tool,hand,program)&&pool.current()==balance,"An empty touch aim neither resolves nor charges");
        actor.setXRot(0);pool.set(0);
        h.assertTrue(!backend.cast(actor,tool,hand,program)&&pool.current()==0,"Native empty balance is never a free cast");
        actor.discard();target.discard();h.succeed();
    }
    private TouchCases() {}
}
