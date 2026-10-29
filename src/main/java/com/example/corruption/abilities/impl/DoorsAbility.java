package com.example.corruption.abilities.impl;

import com.example.corruption.abilities.AbilityManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class DoorsAbility implements Ability {

    private static final String ID = "doors";
    private static final long COOLDOWN_MS = 10000L;

    private final AbilityManager manager;

    public DoorsAbility(AbilityManager manager) {
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
        if (manager.getDoorCoords(player.getUniqueId()) == null) return false;
        return isPlayerInDoor(player);
    }

    @Override
    public String getFailMessage(Player player) {
        if (manager.getDoorCoords(player.getUniqueId()) == null) {
            return ChatColor.RED + "Сначала введите координаты в чат (x y z).";
        }
        return ChatColor.RED + "Вы должны стоять в двери, чтобы активировать способность.";
    }

    @Override
    public void activate(Player player) {
        Location target = manager.getDoorCoords(player.getUniqueId());
        if (target == null) return;

        Block targetBlock = target.getBlock();
        if (targetBlock.getType() == Material.LAVA || targetBlock.getType() == Material.FIRE) {
            player.sendMessage(ChatColor.RED + "Цель опасна — телепорт отменён.");
            return;
        }

        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(),
                60, 0.5, 1, 0.5, 0.5);
        player.teleport(target);
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, target,
                60, 0.5, 1, 0.5, 0.5);
        player.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Вы прошли сквозь двери...");

        manager.clearDoorCoords(player.getUniqueId());
        manager.setCooldown(player.getUniqueId(), ID, COOLDOWN_MS);
    }

    private boolean isPlayerInDoor(Player player) {
        Block feet = player.getLocation().getBlock();
        Block head = player.getLocation().add(0, 1, 0).getBlock();
        return isDoor(feet.getType()) || isDoor(head.getType());
    }

    private boolean isDoor(Material material) {
        return material.name().endsWith("_DOOR");
    }
}
