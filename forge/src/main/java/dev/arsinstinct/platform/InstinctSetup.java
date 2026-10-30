package dev.arsinstinct.platform;
import dev.arsinstinct.api.InstinctCasting;
import dev.arsconflux.api.resource.MobMana;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
public final class InstinctSetup {
    public static void register() {net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(InstinctSetup::joined);}
    private static void joined(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide||!(event.getEntity() instanceof Mob actor)||!InstinctConfig.ENABLED.get())return;
        String id=BuiltInRegistries.ENTITY_TYPE.getKey(actor.getType()).toString();
        if(!actor.getType().is(InstinctCasting.SPELLCASTERS)&&!InstinctConfig.ENTITY_TYPES.get().contains(id))return;
        if(InstinctCasting.install(actor,InstinctConfig.GOAL_PRIORITY.get(),InstinctConfig.MOVEMENT_SPEED.get())
                &&(InstinctConfig.MAXIMUM_MANA.get()>=0||InstinctConfig.MANA_REGENERATION.get()>=0)) {
            var p=MobMana.profile(actor);
            MobMana.configure(actor,new MobMana.Profile(InstinctConfig.MAXIMUM_MANA.get()<0?p.maximum():InstinctConfig.MAXIMUM_MANA.get(),
                    InstinctConfig.MANA_REGENERATION.get()<0?p.regeneration():InstinctConfig.MANA_REGENERATION.get()));
        }
    }
    private InstinctSetup() {}
}
