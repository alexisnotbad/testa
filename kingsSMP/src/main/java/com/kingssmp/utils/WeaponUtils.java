package com.kingssmp.utils;

import com.kingssmp.KingsSMP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class WeaponUtils {

    public static final NamespacedKey WEAPON_ID_KEY = new NamespacedKey(KingsSMP.getInstance(), "weapon_id");
    public static final NamespacedKey LIFESTEALER_HEARTS_KEY = new NamespacedKey(KingsSMP.getInstance(), "ls_hearts");
    public static final NamespacedKey GAVEL_KILLS_KEY = new NamespacedKey(KingsSMP.getInstance(), "gavel_kills");
    public static final NamespacedKey GAVEL_TIER_KEY = new NamespacedKey(KingsSMP.getInstance(), "gavel_tier");
    public static final NamespacedKey VOID_BOW_ARROW_KEY = new NamespacedKey(KingsSMP.getInstance(), "voidbow_arrow");

    public static String getWeaponId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(WEAPON_ID_KEY, PersistentDataType.STRING);
    }

    public static boolean isLegendary(ItemStack item) {
        return getWeaponId(item) != null;
    }

    public static ItemStack setWeaponId(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(WEAPON_ID_KEY, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public static Component legendaryName(String name, NamedTextColor color) {
        return Component.text(name)
                .color(color)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true);
    }

    public static Component loreLine(String text, NamedTextColor color) {
        return Component.text(text).color(color).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> legendaryLore(String... lines) {
        return java.util.Arrays.stream(lines)
                .map(l -> loreLine(l, NamedTextColor.GRAY))
                .collect(java.util.stream.Collectors.toList());
    }

    public static void applyCustomModelData(ItemMeta meta, int modelData) {
        meta.setCustomModelData(modelData);
    }
}
