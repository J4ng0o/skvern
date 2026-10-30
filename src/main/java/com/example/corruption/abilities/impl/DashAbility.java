package com.example.corruption.abilities.impl;

import com.example.corruption.abilities.AbilityManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DashAbility implements Ability {

    private static final String ID = "dash";
    private static final long COOLDOWN_MS = 10000L; // 10 секунд
    private static final double DISTANCE = 10.0;    // 10 блоков

    private final AbilityManager manager;

    public DashAbility(AbilityManager manager) {
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
        Location start = player.getLocation().clone();
        Vector dir = start.getDirection().normalize();
        Location target = start.clone().add(dir.multiply(DISTANCE));

        // Проверка границ мира (чтобы не упасть в пустоту)
        if (target.getY() < target.getWorld().getMinHeight()
                || target.getY() > target.getWorld().getMaxHeight()) {
            player.sendMessage(ChatColor.RED + "Рывок за границы мира невозможен.");
            return;
        }

        // Ищем безопасную точку: если в target блок занят, поднимаемся вверх
        Location safeTarget = findSafeLocation(target, player);
        
        if (safeTarget == null) {
            player.sendMessage(ChatColor.RED + "Не удалось найти безопасное место для выхода.");
            return;
        }

        // Телепортируем (сквозь стены)
        player.teleport(safeTarget);

        // Эффекты
        player.getWorld().spawnParticle(Particle.SMOKE, start, 40, 0.3, 0.3, 0.3, 0.05);
        player.getWorld().spawnParticle(Particle.SMOKE, safeTarget, 40, 0.3, 0.3, 0.3, 0.05);
        player.playSound(safeTarget, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Вы совершили тёмный рывок!");

        manager.setCooldown(player.getUniqueId(), ID, COOLDOWN_MS);
    }

    /**
     * Ищет безопасную точку для телепорта.
     * Если точка занята — поднимается вверх, пока не найдёт свободное место.
     */
    private Location findSafeLocation(Location target, Player player) {
        Location check = target.clone();
        World world = check.getWorld();

        // Максимум поднимаемся на 10 блоков вверх (или до maxHeight)
        for (int i = 0; i < 10; i++) {
            Block feet = check.getBlock();
            Block head = check.clone().add(0, 1, 0).getBlock();

            boolean feetFree = feet.isPassable() && feet.getType() != Material.LAVA;
            boolean headFree = head.isPassable() && head.getType() != Material.LAVA;

            if (feetFree && headFree) {
                // Нашли свободное место. Проверим, что под ногами есть опора (чтобы не падать)
                Block below = check.clone().add(0, -1, 0).getBlock();
                if (below.getType() != Material.AIR && below.isSolid()) {
                    return check;
                }
                // Если опоры нет — тоже возвращаем (пусть падает, это "рывок")
                return check;
            }

            // Поднимаемся на 1 блок вверх
            check.add(0, 1, 0);

            // Если вышли за границы мира — отменяем
            if (check.getY() > world.getMaxHeight()) {
                return null;
            }
        }

        // Если за 10 попыток не нашли — возвращаем null
        return null;
    }
}
