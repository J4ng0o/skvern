package com.example.corruption.abilities;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DeathNoteManager {

    private final NamespacedKey key;
    private boolean exists = false; // есть ли тетрадь в мире

    public DeathNoteManager(JavaPlugin plugin) {
        this.key = new NamespacedKey(plugin, "death_note");
    }

    /** Создаёт предмет «Тетрадь Смерти». */
    public ItemStack createDeathNote() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Тетрадь Смерти")
                    .color(NamedTextColor.DARK_RED)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true));
            meta.lore(java.util.List.of(
                    Component.text("Напиши имя — и он умрёт.", NamedTextColor.GRAY),
                    Component.text("Формат: <ник> <время> <причина>", NamedTextColor.DARK_GRAY)
            ));
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    /** Проверяет, является ли предмет Тетрадью Смерти. */
    public boolean isDeathNote(ItemStack item) {
        if (item == null || item.getType() != Material.WRITABLE_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte val = pdc.get(key, PersistentDataType.BYTE);
        return val != null && val == 1;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
