# Bits — Developer Guide

## Project purpose

Bits is a Paper 1.21 player currency plugin that serves as the shared economy foundation for the server. It provides an integer-based balance system with player-to-player payments, admin management, a public API for inter-plugin use, and a Vault economy bridge.

---

## Architecture

```
BitsPlugin                       — entry point; wires storage, manager, command handler,
                                   Bukkit service registration, and optional Vault bridge
  BalanceStorage                 — YAML persistence (balances.yml); keyed by UUID
  BalanceManager (→ BitsEconomy) — in-memory balance operations; implements the public API
  BitsCommand                    — /bits handler + tab completion
  api/BitsEconomy                — public interface for inter-plugin use
  vault/VaultEconomyBridge       — Vault Economy implementation backed by BalanceManager
```

---

## Inter-plugin API

Other plugins should use the Bukkit service lookup — never cast to `BitsPlugin` directly:

```java
RegisteredServiceProvider<BitsEconomy> rsp =
    Bukkit.getServicesManager().getRegistration(BitsEconomy.class);
if (rsp != null) {
    BitsEconomy economy = rsp.getProvider();
    economy.deposit(playerUUID, 100L);
}
```

Plugins that use Vault can also interact via the `Economy` service — Bits registers itself as a Vault provider when Vault is present.

---

## Storage format

`plugins/Bits/balances.yml`:
```yaml
balances:
  <uuid>: <long>
  <uuid>: <long>
```

Loaded into a `HashMap<UUID, Long>` at startup. Written back on every mutating transaction and on plugin disable.

The `starting-balance` in `config.yml` is the default for players with no entry — it is **not** pre-populated on first join.

---

## Balance rules

- All balances are `long` (whole numbers only — no decimals).
- Minimum balance is `0` — `setBalance()` clamps below zero to zero.
- `withdraw()` and `pay()` are atomic: return `false` without modifying anything if funds are insufficient.
- `deposit()` guards against `Long` overflow by clamping at `Long.MAX_VALUE`.
- `/bits pay` requires both players to be online.
- Admin commands (`give`, `take`, `set`, `balance`) support offline players via the server's offline player cache.

---

## Command map

| Subcommand | Permission | Online-only? | Notes |
|---|---|---|---|
| (none) | — | Player only | Shows sender's balance |
| `balance [player]` | `bits.admin` for others | No | Works offline |
| `pay <player> <amount>` | — | Yes | Both players must be online |
| `give <player> <amount>` | `bits.admin` | No | Works offline |
| `take <player> <amount>` | `bits.admin` | No | Works offline |
| `set <player> <amount>` | `bits.admin` | No | Works offline |
| `help` | — | No | Admin subcommands shown only to `bits.admin` |

---

## Vault bridge

`VaultEconomyBridge` implements `net.milkbowl.vault.economy.Economy` and is registered when Vault is on the server. Key behaviour:
- `fractionalDigits()` returns `0` — no decimal support
- Fractional Vault amounts are truncated (`(long) amount`) before passing to `BalanceManager`
- Bank methods all return `NOT_IMPLEMENTED`
- String-name lookups resolve online players first, then offline player cache

---

## Build

```bash
./gradlew clean jar
# Output: build/Bits-1.0.1.jar
```

Gradle downloads Paper API and Vault API (via JitPack) automatically. No local libs needed.

---

## Workflow rules

Before ending any work session:

1. Increment the version in `plugin.yml` and `build.gradle.kts`.
2. Run `./gradlew clean jar` and confirm the jar is produced.
3. Update `CHANGELOG.md` with a dated entry.
4. Commit all modified source files and the new jar together.

---

## Future phases (not yet implemented)

- Mob kill rewards (configurable per mob type)
- PlaceholderAPI expansion (`%bits_balance%`)
- Bridge minigame — bits awarded per win
- ClaimChest integration
- Starting balance written on first join event
