package dev.arsinstinct.platform;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import dev.arsinstinct.api.*;
public final class TestTools {
 public static ItemStack book(Spell... spells){
  var tool=new ItemStack(com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry.NOVICE_SPELLBOOK.get());
  var caster=new com.hollingsworth.arsnouveau.api.spell.SpellCaster(10);for(int i=0;i<spells.length;i++)caster=caster.setSpell(spells[i],i);caster.saveToStack(tool);
  return tool;
 }
 public static void installationCheck(GameTestHelper h){
  boolean previous=InstinctConfig.ENABLED.get();var ids=InstinctConfig.ENTITY_TYPES.get();
  try{
   InstinctConfig.ENABLED.set(false);InstinctConfig.ENTITY_TYPES.set(java.util.List.of("minecraft:pig"));
   var disabled=h.spawn(EntityType.PIG,2,8,2);disabled.setNoAi(true);
   h.assertTrue(disabled.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof SpellCombatGoal),"Default disabled does not install AI");
   InstinctConfig.ENABLED.set(true);
   var enabled=h.spawn(EntityType.PIG,3,8,2);enabled.setNoAi(true);
   h.assertTrue(enabled.goalSelector.getAvailableGoals().stream().anyMatch(g->g.getGoal() instanceof SpellCombatGoal),"Explicit listed type installs shared AI");
   h.assertTrue(!InstinctCasting.install(enabled,1,1),"Repeated join/install does not duplicate the goal");
   var other=h.spawn(EntityType.COW,4,8,2);other.setNoAi(true);
   h.assertTrue(other.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof SpellCombatGoal),"Unselected types retain their native behavior");
  }finally{InstinctConfig.ENABLED.set(previous);InstinctConfig.ENTITY_TYPES.set(ids);}
 }
 private TestTools(){}
}
