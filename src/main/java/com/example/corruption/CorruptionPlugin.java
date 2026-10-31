package com.example.corruption;

import com.example.corruption.abilities.AbilityListener;
import com.example.corruption.abilities.AbilityManager;
import com.example.corruption.abilities.DeathNoteManager;
import com.example.corruption.abilities.impl.DeathNoteAbility;
import org.bukkit.plugin.java.JavaPlugin;

public class CorruptionPlugin extends JavaPlugin {

    private CorruptionManager corruptionManager;
    private AbilityManager abilityManager;
    private DeathNoteManager deathNoteManager;

    @Override
    public void onEnable() {
        this.corruptionManager = new CorruptionManager(this);
        this.abilityManager = new AbilityManager(this);
        this.deathNoteManager = new DeathNoteManager(this);

        getCommand("corrupt").setExecutor(
                new CorruptionCommand(this, corruptionManager, abilityManager, deathNoteManager));

        getServer().getPluginManager().registerEvents(new CorruptionListener(corruptionManager), this);
        getServer().getPluginManager().registerEvents(new AbilityListener(abilityManager), this);
        
        // Регистрируем слушатель для Тетради Смерти
        getServer().getPluginManager().registerEvents(
                new DeathNoteAbility(abilityManager, deathNoteManager), this);

        getLogger().info("CorruptionPlugin включён.");
    }

    @Override
    public void onDisable() {
        if (corruptionManager != null) corruptionManager.save();
        if (abilityManager != null) abilityManager.save();
        getLogger().info("CorruptionPlugin выключен.");
    }
}
