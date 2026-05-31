# Changelog

## v1.0.1 — 2026-05-31
### Foundation release — shared economy API, Vault bridge, offline player support

**New: BitsEconomy public API (`com.bits.api.BitsEconomy`)**
- Interface with `getBalance`, `has`, `withdraw`, `deposit`, `setBalance`
- Registered as a Bukkit service on enable — other plugins look it up via
  `Bukkit.getServicesManager().getRegistration(BitsEconomy.class)`, no direct casting required

**New: Vault economy bridge (`com.bits.vault.VaultEconomyBridge`)**
- Registered as a Vault `Economy` provider when Vault is present (`softdepend`)
- Currency name: Bits (singular: Bit)
- Whole-number balances only — fractional Vault amounts are floor-truncated
- Supports all `OfflinePlayer`-based Vault methods
- String-name Vault calls resolve via online cache then offline player cache
- Bank methods return `NOT_IMPLEMENTED` (Bits does not support banks)

**Improved: Offline player support in admin commands**
- `/bits balance`, `/bits give`, `/bits take`, `/bits set` now work on offline players
- Resolves by checking online players first, then iterating the server's offline player cache
- Confirmation messages sent to the player only when they are online

**Fixed: `pay()` double-read**
- Payer balance now captured in a single variable before the check and deduction

**Fixed: `deposit()`/`give()` overflow guard**
- Addition result is checked for overflow; clamped to `Long.MAX_VALUE` instead of wrapping negative

**Fixed: `withdraw()` rejects zero and negative amounts**
- `withdraw(uuid, 0)` now correctly returns false

**Updated: `plugin.yml`**
- `softdepend: [Vault]` added so Vault loads before Bits when present

---

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
