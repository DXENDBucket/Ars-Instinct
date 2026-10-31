# Ars Instinct

- Read README.md and ../Ars-Conflux/docs/development-workflow.md. Core supplies glyph hints, execution, resources and timing; AI decisions belong here.
- Shared policy lives in common/. Keep Ars 4/5 and loader differences in platform trees. Core, Iron and Goety implementation classes must not be used to identify specific spells here.
- Ars Nouveau and Ars Conflux are required; Iron/Goety/Beneath the Dying Sun are not dependencies. Backends may integrate their own tools and resource pools without installing a new AI per species.
- No unconditional global Goal installation. A default-off server config may explicitly opt selected entity IDs/tags in on join; no world scan. Consumers opt in and retain ownership of native navigation, targets and species-specific behaviors.
- Labels are hints, never casting permission. Preserve real actors, native payment, native cooldowns and future-trigger billing. Unknown quotations must not become free casts.
- Avoid entity-retaining global maps. A controller belongs to one caller/actor and revalidates its tool, program, recipient and delayed readiness.
- Batch targeted validation. No client launch, shared-instance deployment, remote push or publication is implied by a build.

- Native touch must use the native entity/block interaction resolver after a clipped pickable-entity trace; generic MethodTouch.onCast returns FAILURE. Content subclasses with their own cast coordination should call protected resolve, preserving native payment and actual hit geometry. Unpickable attached fireworks are not touch recipients.

- ProjectileAwareness is opt-in caller-owned perception, not target permission or evasion. Keep spatial queries bounded/cached, report saturation and unknown ownership honestly, and release cached references on lifecycle changes. Source-specific owner lookups and evasive tactics belong to consumers. Do not globally install sensors or manipulate projectile velocity.
