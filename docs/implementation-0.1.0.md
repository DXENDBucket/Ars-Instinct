# First implementation handoff — 2026-10-31

## Scope and baselines

First usable API/controller release, not the entire entity-casting plan. Core tags and Instinct are separate projects. No remote publication, CF upload or shared-client deployment in this batch.

- Core 0.1.44: `c961bccdc3a60788d6977bb4810e5d94428b2ca9` (implementation started from `93cf3e9340662c335de70249614e5a9d56fe7b3b`).
- Iron 0.17.35 hints/build fix: `ff44905b6eee7daa99acc39d57ab04a8b3ed6e4d`.

## Implemented

- Namespaced per-glyph hints, immutable code defaults, atomic server reload, empty masks and default restoration.
- Instinct native-tool backend, first-ability intent, local recipient selection, optional ground goal, controller usable by flying/custom AI, ballistic aiming, bounded saving, first-slot/repeat weights, and shared-cooldown avoidance.
- Real-caster/hand execution with live tool/recipient/funding checks. Provider-owned fees are only estimated, never charged by the selector.
- Pack adapter replaces duplicated multi-slot/targeting logic, while retaining native mana lifecycle, per-species values, preset levels, tool preparation, Iron-specific cooldowns and special movement.
- No automatic mob takeover, global entity scan, second mana pool or fake-player NPC implementation. The fake player in one test is only a native player mana fixture.

## Validation evidence

- Core `GlyphAbilitiesTest`: **3 unit tests passed** (override/restoration/immutability, optional/custom IDs, invalid data/conflicts).
- Combined isolated NeoForge server: **8/8 required GameTests passed**. Log: sibling `Beneath-The-Dying-Sun/build/instinct-gametest-output.log`; test world `run/instinct-integration`.
- Four existing pack cases: native tools/mana/levels/family behavior; actual ray charge execution; ray range and ally choice; shared cooldown and mana reservation.
- Four new Instinct cases: long-combination reservation and stale-hand rejection; first-ability semantics and unknown skipping; self-only heal eligibility; real native mana spend and empty-pool rejection.
- NeoForge Instinct, Core, Iron and pack JAR/source builds completed. Forge Instinct built and reobfuscated; Core Forge distribution verification passed.
- Iron Forge's cached compile initially lacked generated Mixin mapping side outputs. Declared all three outputs, then successfully rebuilt. A subsequent clean build restored `compileJava FROM-CACHE` and completed reobfuscation; the final binary contains its declared refmap and ability data.
- Binary package checks confirm MIT notices, no bundled native mod/Core implementation in Instinct, no GameTests/logs, and present declared refmaps. Source archives are source artifacts, not runtime mod replacements.

## Not verified / remaining

No Minecraft client session or visual verification, no Forge runtime GameTest in this batch. NPC mana initialization/regen and provider-specific non-player cooldown lifecycle are still supplied by the caller/pack. Only initial Ars/Iron ability hints are provided; this does not classify every existing addon glyph. Default native estimates cover first activation, not unlimited repeated hits/channels; non-Ars capacity handling is an extension point.

## Artifacts (SHA-256)

- `Ars-Instinct/build/libs/ars-instinct-0.1.0-mc1.21.1-neoforge.jar`
  `71774805af187b72db508365f2d87a339729d002da3e8567b7d130b7652dc0ad`
- `Ars-Instinct/forge/build/libs/ars-instinct-0.1.0-mc1.20.1-forge.jar`
  `423a02aa16d0730e15b0297bfb155fd829c5b0065b56e036099483cb94873aa6`
- `Ars-Conflux/build/libs/ars-conflux-0.1.44-mc1.21.1-neoforge.jar`
  `a47c4237675307b507382f85766e9c9f6d4df93c147f33eff45843726e229a7b`
- `Ars-Conflux/forge/build/libs/ars-conflux-0.1.44-mc1.20.1-forge.jar`
  `8f258b2949fcad5015aec3efc55f07c921cf41515ceb554e3dcb6fb2fc74e107`
- `Irons-Nouveau/build/libs/irons-nouveau-0.17.35-mc1.21.1-neoforge.jar`
  `23f720699d4cc9e75aa88e9a87b4e43e2369b54f538c1c887b647cdd6537bff2`
- `Irons-Nouveau/forge/build/libs/irons-nouveau-0.17.35-mc1.20.1-forge.jar`
  `1cba7caded5a46d91b7bff755380e122d296063c29444e7d87f9b75e801cf4b4`
