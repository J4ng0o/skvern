package com.example.corruption.abilities.impl;

import com.example.corruption.abilities.AbilityManager;
import com.example.corruption.abilities.DeathNoteManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathNoteAbility implements Listener {

    private final AbilityManager abilityManager;
    private final DeathNoteManager noteManager;

    public DeathNoteAbility(AbilityManager abilityManager, DeathNoteManager noteManager) {
        this.abilityManager = abilityManager;
        this.noteManager = noteManager;
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        Player writer = event.getPlayer();
        ItemStack item = writer.getInventory().getItemInMainHand();

        // Книга должна быть именно Тетрадью Смерти
        if (!noteManager.isDeathNote(item)) return;

        BookMeta meta = event.getNewBookMeta();
        if (!meta.hasPages()) return;
        String lastPage = meta.getPage(meta.getPageCount()).trim();
        if (lastPage.isEmpty()) return;

        // Формат: <ник> <время> <причина>
        String[] parts = lastPage.split("\\s+", 3);
        if (parts.length < 2) {
            writer.sendMessage(ChatColor.RED + "Формат: <ник> <время в секундах> <причина>");
            return;
        }

        String targetName = parts[0];
        int seconds;
        try {
            seconds = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            writer.sendMessage(ChatColor.RED + "Время должно быть числом (в секундах).");
            return;
        }
        if (seconds <= 0) {
            writer.sendMessage(ChatColor.RED + "Время должно быть больше 0.");
            return;
        }
        String reason = parts.length >= 3 ? parts[2] : "неизвестно";

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || target.equals(writer)) {
            writer.sendMessage(ChatColor.RED + "Игрок не найден или это вы.");
            return;
        }

        target.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Ваше имя было записано в тетрадь смерти...");
        target.sendMessage(ChatColor.RED + "Вы умрёте через " + seconds + " сек. Причина: " + reason);
        writer.sendMessage(ChatColor.DARK_PURPLE + target.getName() + " записан. Он умрёт через " + seconds + " сек.");

        // Убийство через указанное время
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline()) {
                    target.sendMessage(ChatColor.DARK_RED + "Вы умерли. Причина: " + reason);
                    target.setHealth(0.0);
                    if (writer.isOnline()) {
                        writer.sendMessage(ChatColor.GRAY + target.getName() + " умер. Причина: " + reason);
                    }
                }
            }
        }.runTaskLater(abilityManager.getPlugin(), seconds * 20L);

        // Забираем полсердца у того, кто пишет
        AttributeInstance maxHealth = writer.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double currentMax = maxHealth.getBaseValue();
            if (currentMax > 2.0) {
                maxHealth.setBaseValue(Math.max(2.0, currentMax - 1.0));
                writer.sendMessage(ChatColor.RED + "Вы потеряли полсердца. Съешьте золотое яблоко, чтобы вернуть.");
            }
        }
    }

    @EventHandler
    public void onEatGoldenApple(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material type = event.getItem().getType();
        if (type != Material.GOLDEN_APPLE && type != Material.ENCHANTED_GOLDEN_APPLE) return;

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null && maxHealth.getBaseValue() < 20.0) {
            double newValue = Math.min(20.0, maxHealth.getBaseValue() + 1.0);
            maxHealth.setBaseValue(newValue);
            player.sendMessage(ChatColor.GREEN + "Вы вернули полсердца. Максимум: " + (newValue / 2.0) + " сердец.");
        }
    }
}
