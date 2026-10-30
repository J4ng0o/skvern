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
    private static final long COOLDOWN_MS = 60000L;
    private static final double RADIUS = 25.0;

    private final AbilityManager manager;
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
        java.util.List<LivingEntity> frozen = new java.util.ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            // Пропускаем миры, отличные от мира игрока
            if (!world.equals(player.getWorld())) continue;

            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity.equals(player)) continue;
                if (entity.getLocation().distanceSquared(player.getLocation()) > RADIUS * RADIUS) continue;

                entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS, durationTicks, 255, false, false));
                frozen.add(entity);

                if (entity instanceof Player frozenPlayer) {
                    AttributeInstance jumpAttr = frozenPlayer.getAttribute(Attribute.JUMP_STRENGTH);
                    if (jumpAttr != null) {
                        originalJumpValues.put(frozenPlayer.getUniqueId(), jumpAttr.getBaseValue());
                        jumpAttr.setBaseValue(0.0);
                    }
                }
            }
        }

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 100, 2, 2, 2, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
        player.sendMessage(ChatColor.AQUA + "Время замерло в радиусе 25 блоков!");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity entity : frozen) {
                    entity.removePotionEffect(PotionEffectType.SLOWNESS);
                    if (entity instanceof Player fp) {
                        AttributeInstance jumpAttr = fp.getAttribute(Attribute.JUMP_STRENGTH);
                        if (jumpAttr != null) {
                            Double original = originalJumpValues.remove(fp.getUniqueId());
                            jumpAttr.setBaseValue(original != null ? original : 0.42);
                        }
                    }
                }
                originalJumpValues.clear();
                player.sendMessage(ChatColor.GRAY + "Время снова потекло.");
            }
        }.runTaskLater(manager.getPlugin(), durationTicks);

        manager.setCooldown(player.getUniqueId(), ID, COOLDOWN_MS);
    }
}
