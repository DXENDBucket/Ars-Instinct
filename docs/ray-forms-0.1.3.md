# Ray planning — 0.1.3

Baseline Core: 7582e45 (runtime 0.1.47). Both loaders share the API; no Core code is changed.

Checked native releases: NEG 4.6.2 / Ars 5 on NeoForge 1.21.1; NEG 3.2.1.1 / Ars 4 on Forge 1.20.1. Both expose BASE_RANGE and BONUS_RANGE_PER_AUGMENT, and calculate range from the cast's AOE multiplier. Configuration field access is cached by class, value reads remain live; missing fields/configuration fail closed. No optional class appears in an Instinct descriptor.

RayForms supplies planning range/first-hit checks, not an alternate spell implementation. Blocks, sensitivity, fluids and entity occlusion are considered. Neo rays can hit any alive entity; older Forge rays select living entities with the native overload's collision margin. Forge planning uses stricter block-surface clipping than the old native block-center approximation. Final native resolve, costs and timings are unchanged.

A content backend may provide a relative selectionWeight; positive finite preferences only affect the weighted selection among eligible programs. They cannot bypass payment, cooldowns or the shared-key turn rule. Existing backends default to one.

Validation: compile/package on both loaders and static distribution checks only. No game/client/server/GameTest launch. Actual moving-target rays and visuals still require manual play verification.
