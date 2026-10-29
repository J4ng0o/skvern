package com.example.corruption;

import com.example.corruption.abilities.AbilityListener;
import com.example.corruption.abilities.AbilityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CorruptionPlugin extends JavaPlugin {

    private CorruptionManager corruptionManager;
    private AbilityManager abilityManager; // НОВОЕ: поле класса

    @Override
    public void onEnable() {
        this.corruptionManager = new CorruptionManager(this);
        this.abilityManager = new AbilityManager(this); // НОВОЕ: создаём ДО команды

        // Команда получает ОБА менеджера
        getCommand("corrupt").setExecutor(
                new CorruptionCommand(this, corruptionManager, abilityManager));

        getServer().getPluginManager().registerEvents(
                new CorruptionListener(corruptionManager), this);
        getServer().getPluginManager().registerEvents(
                new AbilityListener(abilityManager), this);

        getLogger().info("CorruptionPlugin включён.");
    }

    @Override
    public void onDisable() {
        if (corruptionManager != null) corruptionManager.save();
        if (abilityManager != null) abilityManager.save(); // НОВОЕ
        getLogger().info("CorruptionPlugin выключен.");
    }

    public CorruptionManager getCorruptionManager() {
        return corruptionManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
}
