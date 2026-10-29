package com.example.corruption.abilities;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityManager {

    private final JavaPlugin plugin;
    private final File dataFile;

    // Координаты для "Двери"
    private final Map<UUID, Location> pendingDoorCoords = new ConcurrentHashMap<>();

    // Кулдауны: UUID -> (id способности -> время окончания в мс)
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    // Выданные способности: UUID -> набор AbilityType (теперь строго один элемент)
    private final Map<UUID, Set<AbilityType>> playerAbilities = new ConcurrentHashMap<>();

    public AbilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "abilities.yml");
        load();
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

    // ===== Способности =====
    public boolean hasAbility(UUID uuid, AbilityType type) {
        Set<AbilityType> set = playerAbilities.get(uuid);
        return set != null && set.contains(type);
    }

    public void grantAbility(UUID uuid, AbilityType type) {
        // Строго одна способность: очищаем старые перед добавлением
        Set<AbilityType> set = ConcurrentHashMap.newKeySet();
        set.add(type);
        playerAbilities.put(uuid, set);
        save();
    }

    public void revokeAbility(UUID uuid, AbilityType type) {
        Set<AbilityType> set = playerAbilities.get(uuid);
        if (set != null && set.remove(type)) {
            if (set.isEmpty()) playerAbilities.remove(uuid);
            save();
        }
    }

    public Set<AbilityType> getAbilities(UUID uuid) {
        return playerAbilities.getOrDefault(uuid, Collections.emptySet());
    }

    public void clearAbilities(UUID uuid) {
        if (playerAbilities.remove(uuid) != null) {
            save();
        }
    }

    // ===== Очистка всего (при выходе игрока) =====
    public void clearAll(UUID uuid) {
        pendingDoorCoords.remove(uuid);
        cooldowns.remove(uuid);
        // Способности НЕ удаляем — они должны сохраняться
    }

    // ===== Сохранение / загрузка =====
    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        if (config.getConfigurationSection("abilities") != null) {
            for (String uuidStr : config.getConfigurationSection("abilities").getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                List<String> list = config.getStringList("abilities." + uuidStr);
                Set<AbilityType> set = ConcurrentHashMap.newKeySet();
                for (String id : list) {
                    AbilityType type = AbilityType.fromId(id);
                    if (type != null) set.add(type);
                }
                if (!set.isEmpty()) {
                    playerAbilities.put(uuid, set);
                }
            }
        }
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Set<AbilityType>> entry : playerAbilities.entrySet()) {
            List<String> list = new ArrayList<>();
            for (AbilityType type : entry.getValue()) {
                list.add(type.getId());
            }
            config.set("abilities." + entry.getKey().toString(), list);
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить abilities.yml: " + e.getMessage());
        }
    }
}
