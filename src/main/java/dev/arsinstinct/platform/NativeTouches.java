package dev.arsinstinct.platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;

/** Normal interaction targets. An attached, unpickable firework must not intercept touch casts. */
public final class NativeTouches {
    public static EntityHitResult firstHit(LivingEntity actor,Vec3 from,Vec3 to) {
        return ProjectileUtil.getEntityHitResult(actor.level(),actor,from,to,new AABB(from,to).inflate(1),
                entity->entity!=actor&&entity.isAlive()&&!entity.isSpectator()&&entity.isPickable(),0);
    }
    private NativeTouches() {}
}
