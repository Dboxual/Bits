package com.bits.api;

import java.util.UUID;

/**
 * Public API for the Bits economy. Retrieve via Bukkit's ServicesManager:
 *
 *   RegisteredServiceProvider<BitsEconomy> rsp =
 *       Bukkit.getServicesManager().getRegistration(BitsEconomy.class);
 *   if (rsp != null) BitsEconomy economy = rsp.getProvider();
 */
public interface BitsEconomy {

    /** Returns the player's balance. Returns the configured starting balance for unknown players. */
    long getBalance(UUID player);

    /** Returns true if the player has at least {@code amount} bits. */
    boolean has(UUID player, long amount);

    /**
     * Removes {@code amount} bits from the player's balance.
     * Returns false without modifying anything if the player has insufficient funds.
     */
    boolean withdraw(UUID player, long amount);

    /**
     * Adds {@code amount} bits to the player's balance.
     * Clamps at Long.MAX_VALUE instead of wrapping.
     */
    void deposit(UUID player, long amount);

    /** Sets the player's balance to exactly {@code amount}. Clamps to zero on negative input. */
    void setBalance(UUID player, long amount);
}
