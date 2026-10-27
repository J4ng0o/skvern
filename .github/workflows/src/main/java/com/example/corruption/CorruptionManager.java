package com.example.corruption;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CorruptionManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private final Set<UUID> corruptedPlayers = new HashSet<>();
    // UUID игрока -> (тип эффекта -> уровень)
    private final Map<UUID, Map<PotionEffectType, Integer>> symptoms = new HashMap<>();

    public CorruptionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    // ===== Метка скверны =====
    public boolean isCorrupted(Player player) {
        return corruptedPlayers.contains(player.getUniqueId());
    }

    public void corruptPlayer(Player player) {
        corruptedPlayers.add(player.getUniqueId());
        save();
    }

    public void removeCorruption(Player player) {
        UUID uuid = player.getUniqueId();
        corruptedPlayers.remove(uuid);

        // Снимаем все выданные через плагин симптомы
        Map<PotionEffectType, Integer> playerSymptoms = symptoms.remove(uuid);
        if (playerSymptoms != null) {
            for (PotionEffectType type : playerSymptoms.keySet()) {
                player.removePotionEffect(type);
            }
        }
        save();
    }

    // ===== Симптомы (постоянные эффекты) =====
    public void addSymptom(Player player, PotionEffectType type, int amplifier) {
        symptoms.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(type, amplifier);
        applySymptom(player, type, amplifier);
        save();
    }

    public void applySymptom(Player player, PotionEffectType type, int amplifier) {
        // INFINITE_DURATION — эффект никогда не заканчивается
        player.addPotionEffect(new PotionEffect(
                type,
                PotionEffect.INFINITE_DURATION,
                amplifier,
                false,  // ambient
                true,   // particles (можно выключить, если частицы душ уже есть от скверны)
                true    // icon
        ));
    }

    // Восстановление всех симптомов после смерти/респавна
    public void reapplyAllSymptoms(Player player) {
        if (!isCorrupted(player)) return;
        Map<PotionEffectType, Integer> playerSymptoms = symptoms.get(player.getUniqueId());
        if (playerSymptoms == null) return;
        for (Map.Entry<PotionEffectType, Integer> entry : playerSymptoms.entrySet()) {
            applySymptom(player, entry.getKey(), entry.getValue());
        }
    }

    public Map<PotionEffectType, Integer> getSymptoms(Player player) {
        return symptoms.getOrDefault(player.getUniqueId(), Collections.emptyMap());
    }

    // ===== Сохранение / загрузка =====
    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        List<String> corruptedList = config.getStringList("corrupted");
        for (String s : corruptedList) {
            try {
                corruptedPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }

        if (config.getConfigurationSection("symptoms") != null) {
            for (String uuidStr : config.getConfigurationSection("symptoms").getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                Map<PotionEffectType, Integer> map = new HashMap<>();
                for (String effectName : config.getConfigurationSection("symptoms." + uuidStr).getKeys(false)) {
                    PotionEffectType type = PotionEffectType.getByName(effectName);
                    if (type != null) {
                        int amp = config.getInt("symptoms." + uuidStr + "." + effectName);
                        map.put(type, amp);
                    }
                }
                if (!map.isEmpty()) {
                    symptoms.put(uuid, map);
                }
            }
        }
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();

        config.set("corrupted", corruptedPlayers.stream().map(UUID::toString).toList());

        for (Map.Entry<UUID, Map<PotionEffectType, Integer>> entry : symptoms.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<PotionEffectType, Integer> e : entry.getValue().entrySet()) {
                config.set("symptoms." + uuidStr + "." + e.getKey().getName(), e.getValue());
            }
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить данные: " + e.getMessage());
        }
    }

    public Set<UUID> getCorruptedPlayers() {
        return Collections.unmodifiableSet(corruptedPlayers);
    }
}
