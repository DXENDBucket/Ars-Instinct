# Ars Instinct 0.1.1 — native mob casting handoff

Date: 2026-10-31. Local development only: no shared-instance deployment, remote push or CurseForge upload.

## Baselines

- Core 0.1.45: `ab3dc55710e7cd05140f188d0540240e88702366`.
- Iron 0.17.36: `47208cf0830abd82602cbdf663b7b4c53af4b48a`.
- Goety 0.1.7: `1592e5a16e452cf27f1f2dbee8e8a13456a3eabc`.

## Delivered boundaries

Core owns lazy native non-player mana and a pool facade, native attribute/event calculations, optional capacity/budget/cooldown facts and glyph usefulness checks. No Core-installed AI, second mana store, fake-player resource account or world scan.

Instinct consumes those APIs and offers explicit code installation plus a default-off entity-ID/tag join option. It does not create loadouts or aggression. Ars 4 programs are compared by slot/recipe, because each tool read constructs a new Spell; physical item snapshots still protect metadata changes.

Iron and Goety retain native cost/timing rules. Provider-owned persistent non-player deadlines apply their own and Conflux timing reductions once; player-native stores and creative behavior remain native. Goety mobs with no soul account are rejected in soul mode; explicitly selecting Ars mode uses their real native pool. Player-only fallback glyphs are marked accordingly. Hints cover checked categories, not every possible spell composition.

Beneath the Dying Sun 0.35.3 is a consumer. It retains species values, fixed-book permissions/preset levels, trajectories and special flight/aggro. Its old capability/regen loop, manual extra Iron cooldown writes and duplicated pool operations were replaced by thin adapters.

## Validation

- Minimal NeoForge 1.21.1 environment: **9/9 required GameTests passed**, without Iron/Goety/the pack. Includes real native mob attack/heal/regen and provider cooldown; both native upfront and per-trigger mode snapshots; fresh/saved/clamped pools, fractional spending/reservation-safe transfers, unknown/shared budgets, usefulness and opt-in installation. Log: `build/instinct-phase2-gametest.log`.
- Minimal Forge 1.20.1 environment: **5/5 required GameTests passed** for native lifecycle, quotes, opt-in installation and actual mob combat in both native billing modes. Log: `build/instinct-phase2-forge-test.log`. The later fractional/reservation transfer assertions were run on NeoForge; their shared final code was also compiled/packaged for Forge.
- Pack + Ars + Iron + Goety + Ars 'n Spells + NEG server: **7/7 required tests passed**. Four existing jellyfish/fixed-tool/ally/cooldown checks, two native mana checks and one new Goety mob account/buff/deadline check. Log: sibling `Beneath-The-Dying-Sun/build/instinct-phase2-regression.log`.
- After making the pack's pool operations a pure backend facade, its two mana/creative-transfer cases were rerun: **2/2 passed**. Log: sibling `Beneath-The-Dying-Sun/build/instinct-phase2-mana-facade.log`.
- Core, Instinct, Iron and Goety runtime/source builds completed for both loaders. Forge outputs were reobfuscated. Core `verifyDistributions` passed. Eight runtime jars checked for license notices, test/log exclusion, required Forge pack metadata and declared refmaps; Instinct contains no bundled Core/Iron/Goety implementation.
- Test arenas explicitly provide space and loaded chunks. Do not use a one-block empty structure and assume outside coordinates are clear in Forge's generated test world.

## Remaining limits

No client session or visual verification; no all-spell, all-equipment or all-addon AI matrix. First-activation estimates are not a guarantee for every future scatter/channel tick or arbitrary native cost-event changes. Foreign capability selection follows the loader's provider ordering and foreign pools keep their own lifecycle; no claim of universal currency unification. Position-only/complex unknown forms remain unplanned, and native permissions always apply.

## Artifacts / SHA-256

- `Ars-Conflux/build/libs/ars-conflux-0.1.45-mc1.21.1-neoforge.jar`
  `0037be2e96711b797db542d4e68fb0e92d49c75b3853dfb26c4f3ea8950c3bc9`
- `Ars-Conflux/forge/build/libs/ars-conflux-0.1.45-mc1.20.1-forge.jar`
  `4bcdfdede6ad339cbd2bae517e4ffc67fd9904ebf2fcbd06edbaf74cd1b00116`
- `Ars-Instinct/build/libs/ars-instinct-0.1.1-mc1.21.1-neoforge.jar`
  `87e38e73822c81e0f559745cbd21bf9e0015f1276c502647e7cb488ec0c2dcb2`
- `Ars-Instinct/forge/build/libs/ars-instinct-0.1.1-mc1.20.1-forge.jar`
  `d61ac99420ce0d4db3c63ba7631bb7d1472338714a415cb0ee45fb7bb9332064`
- `Irons-Nouveau/build/libs/irons-nouveau-0.17.36-mc1.21.1-neoforge.jar`
  `eb0d73ef14ffb2090ac2e5e2318394c2dfb8a7878a68c6ce2f42fcb5766b1841`
- `Irons-Nouveau/forge/build/libs/irons-nouveau-0.17.36-mc1.20.1-forge.jar`
  `e80a40551cf5ea8fc20ba8b36bda019f8d413b137bfee22fceefd8ffc8a0d2b4`
- `Goety-Nouveau/build/libs/goety-nouveau-0.1.7-mc1.21.1-neoforge.jar`
  `5568372db7dc843e140abd879872cbf159d5d48ec030e5e39b1d01cc35eee53d`
- `Goety-Nouveau/forge/build/libs/goety-nouveau-0.1.7-mc1.20.1-forge.jar`
  `c25c9cad8ba6baf0af3d276a5a7e550bf85854b026cd60bee786570223820d20`
- `Beneath-The-Dying-Sun/build/libs/beneath-the-dying-sun-0.35.3.jar`
  `171e94959d293c5aa617b7f39f9cf0c9620888e3e70ac62709a0d1f58fd9b3ff`
