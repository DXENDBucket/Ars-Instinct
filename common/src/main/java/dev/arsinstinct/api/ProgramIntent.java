package dev.arsinstinct.api;

import com.hollingsworth.arsnouveau.api.spell.*;
import dev.arsconflux.api.glyph.*;
import dev.arsconflux.api.projectile.ProjectileForm;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

/** Start form + first real ability. No flattening or whole-program semantic simulation. */
public record ProgramIntent(AbstractSpellPart primary,Set<ResourceLocation> tags) {
    public ProgramIntent {tags=Set.copyOf(tags);}
    public static ProgramIntent of(Spell spell) {
        if(spell==null||spell.isEmpty()||spell.getCastMethod()==null)return new ProgramIntent(null,Set.of());
        var parts=ArsSpellAccess.parts(spell);
        AbstractSpellPart primary=null;
        for(int i=1;i<parts.size();i++) {
            var part=parts.get(i);
            if(part instanceof ProjectileForm){primary=part;break;}
            if(!(part instanceof AbstractAugment))break;
        }
        if(primary==null)for(var part:parts) {
            if(part instanceof AbstractCastMethod) {if(part!=spell.getCastMethod())break;continue;}
            if(!(part instanceof AbstractAugment)){primary=part;break;}
        }
        return new ProgramIntent(primary,GlyphAbilities.get(primary));
    }
    public boolean has(ResourceLocation tag) {return tags.contains(tag);}
    public boolean known() {return primary!=null&&!tags.isEmpty();}
    public boolean friendly() {return has(AbilityTags.ALLY);}
}
