package com.example.corruption.abilities.impl;

import com.example.corruption.abilities.AbilityManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
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
        return getDoorBlock(player) != null;
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

        // 1. Запоминаем блок двери, в которой стоит игрок
        Block sourceDoor = getDoorBlock(player);
        if (sourceDoor == null) {
            player.sendMessage(ChatColor.RED + "Дверь не найдена.");
            return;
        }
        
        // Сохраняем тип двери и её данные (направление, половина)
        Material doorType = sourceDoor.getType();
        Door sourceData = (Door) sourceDoor.getBlockData();
        BlockFace facing = sourceData.getFacing();

        // 2. Проверяем, что точка назначения не занята (лава/огонь)
        Block targetBlock = target.getBlock();
        if (targetBlock.getType() == Material.LAVA || targetBlock.getType() == Material.FIRE) {
            player.sendMessage(ChatColor.RED + "Цель опасна — телепорт отменён.");
            return;
        }

        // 3. Очищаем место для двери (два блока вверх)
        Location doorBase = target.clone();
        Block bottomBlock = doorBase.getBlock();
        Block topBlock = doorBase.clone().add(0, 1, 0).getBlock();
        
        // Убираем блоки, чтобы дверь влезла
        if (!bottomBlock.isPassable()) bottomBlock.setType(Material.AIR);
        if (!topBlock.isPassable()) topBlock.setType(Material.AIR);

        // 4. Телепортируем игрока
        player.teleport(target);

        // 5. Ставим дверь на точке прибытия
        // Нижняя половина
        bottomBlock.setType(doorType);
        Door bottomData = (Door) bottomBlock.getBlockData();
        bottomData.setFacing(facing);
        bottomData.setHalf(Bisected.Half.BOTTOM);
        bottomBlock.setBlockData(bottomData, false);

        // Верхняя половина
        topBlock.setType(doorType);
        Door topData = (Door) topBlock.getBlockData();
        topData.setFacing(facing);
        topData.setHalf(Bisected.Half.TOP);
        topBlock.setBlockData(topData, false);

        player.sendMessage(ChatColor.DARK_PURPLE + "Вы прошли сквозь двери...");

        manager.clearDoorCoords(player.getUniqueId());
        manager.setCooldown(player.getUniqueId(), ID, COOLDOWN_MS);
    }

    /**
     * Возвращает блок двери, в которой стоит игрок (или null, если не в двери).
     */
    private Block getDoorBlock(Player player) {
        Block feet = player.getLocation().getBlock();
        Block head = player.getLocation().add(0, 1, 0).getBlock();
        
        if (isDoor(feet.getType())) return feet;
        if (isDoor(head.getType())) return head;
        return null;
    }

    private boolean isDoor(Material material) {
        return material.name().endsWith("_DOOR");
    }
}
