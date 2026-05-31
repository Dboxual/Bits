package com.bits.commands;

import com.bits.BitsPlugin;
import com.bits.data.BalanceManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BitsCommand implements CommandExecutor, TabCompleter {

    private final BitsPlugin plugin;
    private final BalanceManager balances;

    public BitsCommand(BitsPlugin plugin, BalanceManager balances) {
        this.plugin = plugin;
        this.balances = balances;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Usage: /bits <subcommand> [args]");
                return true;
            }
            sender.sendMessage("§6Your balance: §e" + balances.getBalance(player.getUniqueId()) + " bits");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help"             -> sendHelp(sender);
            case "balance", "bal"   -> handleBalance(sender, args);
            case "pay"              -> handlePay(sender, args);
            case "give"             -> handleGive(sender, args);
            case "take"             -> handleTake(sender, args);
            case "set"              -> handleSet(sender, args);
            default                 -> sender.sendMessage("§cUnknown subcommand. Use §e/bits help§c.");
        }
        return true;
    }

    // ── Subcommands ───────────────────────────────────────────────────────────

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6--- Bits Help ---");
        sender.sendMessage("§e/bits §7— Show your balance");
        sender.sendMessage("§e/bits balance [player] §7— Check a balance");
        sender.sendMessage("§e/bits pay <player> <amount> §7— Pay another player");
        if (sender.hasPermission("bits.admin")) {
            sender.sendMessage("§e/bits give <player> <amount> §7— §c[Admin] §7Add bits");
            sender.sendMessage("§e/bits take <player> <amount> §7— §c[Admin] §7Remove bits");
            sender.sendMessage("§e/bits set <player> <amount> §7— §c[Admin] §7Set balance");
        }
    }

    private void handleBalance(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cSpecify a player: /bits balance <player>");
                return;
            }
            sender.sendMessage("§6Your balance: §e" + balances.getBalance(player.getUniqueId()) + " bits");
            return;
        }
        if (!sender.hasPermission("bits.admin")) {
            sender.sendMessage("§cYou don't have permission to check other players' balances.");
            return;
        }
        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return; }
        String name = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage("§6" + name + "'s balance: §e" + balances.getBalance(target.getUniqueId()) + " bits"
                + (target.isOnline() ? "" : " §7(offline)"));
    }

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player payer)) { sender.sendMessage("§cOnly players can pay."); return; }
        if (args.length < 3) { sender.sendMessage("§cUsage: /bits pay <player> <amount>"); return; }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found or not online: " + args[1]); return; }
        if (target.equals(payer)) { sender.sendMessage("§cYou cannot pay yourself."); return; }

        long amount = parsePositive(sender, args[2]);
        if (amount < 0) return;

        if (!balances.pay(payer.getUniqueId(), target.getUniqueId(), amount)) {
            sender.sendMessage("§cNot enough bits. Balance: §e" + balances.getBalance(payer.getUniqueId()));
            return;
        }
        payer.sendMessage("§aPaid §e" + amount + " bits §ato §6" + target.getName() + "§a. Balance: §e" + balances.getBalance(payer.getUniqueId()));
        target.sendMessage("§aReceived §e" + amount + " bits §afrom §6" + payer.getName() + "§a. Balance: §e" + balances.getBalance(target.getUniqueId()));
        balances.save();
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bits.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) { sender.sendMessage("§cUsage: /bits give <player> <amount>"); return; }

        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return; }

        long amount = parsePositive(sender, args[2]);
        if (amount < 0) return;

        balances.deposit(target.getUniqueId(), amount);
        String name = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage("§aGave §e" + amount + " bits §ato §6" + name + "§a. Balance: §e" + balances.getBalance(target.getUniqueId()));
        if (target.isOnline()) ((Player) target).sendMessage("§aYou were given §e" + amount + " bits§a. Balance: §e" + balances.getBalance(target.getUniqueId()));
        balances.save();
    }

    private void handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bits.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) { sender.sendMessage("§cUsage: /bits take <player> <amount>"); return; }

        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return; }

        long amount = parsePositive(sender, args[2]);
        if (amount < 0) return;

        String name = target.getName() != null ? target.getName() : args[1];
        if (!balances.withdraw(target.getUniqueId(), amount)) {
            sender.sendMessage("§c" + name + " only has §e" + balances.getBalance(target.getUniqueId()) + " bits§c.");
            return;
        }
        sender.sendMessage("§aTook §e" + amount + " bits §afrom §6" + name + "§a. Balance: §e" + balances.getBalance(target.getUniqueId()));
        if (target.isOnline()) ((Player) target).sendMessage("§c" + amount + " bits were taken from you. Balance: §e" + balances.getBalance(target.getUniqueId()));
        balances.save();
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bits.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) { sender.sendMessage("§cUsage: /bits set <player> <amount>"); return; }

        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return; }

        long amount = parseNonNegative(sender, args[2]);
        if (amount < 0) return;

        String name = target.getName() != null ? target.getName() : args[1];
        balances.setBalance(target.getUniqueId(), amount);
        sender.sendMessage("§aSet §6" + name + "§a's balance to §e" + amount + " bits§a.");
        if (target.isOnline()) ((Player) target).sendMessage("§aYour balance was set to §e" + amount + " bits§a.");
        balances.save();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Parses a positive (>0) long. Returns -1 and sends an error on failure. */
    private long parsePositive(CommandSender sender, String str) {
        try {
            long val = Long.parseLong(str);
            if (val <= 0) { sender.sendMessage("§cAmount must be greater than zero."); return -1; }
            return val;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: §e" + str);
            return -1;
        }
    }

    /** Parses a non-negative (>=0) long. Returns -1 and sends an error on failure. */
    private long parseNonNegative(CommandSender sender, String str) {
        try {
            long val = Long.parseLong(str);
            if (val < 0) { sender.sendMessage("§cAmount cannot be negative."); return -1; }
            return val;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: §e" + str);
            return -1;
        }
    }

    /**
     * Resolves a player name to an OfflinePlayer.
     * Checks online players first, then the server's offline player cache.
     * Returns null if the name has never been seen by this server.
     */
    private OfflinePlayer resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(op.getName())) return op;
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("help", "balance", "pay"));
            if (sender.hasPermission("bits.admin")) subs.addAll(List.of("give", "take", "set"));
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            boolean needsPlayer = sub.equals("pay") || sub.equals("balance")
                    || (sender.hasPermission("bits.admin") && List.of("give", "take", "set").contains(sub));
            if (needsPlayer) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
