package dev.arsinstinct.platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;

public final class NativeRays {
    public static EntityHitResult firstHit(LivingEntity actor,Vec3 from,Vec3 to) {
        return ProjectileUtil.getEntityHitResult(actor.level(),actor,from,to,new AABB(from,to).inflate(1.5),
                entity->entity!=actor&&entity.isAlive(),0);
    }
    private NativeRays() {}
}
