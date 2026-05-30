# Bits — Developer Guide

## Project purpose

Bits is a standalone player currency plugin for Paper 1.21.1. It provides a simple integer-based balance system with player-to-player payments and admin management commands. No Vault, no GUI, no external dependencies beyond Paper.

---

## Architecture

```
BitsPlugin                    — entry point; wires storage, manager, command handler
  BalanceStorage              — YAML persistence (balances.yml); keyed by UUID
  BalanceManager              — in-memory balance operations: get/set/give/take/pay
  BitsCommand                 — /bits handler + tab completion; implements CommandExecutor + TabCompleter
```

---

## Storage format

`plugins/Bits/balances.yml`:
```yaml
balances:
  <uuid>: <long>
  <uuid>: <long>
```

Loaded into a `HashMap<UUID, Long>` at startup. Written back on every mutating transaction (`give`, `take`, `set`, `pay`) and on plugin disable.

The `starting-balance` in `config.yml` is used as the default value for players with no entry — it is **not** pre-populated into the map on first join.

---

## Balance rules

- All balances are stored as `long` (whole numbers, no decimals).
- Minimum balance is `0` — `setBalance()` clamps at zero.
- `take()` and `pay()` are atomic: they return `false` without modifying anything if the source has insufficient funds.
- `/bits pay` requires both players to be online.
- Admin commands (`give`, `take`, `set`) require target to be online in v1.0.0.

---

## Command map

| Subcommand | Permission | Args |
|---|---|---|
| (none) | — | Shows sender's balance |
| `balance [player]` | `bits.admin` for others | Optional player name |
| `pay <player> <amount>` | — | Player must be online |
| `give <player> <amount>` | `bits.admin` | Positive amount |
| `take <player> <amount>` | `bits.admin` | Positive amount |
| `set <player> <amount>` | `bits.admin` | Non-negative amount (0 allowed) |
| `help` | — | Lists admin commands only if sender has `bits.admin` |

Tab completion: subcommands on arg 1, online player names on arg 2 for player-targeting subcommands.

---

## Build

```bash
JAVA_HOME=~/tools/jdk-21.0.11+10 ~/tools/gradle-8.13/bin/gradle jar
# Output: build/Bits-1.0.0.jar
```

Gradle downloads Paper API from `https://repo.papermc.io/repository/maven-public/` automatically. No libs/ directory needed.

---

## Workflow rules

Before ending any work session:

1. Increment the version in `plugin.yml` and `build.gradle.kts`.
2. Run the build command and confirm the jar is produced.
3. Update `CHANGELOG.md` with a dated entry.
4. Commit all modified source files and the new jar together.

---

## Future phases (not yet implemented)

- Mob kill rewards (configurable per mob type)
- Vault economy bridge
- Bridge minigame — bits awarded per win
- ClaimChest integration
- Shop / GUI system
