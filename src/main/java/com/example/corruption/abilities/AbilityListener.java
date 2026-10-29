package com.example.corruption.abilities;

import com.example.corruption.abilities.impl.*;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.EnumMap;
import java.util.Map;

public class AbilityListener implements Listener {

    private final AbilityManager manager;
    private final Map<AbilityType, Ability> abilities = new EnumMap<>(AbilityType.class);

    public AbilityListener(AbilityManager manager) {
        this.manager = manager;
        abilities.put(AbilityType.RAGE, new RageAbility(manager));
        abilities.put(AbilityType.DOORS, new DoorsAbility(manager));
        abilities.put(AbilityType.TIME_STOP, new TimeStopAbility(manager));
        abilities.put(AbilityType.DASH, new DashAbility(manager));
    }

    public Ability get(AbilityType type) {
        return abilities.get(type);
    }

    // ===== Ввод координат в чат =====
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String msg = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        String[] parts = msg.split("\\s+");
        if (parts.length != 3) return;

        try {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);

            Location loc = new Location(player.getWorld(), x, y, z);
            manager.setDoorCoords(player.getUniqueId(), loc);
            event.setCancelled(true);

            Bukkit.getScheduler().runTask(manager.getPlugin(), () ->
                    player.sendMessage(ChatColor.GREEN + "Координаты сохранены: " + x + " " + y + " " + z));
        } catch (NumberFormatException ignored) {
        }
    }

    // ===== Единый обработчик F =====
    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) return;

        event.setCancelled(true);

        // Приоритет: Двери (если есть координаты и игрок в двери) > Рывок > Ярость
        Ability doors = abilities.get(AbilityType.DOORS);
        if (doors.canActivate(player)) {
            tryActivate(player, doors);
            return;
        }

        Ability dash = abilities.get(AbilityType.DASH);
        if (!manager.isOnCooldown(player.getUniqueId(), dash.getId())) {
            tryActivate(player, dash);
            return;
        }

        tryActivate(player, abilities.get(AbilityType.RAGE));
    }

    private void tryActivate(Player player, Ability ability) {
        if (manager.isOnCooldown(player.getUniqueId(), ability.getId())) {
            long sec = manager.getCooldownRemainingMs(player.getUniqueId(), ability.getId()) / 1000;
            player.sendMessage(ChatColor.RED + "Перезарядка: " + sec + " сек.");
            return;
        }
        if (!ability.canActivate(player)) {
            String msg = ability.getFailMessage(player);
            if (msg != null) player.sendMessage(msg);
            return;
        }
        ability.activate(player);
    }

    // ===== Очистка =====
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.clearAll(event.getPlayer().getUniqueId());
    }
}
