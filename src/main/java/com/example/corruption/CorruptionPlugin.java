package com.example.corruption;

import org.bukkit.plugin.java.JavaPlugin;

public class CorruptionPlugin extends JavaPlugin {

    private CorruptionManager corruptionManager;

    @Override
    public void onEnable() {
        this.corruptionManager = new CorruptionManager(this);
        getCommand("corrupt").setExecutor(new CorruptionCommand(this, corruptionManager));
        getServer().getPluginManager().registerEvents(new CorruptionListener(corruptionManager), this);
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
