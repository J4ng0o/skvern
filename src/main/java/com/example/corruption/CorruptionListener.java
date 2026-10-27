package com.example.corruption;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
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
                for (Player player : Bukkit.getOnlinePlayers()) {
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

    // 1. Выдача при входе на сервер (если он вышел и зашел)
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
            manager.reapplyAllSymptoms(event.getPlayer());
        }, 2L); // Задержка 2 тика, чтобы клиент точно прогрузился
    }

    // 2. ИСПРАВЛЕНО: Выдача после смерти с задержкой
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Даем серверу 1 тик, чтобы создать игрока и сбросить старые эффекты
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
            manager.reapplyAllSymptoms(event.getPlayer());
        }, 1L); 
    }

    // 3. ЗАЩИТА ОТ МОЛОКА: Если игрок пьет молоко, возвращаем эффекты обратно
    @EventHandler
    public void onPlayerDrinkMilk(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            Player player = event.getPlayer();
            if (manager.isCorrupted(player)) {
                // Ждем 1 тик, пока молоко снимет эффекты, и выдаем их заново
                Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
                    manager.reapplyAllSymptoms(player);
                }, 1L);
            }
        }
    }
}
