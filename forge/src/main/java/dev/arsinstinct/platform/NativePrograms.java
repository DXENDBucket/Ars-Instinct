package dev.arsinstinct.platform;
import com.hollingsworth.arsnouveau.api.util.CasterUtil;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsinstinct.api.CombatBackend;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import java.util.*;
public final class NativePrograms {
    public static List<CombatBackend.Program> programs(ItemStack tool) {
        var caster=CasterUtil.getCaster(tool);if(caster==null)return List.of();
        var result=new ArrayList<CombatBackend.Program>();
        for(int i=0;i<caster.getMaxSlots();i++){var spell=caster.getSpell(i);if(spell!=null&&!spell.isEmpty())result.add(new CombatBackend.Program(i,spell));}
        return result;
    }
    public static void select(ItemStack tool,int slot) {var caster=CasterUtil.getCaster(tool);if(caster!=null)caster.setCurrentSlot(slot);}
    public static double maximumMana(LivingEntity actor) {return CapabilityRegistry.getMana(actor).resolve().map(cap->(double)cap.getMaxMana()).orElse(-1.0);}
    public static boolean inMelee(Mob actor,LivingEntity target) {double reach=actor.getBbWidth()*2;return actor.distanceToSqr(target)<=reach*reach+target.getBbWidth();}
    private NativePrograms() {}
}
