package dev.arsinstinct.api;

import dev.arsconflux.api.targeting.Allies;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.Function;

/** Caller-owned perception, not an automatic dodge or damage permission. No global actor/entity cache. */
public final class ProjectileAwareness {
    public record Policy(double radius,int intervalTicks,int horizonTicks,double margin,int maximumCandidates) {
        public static final Policy DEFAULT=new Policy(32,4,20,.5,128);
        public Policy {
            if(!Double.isFinite(radius)||radius<=0||intervalTicks<1||horizonTicks<1||!Double.isFinite(margin)
                    ||margin<0||maximumCandidates<1)throw new IllegalArgumentException("Invalid projectile perception policy");
        }
    }
    public enum Origin {NON_ALLIED,UNKNOWN}
    /** Incoming/impact time are short linear estimates, not knowledge of future homing or payload damage. */
    public record Observation(Projectile projectile,Entity source,Origin origin,Vec3 position,Vec3 velocity,
                              double distanceSquared,double impactTicks) {
        public boolean incoming(){return Double.isFinite(impactTicks);}
        public boolean validFor(LivingEntity actor){return projectile.isAlive()&&projectile.level()==actor.level();}
    }
    public record Snapshot(long observedAt,boolean saturated,List<Observation> observations) {
        public static final Snapshot EMPTY=new Snapshot(Long.MIN_VALUE,false,List.of());
        public Snapshot {observations=List.copyOf(observations);}
    }
    private final Policy policy;
    private final Function<Entity,Entity> ownerLookup;
    private Level world;
    private long nextScan;
    private Snapshot snapshot=Snapshot.EMPTY;
    public ProjectileAwareness(){this(Policy.DEFAULT,ProjectileAwareness::nativeOwner);}
    public ProjectileAwareness(Policy policy,Function<Entity,Entity> ownerLookup) {
        this.policy=Objects.requireNonNull(policy);this.ownerLookup=Objects.requireNonNull(ownerLookup);
    }
    private static Entity nativeOwner(Entity entity) {
        return entity instanceof Projectile p?p.getOwner():entity instanceof OwnableEntity own?own.getOwner():null;
    }
    public Snapshot snapshot(){return snapshot;}
    public void clear(){world=null;nextScan=0;snapshot=Snapshot.EMPTY;}
    public Snapshot tick(LivingEntity actor) {
        if(!(actor.level() instanceof ServerLevel level)||!actor.isAlive()||actor.isRemoved()){clear();return snapshot;}
        if(world!=level){clear();world=level;}
        long now=level.getGameTime();if(now<nextScan)return snapshot;
        nextScan=now+policy.intervalTicks();
        var found=new ArrayList<Projectile>();
        // This overload uses the spatial entity index and aborts at the configured candidate budget.
        level.getEntities().get(EntityTypeTest.forClass(Projectile.class),actor.getBoundingBox().inflate(policy.radius()),projectile->{
            found.add(projectile);
            return found.size()>=policy.maximumCandidates()?AbortableIterationConsumer.Continuation.ABORT:AbortableIterationConsumer.Continuation.CONTINUE;
        });
        var observations=new ArrayList<Observation>();
        for(var projectile:found) {
            if(!projectile.isAlive()||projectile.isSpectator()||projectile.distanceToSqr(actor)>policy.radius()*policy.radius())continue;
            Entity source=projectile.getOwner();
            if(friendly(actor,source))continue;
            var position=projectile.position();var velocity=projectile.getDeltaMovement();
            double impact=impact(actor,projectile,position,velocity);
            observations.add(new Observation(projectile,source,source==null?Origin.UNKNOWN:Origin.NON_ALLIED,
                    position,velocity,projectile.distanceToSqr(actor),impact));
        }
        observations.sort(Comparator.comparingDouble(Observation::impactTicks).thenComparingDouble(Observation::distanceSquared));
        snapshot=new Snapshot(now,found.size()>=policy.maximumCandidates(),observations);return snapshot;
    }
    private boolean friendly(LivingEntity actor,Entity source) {
        var visited=new HashSet<UUID>();
        for(int depth=0;source!=null&&depth<16&&visited.add(source.getUUID());depth++) {
            if(source.level()!=actor.level())return false;
            if(Allies.areAllies(actor,source))return true;
            source=ownerLookup.apply(source);
        }
        return false;
    }
    private double impact(LivingEntity actor,Projectile projectile,Vec3 position,Vec3 velocity) {
        var relative=velocity.subtract(actor.getDeltaMovement());
        double size=projectile.getBbWidth()*.5+policy.margin();
        var body=actor.getBoundingBox().inflate(size);
        double time;
        if(body.contains(position))time=0;
        else {
            if(relative.lengthSqr()<1.0E-8)return Double.POSITIVE_INFINITY;
            var hit=body.clip(position,position.add(relative.scale(policy.horizonTicks())));
            if(hit.isEmpty())return Double.POSITIVE_INFINITY;
            time=Math.max(0,hit.get().subtract(position).dot(relative)/relative.lengthSqr());
        }
        if(time>0&&actor.level().clip(new ClipContext(position,position.add(velocity.scale(time)),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,projectile)).getType()!=HitResult.Type.MISS)
            return Double.POSITIVE_INFINITY;
        return time;
    }
}
