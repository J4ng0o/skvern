package com.example.corruption.abilities.impl;

import com.example.corruption.abilities.AbilityManager;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeStopAbility implements Ability {

    private static final String ID = "timestop";
    private static final long DURATION_MS = 5000L;
    private static final long COOLDOWN_MS = 30000L;

    private final AbilityManager manager;

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

        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity instanceof Player) continue;
                entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS, durationTicks, 255, false, false));
                entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.JUMP_BOOST, durationTicks, 128, false, false));
            }
        }

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 100, 2, 2, 2, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
        player.sendMessage(ChatColor.AQUA + "Время замерло для всех, кроме вас!");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (LivingEntity entity : world.getLivingEntities()) {
                        if (entity instanceof Player) continue;
                        entity.removePotionEffect(PotionEffectType.SLOWNESS);
                        entity.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    }
                }
                player.sendMessage(ChatColor.GRAY + "Время снова потекло.");
            }
        }.runTaskLater(manager.getPlugin(), durationTicks);

        manager.setCooldown(player.getUniqueId(), ID, DURATION_MS + COOLDOWN_MS);
    }
}
