# Ars Instinct

Reusable spellcasting decisions for Ars-powered creatures.

**Ars Instinct** is a **library mod for addon developers and modpack authors**, built on **Ars Nouveau** and **Ars Conflux**. It provides shared building blocks for creatures that use real Ars spell programs, while content mods define their creatures, loadouts and fighting styles.

**Installing the library alone does not make ordinary mobs cast spells.** It adds no creatures, equipment, glyphs or global enemy-selection behavior.

## What the Library Provides

- **Reusable combat controllers:** use the supplied ground-navigation Goal, or drive the controller from a creature's custom movement and AI.
- **Ability-driven spell selection:** read the starting cast form and first significant tagged ability to distinguish attacks, healing, buffs and other supported uses.
- **Target and recipient selection:** choose enemies, self or allies using visibility, reach, alliance and usefulness checks.
- **Resource-aware planning:** estimate initial spell costs through Conflux, wait for affordable opportunities and leave actual payment to the native spell providers.
- **Shared-cooldown fairness:** avoid repeatedly letting one spell monopolize opportunities needed by other programs sharing its cooldown.
- **Optional projectile perception:** inspect nearby native projectiles, their known ownership and predicted incoming collisions through bounded, cached queries. Content code decides how to react.

The native Ars backend reads multiple spell slots from Ars-compatible casting tools and uses the actual creature as the caster. Built-in planning covers **Self, Touch, registered projectile forms and supported ray forms**, including the optional Not Enough Glyphs ray integration.

Casting goes through the existing Ars/Conflux paths, retaining native resource payment, preparation, cooldown and effect execution. Ability labels guide decisions; they do not grant casting permission or replace an effect's own rules.

## For Content Developers

Public APIs are under `dev.arsinstinct.api`:

| API | Purpose |
| --- | --- |
| `SpellCombatController` | Select and submit suitable programs for a caller-owned actor. |
| `SpellCombatGoal` | Ready-to-install spellcasting and melee behavior for ordinary ground mobs. |
| `CombatBackend` | Adapt a tool, program list, resource model or custom selection policy. |
| `InstinctCasting` | Explicitly install the supplied Goal on selected mobs. |
| `ProjectileAwareness` | Read-only projectile threat perception for custom tactics. |
| `RayForms` | Supply reach information for additional ray implementations. |

Provide the creature's casting tool, spell programs, resource setup and combat targets through your own content. Flying movement, retreats, evasive actions, summon tactics and species-specific behavior can use the shared APIs while remaining under your mod's control.

Glyph ability declarations and data overrides are provided by **Ars Conflux** and consumed by Instinct. You do not need to add an Iron- or Goety-specific AI to every creature.

## For Modpack Authors

Automatic Goal installation is **disabled by default**. An optional server configuration can opt selected entity IDs or the `ars_instinct:spellcasters` entity-type tag into the supplied AI when those entities join the world.

This option installs the behavior; it does not equip creatures, generate spell loadouts or create attack targets. Optional capacity and regeneration overrides are available for the selected creatures. Default values retain the native Ars/content resource policy.

Configuration: `ars_instinct-server.toml` in the world's `serverconfig` directory; use the corresponding `defaultconfigs` preset for new worlds.

## Requirements

| Minecraft / loader | Required mods |
| --- | --- |
| **1.21.1 / NeoForge** | Ars Nouveau **5.13–5.x** and Ars Conflux **0.1.45+ within 0.1.x** |
| **1.20.1 / Forge** | Ars Nouveau **4.12–4.x** and Ars Conflux **0.1.45+ within 0.1.x** |

Install the matching loader builds and Ars Nouveau's own required dependencies. **Iron's Spells 'n Spellbooks, Goety and Not Enough Glyphs are optional.**

By **EndXiom**. Licensed under **MIT**.
