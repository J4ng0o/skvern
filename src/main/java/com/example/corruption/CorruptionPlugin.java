package com.example.corruption;

import com.example.corruption.abilities.AbilityListener;   // НОВОЕ
import com.example.corruption.abilities.AbilityManager;    // НОВОЕ
import org.bukkit.plugin.java.JavaPlugin;

public class CorruptionPlugin extends JavaPlugin {

    private CorruptionManager corruptionManager;

    @Override
    public void onEnable() {
        this.corruptionManager = new CorruptionManager(this);
        getCommand("corrupt").setExecutor(new CorruptionCommand(this, corruptionManager));
        getServer().getPluginManager().registerEvents(new CorruptionListener(corruptionManager), this);

        // НОВОЕ: способности
        AbilityManager abilityManager = new AbilityManager(this);
        getServer().getPluginManager().registerEvents(
                new AbilityListener(abilityManager), this);

        getLogger().info("CorruptionPlugin включён.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CorruptionPlugin выключен.");
    }

    public CorruptionManager getCorruptionManager() {
        return corruptionManager;
    }
}
