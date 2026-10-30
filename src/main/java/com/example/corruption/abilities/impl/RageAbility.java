package com.example.corruption.abilities.impl;

import com.example.corruption.abilities.AbilityManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class RageAbility implements Ability {

    private static final String ID = "rage";
    private static final long DURATION_MS = 7000L; // 7 секунд
    private static final long COOLDOWN_MS = 15000L; // 15 секунд

    private final AbilityManager manager;

    public RageAbility(AbilityManager manager) {
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

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, 4, false, true, true));

        AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeed != null) {
            double base = attackSpeed.getBaseValue();
            AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(),
                    "corruption_rage",
                    base,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND
            );
            attackSpeed.addModifier(modifier);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        AttributeInstance inst = player.getAttribute(Attribute.ATTACK_SPEED);
                        if (inst != null) inst.removeModifier(modifier);
                    }
                }
            }.runTaskLater(manager.getPlugin(), durationTicks);
        }

        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation(), 40, 0.5, 1, 0.5, 0.1);
        
        // Технологичный звук (как у скалка/кондуита)
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.8f);
        
        player.sendMessage(ChatColor.DARK_PURPLE + "Вы впадаете в ярость!");

        // Трейл (розово-фиолетовый)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= durationTicks) {
                    cancel();
                    return;
                }
                Location behind = player.getLocation().subtract(
                        player.getLocation().getDirection().multiply(0.5)
                ).add(0, 1.0, 0);

                player.getWorld().spawnParticle(Particle.DUST, behind, 3, 0.3, 0.3, 0.3, 0.0,
                        new Particle.DustOptions(Color.fromRGB(255, 105, 180), 1.5f));
                player.getWorld().spawnParticle(Particle.DUST, behind, 3, 0.3, 0.3, 0.3, 0.0,
                        new Particle.DustOptions(Color.fromRGB(138, 43, 226), 1.5f));

                ticks += 3;
            }
        }.runTaskTimer(manager.getPlugin(), 0L, 3L);

        manager.setCooldown(player.getUniqueId(), ID, COOLDOWN_MS);
    }
}
