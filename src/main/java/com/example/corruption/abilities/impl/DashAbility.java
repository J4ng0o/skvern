package com.example.corruption.abilities.impl;

import com.example.corruption.abilities.AbilityManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DashAbility implements Ability {

    private static final String ID = "dash";
    private static final long COOLDOWN_MS = 8000L;
    private static final int MAX_DISTANCE = 7;

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
        Vector dir = player.getLocation().getDirection().normalize();
        Location start = player.getLocation().clone();
        Location target = start.clone();

        for (int i = 1; i <= MAX_DISTANCE; i++) {
            Location check = start.clone().add(dir.clone().multiply(i));
            Block b = check.getBlock();
            if (!b.isPassable() || b.getType() == Material.LAVA) break;
            target = check;
        }

        player.teleport(target);
        player.getWorld().spawnParticle(Particle.SMOKE, start, 40, 0.3, 0.3, 0.3, 0.05);
        player.getWorld().spawnParticle(Particle.SMOKE, target, 40, 0.3, 0.3, 0.3, 0.05);
        player.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Вы совершили тёмный рывок!");

        manager.setCooldown(player.getUniqueId(), ID, COOLDOWN_MS);
    }
}
