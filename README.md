# Bits

A standalone player currency plugin for Paper 1.21.1.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/bits` | — | Show your balance |
| `/bits balance [player]` | `bits.admin` for others | Check a balance |
| `/bits pay <player> <amount>` | — | Pay another online player |
| `/bits give <player> <amount>` | `bits.admin` | Add bits to a player |
| `/bits take <player> <amount>` | `bits.admin` | Remove bits from a player |
| `/bits set <player> <amount>` | `bits.admin` | Set a player's balance |
| `/bits help` | — | Show help |

## Permissions

| Permission | Default | Purpose |
|---|---|---|
| `bits.admin` | op | Admin commands: give, take, set, check others' balances |

## Storage

Balances are stored in `plugins/Bits/balances.yml` keyed by UUID.

## Build

```bash
JAVA_HOME=~/tools/jdk-21.0.11+10 ~/tools/gradle-8.13/bin/gradle jar
# Output: build/Bits-1.0.0.jar
```
