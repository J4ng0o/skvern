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

        // 1. Запоминаем тип двери и направление
        Block sourceDoor = getDoorBlock(player);
        if (sourceDoor == null) {
            player.sendMessage(ChatColor.RED + "Дверь не найдена.");
            return;
        }
        
        Material doorType = sourceDoor.getType();
        Door sourceData = (Door) sourceDoor.getBlockData();
        BlockFace facing = sourceData.getFacing();

        // 2. Проверяем точку назначения на лаву/огонь
        Block targetBlock = target.getBlock();
        if (targetBlock.getType() == Material.LAVA || targetBlock.getType() == Material.FIRE) {
            player.sendMessage(ChatColor.RED + "Цель опасна — телепорт отменён.");
            return;
        }

        // 3. Подготовка места: очищаем два блока (низ и верх)
        Location doorBase = target.clone();
        Block bottomBlock = doorBase.getBlock();
        Block topBlock = doorBase.clone().add(0, 1, 0).getBlock();
        
        // Проверка: есть ли опора под нижним блоком?
        Block below = bottomBlock.getRelative(BlockFace.DOWN);
        if (!below.getType().isSolid()) {
            // Если опоры нет — ставим временный блок (камень), чтобы дверь не упала
            below.setType(Material.STONE, false);
        }

        // Очищаем место для двери
        bottomBlock.setType(Material.AIR, false);
        topBlock.setType(Material.AIR, false);

        // 4. Телепортируем игрока
        player.teleport(target);

        // 5. Ставим дверь ПРАВИЛЬНО: сначала нижняя половина, потом верхняя
        
        // Нижняя половина
        bottomBlock.setType(doorType, false);
        Door bottomData = (Door) bottomBlock.getBlockData();
        bottomData.setFacing(facing);
        bottomData.setHalf(Bisected.Half.BOTTOM);
        bottomData.setOpen(false); // закрыта (можно true, если хочешь открытую)
        bottomBlock.setBlockData(bottomData, false);

        // Верхняя половина
        topBlock.setType(doorType, false);
        Door topData = (Door) topBlock.getBlockData();
        topData.setFacing(facing);
        topData.setHalf(Bisected.Half.TOP);
        topData.setOpen(false);
        topBlock.setBlockData(topData, false);

        // 6. Принудительно применяем физику (чтобы дверь "увидела" опору)
        bottomBlock.getState().update(true, true);
        topBlock.getState().update(true, true);

        player.sendMessage(ChatColor.DARK_PURPLE + "Вы прошли сквозь двери...");

        manager.clearDoorCoords(player.getUniqueId());
        manager.setCooldown(player.getUniqueId(), ID, COOLDOWN_MS);
    }

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
