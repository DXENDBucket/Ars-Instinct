# Ars Instinct

Opt-in, reusable spellcasting AI for Ars Nouveau programs. Mod ID: `ars_instinct`. MIT, EndXiom.

## Scope

- Minecraft 1.21.1 NeoForge / Java 21 and 1.20.1 Forge / Java 17; shared version in `VERSION`.
- Required: Ars Nouveau and Ars Conflux **0.1.44+**. No Iron, Goety, or modpack dependency.
- Installation alone does **not** add goals to mobs, grant spells, choose enemies, or create mana pools.
- Callers supply a real Ars-capable tool, existing resource lifecycle, native target/navigation, and explicit goal installation.

The first implementation reads a program's starting form and first significant ability. A projectile replacement in the initial form's augments takes priority over following hit effects. Unknown/unlabelled abilities are skipped, rather than guessed to be hostile.

## Entry points

```java
// Install explicitly on a mob chosen by your own content/pack rules.
mob.goalSelector.addGoal(1, new SpellCombatGoal(mob, 1.0));

// Flying actors/custom navigation can use the controller without the ground goal.
var controller = new SpellCombatController(backend);
var plan = controller.select(actor, tool, enemy);
if (plan != null) {
    SpellCombatGoal.aim(mob, backend.aimOrigin(actor, tool),
        (plan.recipient() == actor ? enemy : plan.recipient()).getEyePosition(), plan.ability());
    controller.cast(actor, tool, InteractionHand.MAIN_HAND);
}
```

Public types are under `dev.arsinstinct.api`. `CombatBackend` adapts an equipment/resource system once, not once per species. Implement `programs`, `describe`, dry `budget`, native `available` and native `cast`; optionally expose shared cooldown keys/durations, selection, aim origin and recipient usefulness. A `Budget.possible=false` means that program cannot be funded at full capacity or cannot be estimated. A merely unaffordable candidate can be reserved while the actor regenerates.

The default `NativeSpellBackend` reads native Ars slots and uses the actual LivingEntity, hand, native resolver, Conflux quotes and active-cast entrypoint. Quotes do not charge resources; the actual provider/resolver performs payment. Estimates cover one initial activation of each paid glyph, not unlimited future scatter/hits/channel ticks. Unknown triggered prices are skipped conservatively. Capacity detection currently covers the native Ars pool; other providers can supply stronger capacity/cooldown estimates through a backend.

`SpellCombatController` is caller-owned; clear/release it when the actor unloads, changes role, or is discarded. No global scan, fake-player casting or per-tick world registry. Default decisions every five ticks and navigation every ten ticks; target selection is local to each actor's configured range. Stop/leave lifecycle remains the installing mod's responsibility for controllers outside the supplied goal.

## Ability declarations

Conflux supplies optional, namespaced per-glyph hints independently of schools and permissions:

```java
GlyphAbilities.declare(glyphId, Set.of(AbilityTags.HEALING, AbilityTags.SELF, AbilityTags.ALLY));
```

A data pack can replace the complete hint set, including clearing it with `[]`:

`data/<glyph namespace>/conflux/glyph_abilities/<glyph path>.json`

```json
{ "tags": ["ars_conflux:attack", "ars_conflux:receiver_enemy"] }
```

Server reloads atomically replace the snapshot. Removing an override restores code defaults. Absent optional mods are allowed in data. Nothing changes native target permissions. `area` distinguishes area healing from a self-only heal. Receiver tags describe who executes/receives an ability: breath or a dash can damage enemies while being granted to an ally.

## Current limits

This is a developer-facing first release, not a configurable automatic mob overhaul. No complete spell graph reasoning, tactical retreat, summon management, species loadouts or GUI. Position-only/unknown shapes are not automatically planned. Mana initialization/regeneration and pack-specific non-player cooldown/levels have **not** been extracted in this batch; Beneath the Dying Sun retains those native-Ars-backed responsibilities. Its ordinary ground casting and custom flying controllers consume Instinct instead of duplicating the selector.

## Build and validation

```powershell
# NeoForge, Java 21
./gradlew.bat :jar :sourcesJar --offline
./gradlew.bat :runGameTestServer --offline
# Forge, Java 17, from forge/
./gradlew.bat :jar :sourcesJar --offline
```

Sibling `Ars-Conflux` checkout is the default build dependency (`-PconfluxCheckout=...` overrides it); local native mod jars follow Core's existing `libs` layout and are never bundled. Forge `:jar` finalizes with `:reobfJar`; deploy that reobfuscated jar.

Integration test evidence and current migration boundaries are recorded in `docs/implementation-0.1.0.md`. Client visual behavior is not implied by server tests.
