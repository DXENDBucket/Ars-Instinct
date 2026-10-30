package dev.arsinstinct.platform;

import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import dev.arsinstinct.api.CombatBackend.Program;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class NativePrograms {
    public static List<Program> programs(ItemStack tool) {
        var caster=SpellCasterRegistry.from(tool);if(caster==null)return List.of();
        var result=new ArrayList<Program>();
        for(int slot=0;slot<caster.getMaxSlots();slot++) {
            var spell=caster.getSpell(slot);if(!spell.isEmpty())result.add(new Program(slot,spell));
        }
        return List.copyOf(result);
    }
    public static void select(ItemStack tool,int slot) {
        var caster=SpellCasterRegistry.from(tool);if(caster!=null)caster.setCurrentSlot(slot).saveToStack(tool);
    }
    public static double maximumMana(LivingEntity actor) {
        var cap=com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(actor);
        return cap==null?-1:cap.getMaxMana();
    }
    public static boolean inMelee(Mob actor,LivingEntity target) {return actor.isWithinMeleeAttackRange(target);}
    private NativePrograms() {}
}
