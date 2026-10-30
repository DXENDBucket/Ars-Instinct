package dev.arsinstinct.tests;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.effect.*;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import dev.arsconflux.api.glyph.*;
import dev.arsinstinct.api.*;
import dev.arsinstinct.platform.NativePrograms;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("ars_instinct") @PrefixGameTestTemplate(false)
public final class InstinctGameTests {
    private static final Spell ATTACK=new Spell(MethodProjectile.INSTANCE,EffectHarm.INSTANCE);
    private static final Spell STRONG=new Spell(MethodProjectile.INSTANCE,EffectHarm.INSTANCE,AugmentAmplify.INSTANCE);
    private static ItemStack book() {
        var tool=new ItemStack(com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry.NOVICE_SPELLBOOK.get());
        new SpellCaster(10).setSpell(ATTACK,0).setSpell(STRONG,1).saveToStack(tool);return tool;
    }
    private static final class Fixture extends NativeSpellBackend {
        int balance,casts;
        @Override public Budget budget(LivingEntity actor,ItemStack tool,Program p){return new Budget(true,balance>=(p.slot()==1?100:10));}
        @Override public boolean available(LivingEntity actor,ItemStack tool,Program p){return true;}
        @Override public boolean busy(LivingEntity actor){return false;}
        @Override public Map<ResourceLocation,Integer> cooldowns(LivingEntity actor,Program p){return Map.of(ResourceLocation.parse("test:shared"),p.slot()==1?100:10);}
        @Override public boolean cast(LivingEntity actor,ItemStack tool,InteractionHand hand,Program p){casts++;return true;}
    }
    @GameTest(template="empty",timeoutTicks=50)
    public static void longCombinationSavesManaAndChangedToolCannotRelease(GameTestHelper h) {
        var actor=h.spawn(EntityType.ZOMBIE,2,3,2);actor.setNoAi(true);
        var enemy=h.spawn(EntityType.COW,2,3,12);enemy.setNoAi(true);
        var tool=book();actor.setItemSlot(EquipmentSlot.MAINHAND,tool);
        var backend=new Fixture();var controller=new SpellCombatController(backend);
        backend.balance=10;
        h.assertTrue(controller.select(actor,tool,enemy)==null,"Reserve for shared longer combination instead of draining cheap shots");
        h.assertTrue(controller.current()!=null&&controller.current().program().slot()==1,"Selected the longer combination");
        backend.balance=100;h.assertTrue(controller.select(actor,tool,enemy)!=null,"Reserved spell becomes affordable");
        actor.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STICK));
        h.assertTrue(!controller.cast(actor,tool,InteractionHand.MAIN_HAND)&&backend.casts==0,"A stale hand snapshot never releases");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=50)
    public static void labelsUseFirstAbilityAndUnlabelledSpellsAreSkipped(GameTestHelper h) {
        var heal=new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE,EffectHarm.INSTANCE);
        h.assertTrue(ProgramIntent.of(heal).has(AbilityTags.HEALING),"First effect controls intent, not later damage");
        h.assertTrue(!ProgramIntent.of(heal).has(AbilityTags.ATTACK),"No whole-program union");
        var actor=h.spawn(EntityType.ZOMBIE,2,3,2);actor.setNoAi(true);var enemy=h.spawn(EntityType.COW,2,3,12);enemy.setNoAi(true);
        var tool=book();new SpellCaster(10).setSpell(new Spell(MethodProjectile.INSTANCE,EffectBreak.INSTANCE),0).saveToStack(tool);
        actor.setItemSlot(EquipmentSlot.MAINHAND,tool);
        var backend=new Fixture();backend.balance=100;
        h.assertTrue(new SpellCombatController(backend).select(actor,tool,enemy)==null,"Unknown semantic hints do not become arbitrary attacks");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=50)
    public static void fullHealthSelfHealDoesNotFireForWoundedFriend(GameTestHelper h) {
        var actor=h.spawn(EntityType.ZOMBIE,2,3,2);actor.setNoAi(true);
        var friend=h.spawn(EntityType.ZOMBIE,3,3,2);friend.setNoAi(true);friend.setHealth(5);
        h.assertTrue(CombatTargets.recipient(actor,null,new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE),12,e->true)==null,
                "A non-area self heal does not fire just because a nearby friend is wounded");
        actor.setHealth(5);
        h.assertTrue(CombatTargets.recipient(actor,null,new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE),12,e->true)==actor,"Wounded caster can heal itself");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=50)
    public static void nativeBackendUsesRealManaAndRejectsEmptyBalance(GameTestHelper h) {
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"InstinctMana"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var mana=com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player);
        var tool=book();player.setItemSlot(EquipmentSlot.MAINHAND,tool);
        mana.setMaxMana(500);mana.setMana(500);
        var backend=new NativeSpellBackend();var program=NativePrograms.programs(tool).getFirst();
        h.assertTrue(backend.budget(player,tool,program).affordable(),"Real native account can fund a cast");
        h.assertTrue(backend.cast(player,tool,InteractionHand.MAIN_HAND,program),"Native resolver accepts the actual tool and caster");
        h.assertTrue(mana.getCurrentMana()<500,"Actual resolver pays real Ars mana");
        mana.setMana(0);h.assertTrue(!backend.budget(player,tool,program).affordable(),"An empty native account is not free NPC casting");
        player.discard();h.succeed();
    }
}
