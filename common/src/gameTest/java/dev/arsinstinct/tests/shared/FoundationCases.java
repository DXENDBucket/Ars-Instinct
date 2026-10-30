package dev.arsinstinct.tests.shared;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.effect.*;
import dev.arsconflux.api.casting.*;
import dev.arsconflux.api.glyph.TriggerPaidGlyph;
import dev.arsconflux.api.resource.*;
import dev.arsinstinct.api.*;
import dev.arsinstinct.platform.TestTools;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class FoundationCases {
    private static ResourceLocation id(String s){return ResourceLocation.tryParse(s);}
    private static final PersistentCooldowns.Namespace STORE=PersistentCooldowns.register(id("ars_instinct:test_casting"),"ars_instinct_test_cd");
    private static boolean registered;
    private static void timing() {
        if(registered)return;registered=true;
        ActiveCasts.register(id("ars_instinct:test_casting"),new ActiveCastProvider(){
            public boolean supports(Spell spell){return true;}
            private boolean applies(ActiveCastRequest r){return r.caster().getTags().contains("instinct_test_timing");}
            public List<CooldownQuote> cooldowns(ActiveCastRequest r){return applies(r)?List.of(new CooldownQuote(id("ars_instinct:test_attack"),40,STORE.remaining(r.caster(),"attack"))):List.of();}
            public CastRejection rejection(ActiveCastRequest r){return applies(r)&&STORE.remaining(r.caster(),"attack")>0?new CastRejection(null,STORE.remaining(r.caster(),"attack")):null;}
            public void released(ActiveCastRequest r,List<Spell> spells){if(applies(r)){STORE.extendRaw(r.caster(),"attack",40);r.caster().getPersistentData().putInt("test_releases",r.caster().getPersistentData().getInt("test_releases")+1);}}
        });
    }
    public static void nativeStorageSurvivesReloadAndReconfiguration(GameTestHelper h) {
        var actor=EntityType.COW.create(h.getLevel());
        var pool=ArsManaPools.of(actor);h.assertTrue(pool!=null&&pool.current()==pool.maximum()&&pool.maximum()>0,"Fresh native mob pool starts full");
        pool.set(0);var restored=EntityType.COW.create(h.getLevel());restored.load(actor.saveWithoutId(new net.minecraft.nbt.CompoundTag()));
        var read=ArsManaPools.of(restored);h.assertTrue(read.current()==0,"Saved empty pool stays empty on reload");
        MobMana.configure(restored,new MobMana.Profile(300,7));h.assertTrue(read.current()==0&&read.maximum()==300,"Configuring an old pool does not refill");
        read.set(300);MobMana.configure(restored,new MobMana.Profile(100,7));h.assertTrue(read.current()==100&&read.maximum()==100,"Capacity shrink clamps actual native storage");
        var donor=EntityType.COW.create(h.getLevel());var from=ArsManaPools.of(donor);from.set(35);read.set(0);
        h.assertTrue(ArsManaPools.transfer(donor,restored,50)==35&&from.current()==0&&read.current()==35,"Native transfer conserves actual stored mana");
        read.set(.5);h.assertTrue(ArsManaPools.trySpend(restored,.5)&&read.current()==0,"Native fractional stock can be spent exactly");
        from.set(100);read.set(0);
        var reserved=ResourceCharge.exact(new ArsManaAccount(com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster.from(donor)),70);
        h.assertTrue(reserved.trigger(()->ArsManaPools.transfer(donor,restored,100)==30),"Transfers cannot reuse stock reserved by an active trigger");
        h.assertTrue(from.current()==0&&read.current()==30,"Reserved payment and transfer jointly conserve stock");
        h.succeed();
    }
    private static final class Paid extends AbstractEffect implements TriggerPaidGlyph {
        private final ResourceCharge charge;
        Paid(String name,ResourceCharge charge){super(id("ars_instinct:test_"+name),name);this.charge=charge;}
        protected java.util.Set<AbstractAugment> getCompatibleAugments(){return java.util.Set.of();}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}public SpellTier defaultTier(){return SpellTier.ONE;}
        public ResourceCharge firstCharge(SpellContext c,List<AbstractAugment> a){return charge;}
        public void onResolve(net.minecraft.world.phys.HitResult hit,net.minecraft.world.level.Level world,LivingEntity actor,SpellStats stats,SpellContext c,SpellResolver resolver){}
    }
    public static void estimatesMergePoolsAndKeepUnknownCosts(GameTestHelper h) {
        var actor=EntityType.COW.create(h.getLevel());
        var account=new ResourceAccount(){public Object reservationKey(){return "one_pool";}public long capacity(){return 100;}public boolean canSpend(int n){return n<=100;}public void spend(int n){throw new AssertionError("A quote must not spend");}};
        var a=new Paid("a",ResourceCharge.exact(account,60));var b=new Paid("b",ResourceCharge.exact(account,50));
        var quote=SpellBudgets.estimate(SpellContext.fromEntity(new Spell(MethodSelf.INSTANCE,a,b),actor,ItemStack.EMPTY));
        h.assertTrue(quote.complete()&&!quote.possible()&&!quote.affordable(),"Two prices in one physical pool cannot each reuse its full balance");
        var unknown=new Paid("unknown",null);
        h.assertTrue(!SpellBudgets.estimate(SpellContext.fromEntity(new Spell(MethodSelf.INSTANCE,unknown),actor,ItemStack.EMPTY)).complete(),"Unknown prices stay unknown");
        boolean previous=dev.arsconflux.config.ConfluxConfig.VANILLA_TRIGGER_BILLING.get();
        try {
            dev.arsconflux.config.ConfluxConfig.VANILLA_TRIGGER_BILLING.set(true);
            var nativeQuote=SpellBudgets.estimate(SpellContext.fromEntity(new Spell(MethodProjectile.INSTANCE,EffectHarm.INSTANCE),actor,ItemStack.EMPTY));
            h.assertTrue(nativeQuote.pools().stream().mapToLong(SpellBudgets.Pool::amount).sum()>0,"Trigger billing still estimates the future native damage fee");
            ArsManaPools.of(actor).set(0);h.assertTrue(!nativeQuote.affordable(),"Deferred vanilla damage does not become a free AI quote");
        }finally{dev.arsconflux.config.ConfluxConfig.VANILLA_TRIGGER_BILLING.set(previous);}
        h.succeed();
    }
    public static void optInInstallationIsScopedAndIdempotent(GameTestHelper h) {
        TestTools.installationCheck(h);
        h.succeed();
    }
    public static void ordinaryMobRegeneratesAttacksHealsAndUsesProviderCooldown(GameTestHelper h) { ordinaryMobCombat(h,false); }
    public static void ordinaryMobTriggerBilling(GameTestHelper h) { ordinaryMobCombat(h,true); }
    private static void ordinaryMobCombat(GameTestHelper h,boolean triggerBilling) {

        timing();
        var undead=EntityType.HUSK.create(h.getLevel());
        h.assertTrue(!new NativeSpellBackend().useful(undead,undead,new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE)),"Native healing is not useful on undead recipients");
        // Forge's test world terrain can extend far above the template origin. Clear a real arena.
        for(int x=0;x<=4;x++)for(int y=7;y<=12;y++)for(int z=0;z<=12;z++)h.setBlock(x,y,z,net.minecraft.world.level.block.Blocks.AIR);
        var actor=h.spawn(EntityType.PILLAGER,2,8,2);actor.setNoAi(true);actor.setNoGravity(true);
        var enemy=h.spawn(EntityType.COW,2,8,9);enemy.setNoAi(true);enemy.setNoGravity(true);
        var chunk=actor.chunkPosition();
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.getLevel().setChunkForced(chunk.x+x,chunk.z+z,true);
        actor.setTarget(enemy);actor.addTag("instinct_test_timing");
        MobMana.configure(actor,new MobMana.Profile(200,100));var pool=ArsManaPools.of(actor);pool.set(0);
        actor.setItemSlot(EquipmentSlot.MAINHAND,TestTools.book(new Spell(MethodProjectile.INSTANCE,EffectHarm.INSTANCE)));
        var backend=new NativeSpellBackend(){
            @Override protected SpellContext context(LivingEntity actor,ItemStack tool,Spell spell) {
                var c=super.context(actor,tool,spell);
                // Isolated native mode snapshots let both test batches run concurrently.
                c.tag.putBoolean("ars_conflux_trigger_billing",triggerBilling);return c;
            }
        };
        var goal=new SpellCombatGoal(actor,1,backend);goal.start();
        h.assertTrue(backend.programs(actor,actor.getMainHandItem()).get(0).equals(backend.programs(actor,actor.getMainHandItem()).get(0)),
                "Independent reads of the same tool retain semantic program identity");
        h.assertTrue(goal.canUse(),"Ordinary mob with a written book can use the shared goal");
        goal.tick();h.assertTrue(actor.getPersistentData().getInt("test_releases")==0,"Empty pool does not cast for free");
        float before=enemy.getHealth();float[] health=new float[1];
        h.startSequence().thenWaitUntil(()->{goal.tick();h.assertTrue(enemy.getHealth()<before,"Wait for native projectile damage: mana="+pool.current()+"/"+pool.maximum()+", block="+enemy.level().getBlockState(enemy.blockPosition())+", tags="+ProgramIntent.of(new Spell(MethodProjectile.INSTANCE,EffectHarm.INSTANCE)).tags()+", available="+backend.available(actor,actor.getMainHandItem(),new CombatBackend.Program(0,new Spell(MethodProjectile.INSTANCE,EffectHarm.INSTANCE)))+", regen="+pool.regeneration()+", releases="+actor.getPersistentData().getInt("test_releases")+", health="+enemy.getHealth()+", target="+(actor.getTarget()==enemy));})
            .thenExecute(()->{
                h.assertTrue(enemy.getLastHurtByMob()==actor,"Real mob damage owner: observed="+enemy.getLastHurtByMob()+", expected="+actor+", source="+enemy.getLastDamageSource());
                h.assertTrue(actor.getPersistentData().getInt("test_releases")==1,"Shared provider acknowledges one actual release");
                var spell=new Spell(MethodProjectile.INSTANCE,EffectHarm.INSTANCE);
                h.assertTrue(ActiveCasts.rejection(spell,actor,ActiveCastSource.CASTER_TOOL)!=null,"Native backend sees provider cooldown");
                h.assertTrue(backend.cooldowns(actor,new CombatBackend.Program(0,spell)).containsKey(id("ars_instinct:test_attack")),"AI sees provider shared keys");
                goal.stop();actor.removeTag("instinct_test_timing");actor.setHealth(actor.getMaxHealth()-8);health[0]=actor.getHealth();pool.set(200);
                actor.setItemSlot(EquipmentSlot.MAINHAND,TestTools.book(new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE)));goal.start();
            }).thenWaitUntil(()->{goal.tick();h.assertTrue(actor.getHealth()>health[0],"Wounded mob uses native self heal");})
            .thenExecute(()->{h.assertTrue(pool.current()<200,"Native self heal pays real mana");goal.stop();}).thenSucceed();
    }
    private FoundationCases() {}
}
