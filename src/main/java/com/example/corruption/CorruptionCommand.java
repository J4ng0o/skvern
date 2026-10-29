package com.example.corruption;

import com.example.corruption.abilities.AbilityManager;
import com.example.corruption.abilities.AbilityType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Set;

public class CorruptionCommand implements CommandExecutor {

    private final CorruptionPlugin plugin;
    private final CorruptionManager manager;
    private final AbilityManager abilityManager; // НОВОЕ

    public CorruptionCommand(CorruptionPlugin plugin,
                             CorruptionManager manager,
                             AbilityManager abilityManager) {
        this.plugin = plugin;
        this.manager = manager;
        this.abilityManager = abilityManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверка оператора
        if (!(sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "Только операторы могут использовать эту команду.");
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден: " + args[1]);
            return true;
        }

        switch (sub) {
            case "give":
                manager.corruptPlayer(target);
                sender.sendMessage(ChatColor.GREEN + target.getName() + " заражён скверной.");
                target.sendMessage(ChatColor.DARK_PURPLE + "Вы чувствуете, как что-то тёмное проникает в вас...");
                break;

            case "symptom":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /corrupt symptom <игрок> <эффект> [уровень]");
                    return true;
                }
                PotionEffectType type = PotionEffectType.getByName(args[2].toUpperCase());
                if (type == null) {
                    sender.sendMessage(ChatColor.RED + "Неизвестный эффект: " + args[2]);
                    return true;
                }
                int amplifier = 0;
                if (args.length >= 4) {
                    try {
                        amplifier = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Уровень должен быть числом.");
                        return true;
                    }
                }
                if (!manager.isCorrupted(target)) {
                    sender.sendMessage(ChatColor.RED + target.getName() + " не заражён скверной. Сначала используйте /corrupt give.");
                    return true;
                }
                manager.addSymptom(target, type, amplifier);
                sender.sendMessage(ChatColor.GREEN + "Симптом " + type.getName() + " (уровень " + (amplifier + 1) + ") выдан " + target.getName() + " навсегда.");
                break;

            case "remove":
                if (!manager.isCorrupted(target)) {
                    sender.sendMessage(ChatColor.YELLOW + target.getName() + " не заражён скверной.");
                    return true;
                }
                manager.removeCorruption(target);
                sender.sendMessage(ChatColor.GREEN + "Скверна и все симптомы сняты с " + target.getName() + ".");
                target.sendMessage(ChatColor.GRAY + "Тьма отступает...");
                break;

            case "check":
                if (manager.isCorrupted(target)) {
                    sender.sendMessage(ChatColor.DARK_PURPLE + target.getName() + " заражён скверной.");
                    Map<PotionEffectType, Integer> symptoms = manager.getSymptoms(target);
                    if (symptoms.isEmpty()) {
                        sender.sendMessage(ChatColor.GRAY + "  Симптомы: нет");
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "  Симптомы:");
                        for (Map.Entry<PotionEffectType, Integer> e : symptoms.entrySet()) {
                            sender.sendMessage(ChatColor.GRAY + "    - " + e.getKey().getName() + " " + (e.getValue() + 1));
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.GREEN + target.getName() + " не заражён.");
                }
                break;

            // ===== НОВОЕ: управление способностями =====
            case "ability":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /corrupt ability <give|remove|list> <игрок> [способность]");
                    return true;
                }
                String action = args[2].toLowerCase();

                if (action.equals("list")) {
                    Set<AbilityType> abilities = abilityManager.getAbilities(target.getUniqueId());
                    if (abilities.isEmpty()) {
                        sender.sendMessage(ChatColor.GRAY + target.getName() + " не имеет способностей.");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "Способности " + target.getName() + ":");
                        for (AbilityType t : abilities) {
                            sender.sendMessage(ChatColor.YELLOW + "  - " + t.name());
                        }
                    }
                    break;
                }

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Укажите способность: rage, doors, timestop, dash");
                    return true;
                }

                AbilityType abilityType = AbilityType.fromId(args[3]);
                if (abilityType == null) {
                    sender.sendMessage(ChatColor.RED + "Неизвестная способность: " + args[3]);
                    return true;
                }

                switch (action) {
                    case "give":
                        abilityManager.grantAbility(target.getUniqueId(), abilityType);
                        sender.sendMessage(ChatColor.GREEN + target.getName() + " получил способность " + abilityType.name() + ".");
                        target.sendMessage(ChatColor.DARK_PURPLE + "Вы получили способность: " + abilityType.name());
                        break;
                    case "remove":
                        abilityManager.revokeAbility(target.getUniqueId(), abilityType);
                        sender.sendMessage(ChatColor.GREEN + "Способность " + abilityType.name() + " отозвана у " + target.getName() + ".");
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Неизвестное действие: " + action);
                }
                break;

            default:
                sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Скверна ===");
        sender.sendMessage(ChatColor.YELLOW + "/corrupt give <игрок>" + ChatColor.GRAY + " — заразить");
        sender.sendMessage(ChatColor.YELLOW + "/corrupt symptom <игрок> <эффект> [уровень]" + ChatColor.GRAY + " — выдать вечный симптом");
        sender.sendMessage(ChatColor.YELLOW + "/corrupt remove <игрок>" + ChatColor.GRAY + " — снять скверну и симптомы");
        sender.sendMessage(ChatColor.YELLOW + "/corrupt check <игрок>" + ChatColor.GRAY + " — проверить статус");
        sender.sendMessage(ChatColor.YELLOW + "/corrupt ability <give|remove|list> <игрок> [способность]" + ChatColor.GRAY + " — управление способностями");
    }
}
