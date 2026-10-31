package com.example.corruption.abilities;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DeathNoteCommand implements CommandExecutor {

    private final DeathNoteManager noteManager;

    public DeathNoteCommand(DeathNoteManager noteManager) {
        this.noteManager = noteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Только ОП
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Только операторы могут выдать тетрадь.");
            return true;
        }

        // Тетрадь уже существует?
        if (noteManager.isExists()) {
            sender.sendMessage(ChatColor.RED + "Тетрадь Смерти уже существует в мире. Уничтожьте её сначала.");
            return true;
        }

        // Кому выдаём?
        Player target;
        if (args.length >= 1) {
            target = org.bukkit.Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Игрок не найден: " + args[0]);
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p; // если аргумент не указан — выдаём себе
        } else {
            sender.sendMessage(ChatColor.RED + "Укажите ник игрока: /corrupt givenote <ник>");
            return true;
        }

        ItemStack note = noteManager.createDeathNote();
        target.getInventory().addItem(note);
        noteManager.setExists(true);

        sender.sendMessage(ChatColor.GREEN + "Тетрадь Смерти выдана " + target.getName() + ".");
        target.sendMessage(ChatColor.DARK_RED + "Вы получили Тетрадь Смерти. Напишите в неё имя...");
        return true;
    }
}
