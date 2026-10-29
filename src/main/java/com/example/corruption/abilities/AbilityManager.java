package com.example.corruption.abilities;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityManager {

    private final JavaPlugin plugin;

    // Координаты для "Двери"
    private final Map<UUID, Location> pendingDoorCoords = new ConcurrentHashMap<>();

    // Кулдауны: UUID -> (id способности -> время окончания в мс)
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public AbilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    // ===== Двери =====
    public void setDoorCoords(UUID uuid, Location loc) {
        pendingDoorCoords.put(uuid, loc);
    }

    public Location getDoorCoords(UUID uuid) {
        return pendingDoorCoords.get(uuid);
    }

    public void clearDoorCoords(UUID uuid) {
        pendingDoorCoords.remove(uuid);
    }

    // ===== Кулдауны =====
    public boolean isOnCooldown(UUID uuid, String abilityId) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return false;
        Long end = map.get(abilityId);
        if (end == null) return false;
        if (System.currentTimeMillis() >= end) {
            map.remove(abilityId);
            return false;
        }
        return true;
    }

    public long getCooldownRemainingMs(UUID uuid, String abilityId) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return 0;
        Long end = map.get(abilityId);
        if (end == null) return 0;
        return Math.max(0, end - System.currentTimeMillis());
    }

    public void setCooldown(UUID uuid, String abilityId, long millis) {
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(abilityId, System.currentTimeMillis() + millis);
    }

    public void clearAll(UUID uuid) {
        pendingDoorCoords.remove(uuid);
        cooldowns.remove(uuid);
    }
}
