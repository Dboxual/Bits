# Changelog

## v1.0.0 — 2026-05-30
### Initial release — standalone currency plugin

**Commands:**
- `/bits` — display your own balance
- `/bits balance [player]` — check your balance; `bits.admin` required to check others
- `/bits pay <player> <amount>` — transfer bits to an online player; blocked if insufficient funds
- `/bits give <player> <amount>` — admin: add bits to a player's balance
- `/bits take <player> <amount>` — admin: remove bits (blocked if player has insufficient funds)
- `/bits set <player> <amount>` — admin: set a player's balance to an exact value
- `/bits help` — list available commands (admin commands shown only to `bits.admin`)

**Permissions:**
- `bits.admin` (default: op) — covers give, take, set, and checking others' balances

**Storage:**
- Balances stored in `plugins/Bits/balances.yml` keyed by UUID
- Saves on every transaction and on plugin disable
- `settings.starting-balance` in `config.yml` sets the default for players with no recorded balance (default: 0)

**Not yet included (future phases):**
- Mob kill rewards
- Vault integration
- Bridge minigame integration
- ClaimChest integration
- Shop / GUI system
