package dev.arsinstinct.platform;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;
public final class InstinctConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_TYPES;
    public static final ForgeConfigSpec.IntValue GOAL_PRIORITY;
    public static final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED,MAXIMUM_MANA,MANA_REGENERATION;
    static {
        var b=new ForgeConfigSpec.Builder();
        ENABLED=b.comment("Explicit opt-in: add the spellcasting goal to listed entity types or the ars_instinct:spellcasters entity-type tag.",
                "False by default. Does not equip mobs or create attack targets. Changes apply on the next entity join.").define("enabled",false);
        ENTITY_TYPES=b.comment("Entity IDs, e.g. minecraft:zombie. Empty by default; no wildcard.").defineListAllowEmpty("entity_types",List.of(),v->v instanceof String s&&net.minecraft.resources.ResourceLocation.tryParse(s)!=null);
        GOAL_PRIORITY=b.defineInRange("goal_priority",1,0,100);
        MOVEMENT_SPEED=b.defineInRange("movement_speed",1.0,0,16);
        MAXIMUM_MANA=b.comment("Optional per-entity base capacity override for installed mobs. -1 keeps native Ars/content policy.").defineInRange("maximum_mana",-1.0,-1,Integer.MAX_VALUE);
        MANA_REGENERATION=b.comment("Optional mana per second. -1 keeps native Ars/content policy.").defineInRange("mana_regeneration",-1.0,-1,1000000);
        SPEC=b.build();
    }
    private InstinctConfig() {}
}
