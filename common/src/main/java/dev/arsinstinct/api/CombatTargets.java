package dev.arsinstinct.api;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.common.spell.method.MethodSelf;
import dev.arsconflux.api.glyph.AbilityTags;
import dev.arsconflux.api.targeting.Allies;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import java.util.function.Predicate;

public final class CombatTargets {
    public static boolean wounded(LivingEntity entity) {return entity.getHealth()<entity.getMaxHealth()-.5f;}
    public static boolean visible(LivingEntity actor,LivingEntity target) {
        return !(actor instanceof Mob mob)||mob.getSensing().hasLineOfSight(target);
    }
    public static boolean clearRay(LivingEntity actor,LivingEntity target) {
        if(!visible(actor,target))return false;
        var start=actor.getEyePosition();var end=target.getEyePosition();
        var hit=ProjectileUtil.getEntityHitResult(actor.level(),actor,start,end,new AABB(start,end).inflate(1.5),
                e->e!=actor&&e.isAlive()&&!e.isSpectator(),0);
        return hit!=null&&hit.getEntity()==target;
    }
    public static LivingEntity recipient(LivingEntity actor,LivingEntity enemy,Spell spell,double range,
                                        Predicate<LivingEntity> useful) {
        var intent=ProgramIntent.of(spell);
        if(!intent.known()||intent.has(AbilityTags.PLAYER_ONLY)&&!(actor instanceof net.minecraft.world.entity.player.Player))return null;
        boolean healing=intent.has(AbilityTags.HEALING);
        if(spell.getCastMethod()==MethodSelf.INSTANCE) {
            if(!intent.has(AbilityTags.SELF))return null;
            if(!healing&&(enemy==null||!enemy.isAlive()||actor.distanceTo(enemy)>range))return null;
            if(healing&&!wounded(actor)) {
                if(!intent.has(AbilityTags.AREA))return null;
                boolean nearby=!actor.level().getEntitiesOfClass(LivingEntity.class,actor.getBoundingBox().inflate(5),
                        e->e.isAlive()&&wounded(e)&&Allies.areAllies(actor,e)&&actor.distanceToSqr(e)<=25).isEmpty();
                if(!nearby)return null;
            }
            return useful.test(actor)?actor:null;
        }
        if(intent.friendly()) {
            LivingEntity best=null;double score=-Double.MAX_VALUE;
            for(var friend:actor.level().getEntitiesOfClass(LivingEntity.class,actor.getBoundingBox().inflate(range),
                    e->e!=actor&&e.isAlive()&&!e.isSpectator()&&Allies.areAllies(actor,e)
                            &&actor.getEyePosition().distanceToSqr(e.getEyePosition())<=range*range)) {
                if(healing&&!wounded(friend)||!useful.test(friend)||!clearRay(actor,friend))continue;
                if(!healing&&(enemy==null||!enemy.isAlive()))continue;
                double value=enemy!=null&&enemy.isAlive()?-friend.distanceToSqr(enemy)
                        :100*(1-friend.getHealth()/friend.getMaxHealth())-actor.distanceTo(friend);
                if(value>score){best=friend;score=value;}
            }
            return best;
        }
        return intent.has(AbilityTags.ENEMY)&&enemy!=null&&enemy.isAlive()&&!Allies.areAllies(actor,enemy)
                &&actor.distanceTo(enemy)<=range&&visible(actor,enemy)&&useful.test(enemy)?enemy:null;
    }
    private CombatTargets() {}
}
