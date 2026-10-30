package dev.arsinstinct.api;

import dev.arsinstinct.platform.NativePrograms;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/** Explicitly install on a caller-chosen mob. Existing targets/navigation remain native. */
public final class SpellCombatGoal extends Goal {
    private final Mob mob;
    private final CombatBackend backend;
    private final SpellCombatController controller;
    private final double speed;
    private int decisionDelay,pathDelay,meleeDelay;
    private SpellCombatController.Plan last;
    public SpellCombatGoal(Mob mob,double speed) {this(mob,speed,new NativeSpellBackend());}
    public SpellCombatGoal(Mob mob,double speed,CombatBackend backend) {
        this.mob=mob;this.speed=speed;this.backend=backend;controller=new SpellCombatController(backend);
        setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));
    }
    @Override public boolean canUse() {return mob.getTarget()!=null&&mob.getTarget().isAlive()
            &&!backend.programs(mob,mob.getMainHandItem()).isEmpty();}
    @Override public boolean canContinueToUse() {return canUse();}
    @Override public boolean requiresUpdateEveryTick() {return true;}
    @Override public void start() {decisionDelay=mob.getId()%5;mob.setAggressive(true);}
    @Override public void stop() {controller.clear();last=null;mob.getNavigation().stop();mob.setAggressive(false);}
    @Override public void tick() {
        var enemy=mob.getTarget();if(enemy==null)return;
        var tool=mob.getMainHandItem();
        if(meleeDelay>0)meleeDelay--;
        if(backend.busy(mob)) {
            mob.getNavigation().stop();
            var recipient=last==null||last.recipient()==mob?enemy:last.recipient();
            if(recipient.isAlive())aim(mob,backend.aimOrigin(mob,tool),recipient.getEyePosition(),last==null?null:last.ability());
            return;
        }
        if(--decisionDelay<=0) {
            decisionDelay=5;
            var plan=controller.select(mob,tool,enemy);
            if(plan!=null) {
                aim(mob,backend.aimOrigin(mob,tool),(plan.recipient()==mob?enemy:plan.recipient()).getEyePosition(),plan.ability());
                if(controller.cast(mob,tool,net.minecraft.world.InteractionHand.MAIN_HAND)){last=plan;mob.getNavigation().stop();return;}
            }
        }
        mob.getLookControl().setLookAt(enemy,30,30);
        if(NativePrograms.inMelee(mob,enemy)&&CombatTargets.visible(mob,enemy)) {
            mob.getNavigation().stop();
            if(meleeDelay<=0) {
                mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);mob.doHurtTarget(enemy);
                double attacks=mob.getAttributes().hasAttribute(Attributes.ATTACK_SPEED)?mob.getAttributeValue(Attributes.ATTACK_SPEED):1.6;
                meleeDelay=Math.max(1,(int)Math.ceil(20/Math.max(.1,attacks)));
            }
        } else if(--pathDelay<=0) {pathDelay=10;mob.getNavigation().moveTo(enemy,speed);}
    }
    public static void aim(Mob mob,Vec3 origin,Vec3 target,SpellAbility ability) {
        var offset=target.subtract(origin);
        if(ability!=null&&ability.gravity()>0)offset=ballistic(offset,ability.projectileSpeed(),ability.gravity());
        float yaw=(float)Math.toDegrees(Math.atan2(offset.z,offset.x))-90;
        float pitch=(float)-Math.toDegrees(Math.atan2(offset.y,offset.horizontalDistance()));
        mob.setYRot(yaw);mob.setYHeadRot(yaw);mob.yBodyRot=yaw;mob.setXRot(pitch);
        var look=mob.getEyePosition().add(offset);
        mob.getLookControl().setLookAt(look.x,look.y,look.z,180,180);
    }
    public static Vec3 ballistic(Vec3 offset,double speed,double gravity) {
        double horizontal=offset.horizontalDistance(),v2=speed*speed;
        double discriminant=v2*v2-gravity*(gravity*horizontal*horizontal+2*offset.y*v2);
        if(horizontal<.001||speed<=0||gravity<=0||discriminant<0)return offset;
        return new Vec3(offset.x,horizontal*(v2-Math.sqrt(discriminant))/(gravity*horizontal),offset.z);
    }
}
