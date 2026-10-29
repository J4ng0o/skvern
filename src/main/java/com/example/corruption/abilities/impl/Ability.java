package com.example.corruption.abilities.impl;

import org.bukkit.entity.Player;

public interface Ability {

    /**
     * Уникальный ID способности (совпадает с AbilityType.getId()).
     */
    String getId();

    /**
     * Кулдаун в миллисекундах.
     */
    long getCooldownMs();

    /**
     * Можно ли активировать способность прямо сейчас
     * (например, "Двери" требует стоять в двери и иметь координаты).
     */
    boolean canActivate(Player player);

    /**
     * Основная логика при активации.
     */
    void activate(Player player);

    /**
     * Сообщение об ошибке, если canActivate() вернул false.
     */
    String getFailMessage(Player player);
}
