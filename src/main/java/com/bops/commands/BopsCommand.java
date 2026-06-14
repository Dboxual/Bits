package com.bops.commands;

import com.bops.BopsPlugin;
import com.bops.data.BalanceManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BopsCommand implements CommandExecutor, TabCompleter {

    private final BopsPlugin plugin;
    private final BalanceManager balances;

    public BopsCommand(BopsPlugin plugin, BalanceManager balances) {
        this.plugin = plugin;
        this.balances = balances;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Usage: /bops <subcommand> [args]");
                return true;
            }
            sender.sendMessage("§6Your balance: §e" + fmt(balances.getBalance(player.getUniqueId())) + " bops");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help"             -> sendHelp(sender);
            case "balance", "bal"   -> handleBalance(sender, args);
            case "pay"              -> handlePay(sender, args);
            case "top"              -> handleTop(sender);
            case "give"             -> handleGive(sender, args);
            case "take"             -> handleTake(sender, args);
            case "set"              -> handleSet(sender, args);
            case "reload"           -> handleReload(sender);
            default                 -> sender.sendMessage("§cUnknown subcommand. Use §e/bops help§c.");
        }
        return true;
    }

    // ── Subcommands ───────────────────────────────────────────────────────────

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6--- Bops Help ---");
        sender.sendMessage("§e/bops §7— Show your balance");
        sender.sendMessage("§e/bops balance [player] §7— Check a balance");
        sender.sendMessage("§e/bops pay <player> <amount> §7— Pay another player");
        sender.sendMessage("§e/bops top §7— Show the top 10 balances");
        if (sender.hasPermission("bops.admin")) {
            sender.sendMessage("§e/bops give <player> <amount> §7— §c[Admin] §7Add bops");
            sender.sendMessage("§e/bops take <player> <amount> §7— §c[Admin] §7Remove bops");
            sender.sendMessage("§e/bops set <player> <amount> §7— §c[Admin] §7Set balance");
            sender.sendMessage("§e/bops reload §7— §c[Admin] §7Reload config");
        }
    }

    private void handleBalance(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cSpecify a player: /bops balance <player>");
                return;
            }
            sender.sendMessage("§6Your balance: §e" + fmt(balances.getBalance(player.getUniqueId())) + " bops");
            return;
        }
        if (!sender.hasPermission("bops.admin")) {
            sender.sendMessage("§cYou don't have permission to check other players' balances.");
            return;
        }
        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return; }
        String name = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage("§6" + name + "'s balance: §e" + fmt(balances.getBalance(target.getUniqueId())) + " bops"
                + (target.isOnline() ? "" : " §7(offline)"));
    }

    private void handleTop(CommandSender sender) {
        Map<UUID, Double> all = balances.getAllBalances();
        if (all.isEmpty()) {
            sender.sendMessage("§7No balances recorded yet.");
            return;
        }
        List<Map.Entry<UUID, Double>> sorted = all.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        sender.sendMessage("§6--- Top Bops ---");
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : sorted) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (name == null) name = "§8Unknown";
            sender.sendMessage("§e" + rank + ". §6" + name + " §7— §e" + fmt(entry.getValue()) + " bops");
            rank++;
        }
    }

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player payer)) { sender.sendMessage("§cOnly players can pay."); return; }
        if (args.length < 3) { sender.sendMessage("§cUsage: /bops pay <player> <amount>"); return; }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found or not online: " + args[1]); return; }
        if (target.equals(payer)) { sender.sendMessage("§cYou cannot pay yourself."); return; }

        double amount = parsePositive(sender, args[2]);
        if (amount < 0) return;

        if (!balances.pay(payer.getUniqueId(), target.getUniqueId(), amount)) {
            sender.sendMessage("§cNot enough bops. Balance: §e" + fmt(balances.getBalance(payer.getUniqueId())));
            return;
        }
        payer.sendMessage("§aPaid §e" + fmt(amount) + " bops §ato §6" + target.getName() + "§a. Balance: §e" + fmt(balances.getBalance(payer.getUniqueId())));
        target.sendMessage("§aReceived §e" + fmt(amount) + " bops §afrom §6" + payer.getName() + "§a. Balance: §e" + fmt(balances.getBalance(target.getUniqueId())));
        balances.save();
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bops.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) { sender.sendMessage("§cUsage: /bops give <player> <amount>"); return; }

        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return; }

        double amount = parsePositive(sender, args[2]);
        if (amount < 0) return;

        balances.deposit(target.getUniqueId(), amount);
        String name = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage("§aGave §e" + fmt(amount) + " bops §ato §6" + name + "§a. Balance: §e" + fmt(balances.getBalance(target.getUniqueId())));
        if (target.isOnline()) ((Player) target).sendMessage("§aYou were given §e" + fmt(amount) + " bops§a. Balance: §e" + fmt(balances.getBalance(target.getUniqueId())));
        balances.save();
    }

    private void handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bops.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) { sender.sendMessage("§cUsage: /bops take <player> <amount>"); return; }

        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return; }

        double amount = parsePositive(sender, args[2]);
        if (amount < 0) return;

        String name = target.getName() != null ? target.getName() : args[1];
        if (!balances.withdraw(target.getUniqueId(), amount)) {
            sender.sendMessage("§c" + name + " only has §e" + fmt(balances.getBalance(target.getUniqueId())) + " bops§c.");
            return;
        }
        sender.sendMessage("§aTook §e" + fmt(amount) + " bops §afrom §6" + name + "§a. Balance: §e" + fmt(balances.getBalance(target.getUniqueId())));
        if (target.isOnline()) ((Player) target).sendMessage("§c" + fmt(amount) + " bops were taken from you. Balance: §e" + fmt(balances.getBalance(target.getUniqueId())));
        balances.save();
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bops.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) { sender.sendMessage("§cUsage: /bops set <player> <amount>"); return; }

        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found: " + args[1]); return; }

        double amount = parseNonNegative(sender, args[2]);
        if (amount < 0) return;

        String name = target.getName() != null ? target.getName() : args[1];
        balances.setBalance(target.getUniqueId(), amount);
        sender.sendMessage("§aSet §6" + name + "§a's balance to §e" + fmt(amount) + " bops§a.");
        if (target.isOnline()) ((Player) target).sendMessage("§aYour balance was set to §e" + fmt(amount) + " bops§a.");
        balances.save();
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("bops.admin")) { sender.sendMessage("§cNo permission."); return; }
        plugin.reload();
        sender.sendMessage("§aBops config reloaded.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String fmt(double v) {
        return String.format("%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private double parsePositive(CommandSender sender, String str) {
        try {
            double val = Double.parseDouble(str);
            if (val <= 0) { sender.sendMessage("§cAmount must be greater than zero."); return -1; }
            return val;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: §e" + str);
            return -1;
        }
    }

    private double parseNonNegative(CommandSender sender, String str) {
        try {
            double val = Double.parseDouble(str);
            if (val < 0) { sender.sendMessage("§cAmount cannot be negative."); return -1; }
            return val;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: §e" + str);
            return -1;
        }
    }

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
            List<String> subs = new ArrayList<>(List.of("help", "balance", "pay", "top"));
            if (sender.hasPermission("bops.admin")) subs.addAll(List.of("give", "take", "set", "reload"));
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            boolean needsPlayer = sub.equals("pay") || sub.equals("balance")
                    || (sender.hasPermission("bops.admin") && List.of("give", "take", "set").contains(sub));
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
