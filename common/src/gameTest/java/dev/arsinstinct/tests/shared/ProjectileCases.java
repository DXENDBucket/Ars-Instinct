package dev.arsinstinct.tests.shared;

import dev.arsinstinct.api.ProjectileAwareness;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import java.util.List;

public final class ProjectileCases {
    private static Projectile arrow(GameTestHelper h,LivingEntity actor,Entity owner,Vec3 velocity) {
        var arrow=EntityType.ARROW.create(h.getLevel());arrow.setOwner(owner);arrow.setNoGravity(true);
        arrow.setPos(actor.getEyePosition().add(0,0,10));arrow.setDeltaMovement(velocity);h.getLevel().addFreshEntity(arrow);return arrow;
    }
    private static void clearLane(GameTestHelper h) {
        for(int x=1;x<=3;x++)for(int y=7;y<=11;y++)for(int z=1;z<=14;z++)h.setBlock(x,y,z,Blocks.AIR);
    }
    public static void originsAndIncoming(GameTestHelper h) {
        clearLane(h);
        var actor=h.spawn(EntityType.COW,2,8,2);actor.setNoAi(true);actor.setNoGravity(true);
        var enemy=h.spawn(EntityType.COW,6,8,2);enemy.setNoAi(true);
        var servant=h.spawn(EntityType.COW,7,8,2);servant.setNoAi(true);
        var incoming=arrow(h,actor,enemy,new Vec3(0,0,-1));
        var outgoing=arrow(h,actor,enemy,new Vec3(0,0,1));
        var self=arrow(h,actor,actor,new Vec3(0,0,-1));
        var friend=arrow(h,actor,servant,new Vec3(0,0,-1));
        var unknown=arrow(h,actor,null,new Vec3(0,0,-1));
        var sensor=new ProjectileAwareness(ProjectileAwareness.Policy.DEFAULT,entity->entity==servant?actor:null);
        var snapshot=sensor.tick(actor);
        h.assertTrue(snapshot.observations().stream().anyMatch(o->o.projectile()==incoming&&o.incoming()
                &&o.origin()==ProjectileAwareness.Origin.NON_ALLIED),"Incoming non-allied projectile is recognized");
        h.assertTrue(snapshot.observations().stream().anyMatch(o->o.projectile()==outgoing&&!o.incoming()),"Nearby outgoing projectile is noticed but not incoming");
        h.assertTrue(snapshot.observations().stream().anyMatch(o->o.projectile()==unknown&&o.incoming()
                &&o.origin()==ProjectileAwareness.Origin.UNKNOWN),"Unknown origin stays unknown, not declared an enemy caster");
        h.assertTrue(snapshot.observations().stream().noneMatch(o->o.projectile()==self||o.projectile()==friend),"Self and owned ally projectiles are excluded");
        h.setBlock(2,9,6,Blocks.STONE);sensor.clear();
        h.assertTrue(sensor.tick(actor).observations().stream().anyMatch(o->o.projectile()==incoming&&!o.incoming()),"An intervening wall stops predicted collision, not detection");
        for(var p:List.of(incoming,outgoing,self,friend,unknown))p.discard();actor.discard();enemy.discard();servant.discard();sensor.clear();h.succeed();
    }
    public static void cachedAndBounded(GameTestHelper h) {
        clearLane(h);var actor=h.spawn(EntityType.COW,2,8,2);actor.setNoAi(true);actor.setNoGravity(true);
        var a=arrow(h,actor,null,new Vec3(0,0,-1));var b=arrow(h,actor,null,new Vec3(0,0,-1));
        var sensor=new ProjectileAwareness(new ProjectileAwareness.Policy(32,4,20,.5,1),entity->null);
        var first=sensor.tick(actor);h.assertTrue(first.saturated()&&first.observations().size()<=1,"Candidate cap is explicit and reported");
        h.assertTrue(sensor.tick(actor)==first&&sensor.snapshot()==first,"Repeated reads do not trigger a second scan");
        sensor.clear();h.assertTrue(sensor.snapshot().observations().isEmpty(),"Lifecycle clear drops cached entity references");
        a.discard();b.discard();actor.discard();h.succeed();
    }
    private ProjectileCases() {}
}
