package com.example.corruption;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CorruptionListener implements Listener {

    private final CorruptionManager manager;

    public CorruptionListener(CorruptionManager manager) {
        this.manager = manager;

        // Каждые 10 тиков (0.5 сек) спавним частицы душ у заражённых
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : player.getServer().getOnlinePlayers()) {
                    if (manager.isCorrupted(player)) {
                        player.getWorld().spawnParticle(
                                Particle.SOUL,
                                player.getLocation().add(0, 1, 0),
                                3,      // количество
                                0.3, 0.5, 0.3,  // разброс
                                0.01    // скорость
                        );
                    }
                }
            }
        }.runTaskTimer(manager.getPlugin(), 0L, 10L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // При входе восстанавливаем симптомы, если игрок заражён
        manager.reapplyAllSymptoms(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // После смерти и респавна эффекты выдаются заново
        manager.reapplyAllSymptoms(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Ничего не делаем — эффекты снимутся смертью, но respawn их вернёт
    }
}
