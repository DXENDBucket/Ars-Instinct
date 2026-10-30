# Shared-cooldown program opportunities

The previous controller preferred longer overall cooldowns and reduced the random weight of consecutive uses. That still allowed equal-key programs to repeatedly win selection, and a longer-cooldown program could indefinitely take precedence over a shorter sibling.

Version 0.1.2 records successfully submitted casts per semantic `Program` (slot plus recipe). Among useful candidates, a more recently submitted program yields to a less recently submitted sibling when both share a positive cooldown key and the sibling is ready, affordable and available. Unknown or impossible budgets never become free. Unrelated cooldown groups retain their existing weighted selection.

The longer-cooldown waiting rule remains for equally used programs and for preventing rapid casts from continually postponing an older long-cooldown candidate. A shorter program with an older use record is allowed its turn. A pending resource-saving selection also yields when a usable shared-key sibling is owed a turn.

Ordinary plan clearing does not erase usage history; a temporarily unnecessary buff therefore retains its record until it becomes useful again. History is pruned against the entire current loadout, not just the currently useful candidates, and lives inside the caller-owned controller. Failed submissions do not advance history. An accepted chant that is later canceled still counts as an accepted submission, not as confirmed native effect execution.

For two simultaneously useful, ready, affordable and available programs sharing a key, the first choice retains the prior policy, then the more recently used one yields. If one has no valid recipient or is no longer useful, it does not force the other to wait for a turn. This is an opportunity rule, not a forced global rotation, and never changes a provider's actual cooldown or target restrictions.

Both loaders are compiled and packaged; archive metadata and Forge production artifacts are checked. The user requested build-only validation: no GameTest, client or server launch, and no claim of an in-game scheduling regression pass.
