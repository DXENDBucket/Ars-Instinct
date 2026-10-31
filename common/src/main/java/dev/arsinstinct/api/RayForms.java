package dev.arsinstinct.api;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSensitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/** Optional instant-ray geometry. Planning only; native methods still perform and pay for the cast. */
public final class RayForms {
    @FunctionalInterface public interface Range {double blocks(AbstractCastMethod method,SpellStats stats);}
    private static final Map<ResourceLocation,Range> RANGES=new HashMap<>();
    static {RANGES.put(ResourceLocation.parse("toomanyglyphs:glyph_ray"),RayForms::negRange);}
    /** Register on the server setup thread. Providers own the method's native range semantics. */
    public static void register(ResourceLocation method,Range range) {RANGES.put(Objects.requireNonNull(method),Objects.requireNonNull(range));}
    public static boolean supports(AbstractCastMethod method) {return method!=null&&RANGES.containsKey(method.getRegistryName());}
    public static double range(SpellContext context) {
        var method=context.getSpell().getCastMethod();var provider=method==null?null:RANGES.get(method.getRegistryName());
        if(provider==null)return 0;
        double value=provider.blocks(method,new SpellResolver(context).getCastStats());
        return Double.isFinite(value)&&value>0?value:0;
    }
    /** Aim at the target's eye and require it to be the native ray's first hit, including block clipping. */
    public static boolean canHit(LivingEntity actor,LivingEntity target,SpellContext context) {
        if(target==actor||!target.isAlive()||target.level()!=actor.level())return false;
        double range=range(context);var from=actor.getEyePosition();var offset=target.getEyePosition().subtract(from);
        if(range<=0||offset.lengthSqr()<1.0e-8||offset.lengthSqr()>range*range)return false;
        var stats=new SpellResolver(context).getCastStats();int sensitive=stats.getBuffCount(AugmentSensitive.INSTANCE);
        var to=from.add(offset.normalize().scale(range));
        var block=actor.level().clip(new ClipContext(from,to,sensitive>=1?ClipContext.Block.OUTLINE:ClipContext.Block.COLLIDER,
                sensitive>=2?ClipContext.Fluid.ANY:ClipContext.Fluid.NONE,actor));
        if(block.getType()!=HitResult.Type.MISS)to=block.getLocation();
        var hit=dev.arsinstinct.platform.NativeRays.firstHit(actor,from,to);
        return hit!=null&&hit.getEntity()==target;
    }
    // NEG exposes these config values on both loaders. No optional mod classes or loader config types in our API.
    private record Fields(Field base,Field bonus) {}
    private static final ClassValue<Optional<Fields>> NEG_FIELDS=new ClassValue<>() {
        @Override protected Optional<Fields> computeValue(Class<?> type) {
            try {return Optional.of(new Fields(type.getField("BASE_RANGE"),type.getField("BONUS_RANGE_PER_AUGMENT")));}
            catch(ReflectiveOperationException missing){return Optional.empty();}
        }
    };
    private static double negRange(AbstractCastMethod method,SpellStats stats) {
        var fields=NEG_FIELDS.get(method.getClass());if(fields.isEmpty())return 0;
        try {return value(fields.get().base().get(method))+value(fields.get().bonus().get(method))*stats.getAoeMultiplier();}
        catch(ReflectiveOperationException|RuntimeException unavailable){return 0;}
    }
    private static double value(Object config) {
        return config instanceof Supplier<?> supplier&&supplier.get() instanceof Number number?number.doubleValue():Double.NaN;
    }
    private RayForms() {}
}
