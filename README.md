# Ars Instinct

Opt-in, reusable spellcasting AI for Ars Nouveau programs. Mod ID: `ars_instinct`. MIT, EndXiom.

## Scope

- Minecraft 1.21.1 NeoForge / Java 21 and 1.20.1 Forge / Java 17; shared version in `VERSION`.
- Required: Ars Nouveau and Ars Conflux **0.1.45+**. No Iron, Goety, or modpack dependency.
- Installation alone does **not** add goals to mobs, grant spells or choose enemies. Core now owns lazy native non-player mana initialization/regeneration.
- Callers supply a real Ars-capable tool, existing resource lifecycle, native target/navigation, and explicit goal installation.

The first implementation reads a program's starting form and first significant ability. A projectile replacement in the initial form's augments takes priority over following hit effects. Unknown/unlabelled abilities are skipped, rather than guessed to be hostile.

## 0.1.5: opt-in projectile awareness

`ProjectileAwareness` is a caller-owned, read-only sensor for native `Projectile` entities. Default policy: 32-block radius, one refresh every four ticks, 20-tick linear prediction and at most 128 candidates from the local spatial entity index. It excludes self/allied ownership, accepts a caller's additional owner lookup, distinguishes non-allied and unknown sources, and estimates incoming collision using relative movement plus block obstruction. Snapshots are immutable, report candidate saturation and need explicit lifecycle clearing. It grants no immunity, changes no projectile and installs no dodge/AI on any entity. Content decides how to react. Beams, area effects and non-Projectile custom entities are outside this initial sensor.

## 0.1.4: native touch interaction dispatch

Touch uses native `SpellResolver.onCastOnEntity` / `onCastOnBlock` after a real, block-clipped interaction trace. Ars' generic `MethodTouch.onCast` always fails. Empty aim fails without payment; unpickable attached fireworks cannot intercept touch. The shared protected `NativeSpellBackend.resolve` also supports content adapters that own their active-cast policy. Override `touchRange` to keep planning and actual reach aligned (default 3). No automatic target hit, fake player, damage replacement or resource/cooldown bypass. Other cast forms retain the ordinary native entry point.

## 0.1.3: optional ray tactics

`NativeSpellBackend` now recognizes NEG's Ray form without a hard NEG dependency. `RayForms` reads its configured base/range-per-AOE values and actual cast stats; callers may register other ray range providers. Entity selection checks blocks and the first intersecting entity, using each loader's native entity filter. Forge planning deliberately clips at the actual block surface (the older native ray clips more loosely near a block center). The native resolver remains authoritative for visuals, payment and effects.

`CombatBackend.selectionWeight` lets content prioritize suitable programs, while preserving shared-cooldown fairness, readiness and real resource budgets. The default weight is one, so existing selection is unchanged. Neither ray recognition nor this hook installs AI on new entities.

## 0.1.2: shared-cooldown opportunities

The caller-owned controller records successfully submitted programs. Among ready, affordable, usable programs sharing a nonzero cooldown key, a more recently used program yields to an older one. The longer-cooldown preference remains for ties and waiting opportunities, but cannot permanently monopolize an older sibling. Independent cooldown groups retain weighted selection. History survives usefulness changes and ordinary plan clears, and is pruned when a program leaves the current loadout. No native cooldown is rewritten or bypassed. This batch is built for both loaders without launching game tests.

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

The default `NativeSpellBackend` reads native Ars slots and uses the actual LivingEntity, hand, native resolver, Core-managed native mana, combined resource budgets, shared provider cooldown facts and the active-cast entrypoint. Quotes do not charge resources; the actual provider/resolver performs payment. Estimates cover one initial activation of each paid glyph, not unlimited future scatter/hits/channel ticks. Unknown triggered prices are skipped conservatively. Account capacity is provider-owned. Unknown prices are never treated as free, and unknown capacities are only admitted when already affordable. Native Ars trigger billing includes the first future native effect fees in its estimate.

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

## Optional automatic installation

World server configuration: `serverconfig/ars_instinct-server.toml` (or matching `defaultconfigs` preset).

```toml
enabled = false
entity_types = []
goal_priority = 1
movement_speed = 1.0
maximum_mana = -1.0
mana_regeneration = -1.0
```

When explicitly enabled, only the listed entity types **or** members of the `ars_instinct:spellcasters` entity-type tag receive the goal, on entity join. No wildcard. The optional mana overrides use -1 for the native Ars/content policy; they do not refill existing pools. Duplicate join/install attempts do not add duplicate goals. Native target selection, aggression, equipment and alternative movement are still content-owned. Changes apply on the next join/restart, not through a world-wide scan.

Code callers can use `InstinctCasting.install(mob, priority, speed[, backend])` without enabling the configuration. The provided goal is for ordinary ground navigation; flying casters should use the controller and retain their own flight rules.

## Current limits

No complete spell-graph reasoning, tactical retreat, summon management, generated loadouts or GUI. Position-only/unknown shapes are not automatically planned. NPC-only soul accounts are not invented: Goety users must supply a real account or explicitly select the existing Ars resource mode. Ability hints and usefulness predicates guide AI, but never bypass native permissions or restrictions. Summons retain their original provider lifecycle and limits.

Beneath the Dying Sun now delegates mana lifecycle, quotes and non-player provider cooldowns to Core/addons, while retaining species values, equipment preparation, fixed-book permissions, preset levels and special flight/aggro. Integration does not require that pack.

## Build and validation

```powershell
# NeoForge, Java 21
./gradlew.bat :jar :sourcesJar --offline
./gradlew.bat :runGameTestServer --offline
# Forge, Java 17, from forge/
./gradlew.bat :jar :sourcesJar --offline
```

Sibling `Ars-Conflux` checkout is the default build dependency (`-PconfluxCheckout=...` overrides it); local native mod jars follow Core's existing `libs` layout and are never bundled. Forge `:jar` finalizes with `:reobfJar`; deploy that reobfuscated jar.

Integration test evidence and current migration boundaries are recorded in `docs/implementation-0.1.1.md`. Client visual behavior is not implied by server tests.
