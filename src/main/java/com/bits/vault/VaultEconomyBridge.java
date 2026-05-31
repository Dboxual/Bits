package com.bits.vault;

import com.bits.BitsPlugin;
import com.bits.data.BalanceManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.util.List;
import java.util.UUID;

public class VaultEconomyBridge implements Economy {

    private final BitsPlugin plugin;
    private final BalanceManager balances;

    public VaultEconomyBridge(BitsPlugin plugin, BalanceManager balances) {
        this.plugin = plugin;
        this.balances = balances;
    }

    public void register() {
        plugin.getServer().getServicesManager().register(Economy.class, this, plugin, ServicePriority.Normal);
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    @Override public boolean isEnabled()            { return plugin.isEnabled(); }
    @Override public String getName()               { return "Bits"; }
    @Override public boolean hasBankSupport()       { return false; }
    @Override public int fractionalDigits()         { return 0; }
    @Override public String currencyNamePlural()    { return "Bits"; }
    @Override public String currencyNameSingular()  { return "Bit"; }

    @Override
    public String format(double amount) {
        return (long) amount + (amount == 1.0 ? " Bit" : " Bits");
    }

    // ── Account existence (all players are implicitly supported) ──────────────

    @Override public boolean hasAccount(String name)                       { return true; }
    @Override public boolean hasAccount(OfflinePlayer player)              { return true; }
    @Override public boolean hasAccount(String name, String world)         { return true; }
    @Override public boolean hasAccount(OfflinePlayer player, String world){ return true; }

    @Override public boolean createPlayerAccount(String name)                       { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer player)              { return true; }
    @Override public boolean createPlayerAccount(String name, String world)         { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer player, String world){ return true; }

    // ── Balance ───────────────────────────────────────────────────────────────

    @Override
    public double getBalance(OfflinePlayer player) {
        return balances.getBalance(player.getUniqueId());
    }

    @Override public double getBalance(String name)               { return getBalance(resolveOrFail(name)); }
    @Override public double getBalance(OfflinePlayer p, String w) { return getBalance(p); }
    @Override public double getBalance(String name, String world) { return getBalance(name); }

    // ── Has ───────────────────────────────────────────────────────────────────

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        if (amount < 0) return false;
        return balances.has(player.getUniqueId(), (long) amount);
    }

    @Override public boolean has(String name, double amount)               { return has(resolveOrFail(name), amount); }
    @Override public boolean has(OfflinePlayer p, String world, double amt){ return has(p, amt); }
    @Override public boolean has(String name, String world, double amount)  { return has(name, amount); }

    // ── Withdraw ──────────────────────────────────────────────────────────────

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount <= 0)
            return fail(player, "Amount must be positive.");
        long whole = (long) amount;
        boolean ok = balances.withdraw(player.getUniqueId(), whole);
        double newBal = getBalance(player);
        return ok
            ? new EconomyResponse(whole, newBal, ResponseType.SUCCESS, null)
            : new EconomyResponse(0, newBal, ResponseType.FAILURE, "Insufficient funds.");
    }

    @Override public EconomyResponse withdrawPlayer(String name, double amount)               { return withdrawPlayer(resolveOrFail(name), amount); }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer p, String w, double amount) { return withdrawPlayer(p, amount); }
    @Override public EconomyResponse withdrawPlayer(String name, String w, double amount)      { return withdrawPlayer(name, amount); }

    // ── Deposit ───────────────────────────────────────────────────────────────

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount <= 0)
            return fail(player, "Amount must be positive.");
        long whole = (long) amount;
        balances.deposit(player.getUniqueId(), whole);
        return new EconomyResponse(whole, getBalance(player), ResponseType.SUCCESS, null);
    }

    @Override public EconomyResponse depositPlayer(String name, double amount)               { return depositPlayer(resolveOrFail(name), amount); }
    @Override public EconomyResponse depositPlayer(OfflinePlayer p, String w, double amount) { return depositPlayer(p, amount); }
    @Override public EconomyResponse depositPlayer(String name, String w, double amount)      { return depositPlayer(name, amount); }

    // ── Banks (unsupported) ───────────────────────────────────────────────────

    @Override public EconomyResponse createBank(String name, String player)       { return noBank(); }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player){ return noBank(); }
    @Override public EconomyResponse deleteBank(String name)                      { return noBank(); }
    @Override public EconomyResponse bankBalance(String name)                     { return noBank(); }
    @Override public EconomyResponse bankHas(String name, double amount)          { return noBank(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount)     { return noBank(); }
    @Override public EconomyResponse bankDeposit(String name, double amount)      { return noBank(); }
    @Override public EconomyResponse isBankOwner(String name, String player)      { return noBank(); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer p)    { return noBank(); }
    @Override public EconomyResponse isBankMember(String name, String player)     { return noBank(); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer p)   { return noBank(); }
    @Override public List<String> getBanks()                                       { return List.of(); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private EconomyResponse noBank() {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Bits does not support bank accounts.");
    }

    private EconomyResponse fail(OfflinePlayer player, String reason) {
        return new EconomyResponse(0, getBalance(player), ResponseType.FAILURE, reason);
    }

    /**
     * Resolves a name to an OfflinePlayer for string-based Vault calls.
     * Returns a dummy sentinel (balance 0, UUID derived from name) when not found
     * so string-based Vault callers don't get NPEs — the result will simply show 0 balance.
     */
    private OfflinePlayer resolveOrFail(String name) {
        org.bukkit.entity.Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(op.getName())) return op;
        }
        // Unknown name — return a wrapper whose UUID hashes from the name.
        // No balance entry will exist for it, so callers receive the starting balance (0).
        return Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()));
    }
}
