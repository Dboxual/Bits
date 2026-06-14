# Bops

A standalone player currency plugin for Paper 1.21.1.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/bops` | - | Show your balance |
| `/bops balance [player]` | `bops.admin` for others | Check a balance |
| `/bops pay <player> <amount>` | - | Pay another online player |
| `/bops give <player> <amount>` | `bops.admin` | Add bops to a player |
| `/bops take <player> <amount>` | `bops.admin` | Remove bops from a player |
| `/bops set <player> <amount>` | `bops.admin` | Set a player's balance |
| `/bops help` | - | Show help |

## Permissions

| Permission | Default | Purpose |
|---|---|---|
| `bops.admin` | op | Admin commands: give, take, set, check others' balances |

## Storage

Balances are stored in `plugins/Bops/balances.yml` keyed by UUID.
On first run after the rebrand, Bops can copy balances from the legacy
`plugins/Bits/balances.yml` file without deleting that old data.

## Build

```bash
JAVA_HOME=~/tools/jdk-21.0.11+10 ~/tools/gradle-8.13/bin/gradle jar
# Output: build/Bops-<version>.jar
```
