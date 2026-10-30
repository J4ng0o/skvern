package com.example.corruption.abilities.impl;

import com.example.corruption.abilities.AbilityManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeStopAbility implements Ability {

    private static final String ID = "timestop";
    private static final long DURATION_MS = 5000L;
    private static final long COOLDOWN_MS = 60000L; // 60 секунд

    private final AbilityManager manager;

    // Запоминаем исходное значение прыжка у игроков, чтобы вернуть его потом
    private final Map<UUID, Double> originalJumpValues = new HashMap<>();

    public TimeStopAbility(AbilityManager manager) {
        this.manager = manager;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public long getCooldownMs() {
        return COOLDOWN_MS;
    }

    @Override
    public boolean canActivate(Player player) {
        return true;
    }

    @Override
    public String getFailMessage(Player player) {
        return null;
    }

    @Override
    public void activate(Player player) {
        int durationTicks = (int) (DURATION_MS / 50);

        // Замораживаем ВСЕХ, кроме активатора
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity.equals(player)) continue;

                // Останавливаем движение (Slowness)
                entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS, durationTicks, 255, false, false));

                // Блокируем прыжок через атрибут (только для игроков)
                if (entity instanceof Player frozenPlayer) {
                    AttributeInstance jumpAttr = frozenPlayer.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                    if (jumpAttr != null) {
                        // Запоминаем текущее значение
                        originalJumpValues.put(frozenPlayer.getUniqueId(), jumpAttr.getBaseValue());
                        // Устанавливаем 0 (нет прыжка)
                        jumpAttr.setBaseValue(0.0);
                    }
                }
            }
        }

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 100, 2, 2, 2, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
        player.sendMessage(ChatColor.AQUA + "Время замерло для всех, кроме вас!");

        new BukkitRunnable() {
            @Override
            public void run() {
                // Снимаем эффекты со всех
                for (World world : Bukkit.getWorlds()) {
                    for (LivingEntity entity : world.getLivingEntities()) {
                        if (entity.equals(player)) continue;
                        
                        entity.removePotionEffect(PotionEffectType.SLOWNESS);

                        // Возвращаем прыжок игрокам
                        if (entity instanceof Player frozenPlayer) {
                            AttributeInstance jumpAttr = frozenPlayer.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                            if (jumpAttr != null) {
                                Double original = originalJumpValues.remove(frozenPlayer.getUniqueId());
                                // Если исходное значение было (0.42), возвращаем его, иначе стандарт
                                jumpAttr.setBaseValue(original != null ? original : 0.42);
                            }
                        }
                    }
                }
                // Очищаем оставшиеся записи (на случай, если игрок вышел)
                originalJumpValues.clear();
                player.sendMessage(ChatColor.GRAY + "Время снова потекло.");
            }
        }.runTaskLater(manager.getPlugin(), durationTicks);

        manager.setCooldown(player.getUniqueId(), ID, COOLDOWN_MS);
    }
}
