package com.kingssmp.weapons;

import com.kingssmp.KingsSMP;
import com.kingssmp.utils.WeaponUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Lifestealer extends LegendaryWeapon {

    public Lifestealer(KingsSMP plugin) {
        super(plugin, "lifestealer", "Lifestealer");
    }

    @Override
    public ItemStack buildItem() {
        return buildItemWithHearts(0);
    }

    public ItemStack buildItemWithHearts(int extraHearts) {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.displayName(WeaponUtils.legendaryName("✦ Lifestealer", NamedTextColor.DARK_RED));
        meta.lore(List.of(
                WeaponUtils.loreLine("§7Legendary Sword", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§c» §4Steals 10% of enemy's health", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §4passively on every hit.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§c» §4Kill a player to gain", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §4+1 permanent heart (max 4).", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§c❤ Bonus Hearts: §4" + extraHearts + "/4", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§8[Legendary] [KingsSMP]", NamedTextColor.DARK_GRAY)
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);
        meta.addEnchant(Enchantment.MENDING, 2, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        WeaponUtils.applyCustomModelData(meta, 1002);
        meta.getPersistentDataContainer().set(WeaponUtils.WEAPON_ID_KEY, PersistentDataType.STRING, "lifestealer");
        meta.getPersistentDataContainer().set(WeaponUtils.LIFESTEALER_HEARTS_KEY, PersistentDataType.INTEGER, extraHearts);
        sword.setItemMeta(meta);
        return sword;
    }

    @Override
    protected ShapedRecipe buildRecipe() {
        ItemStack result = buildItem();
        ShapedRecipe recipe = new ShapedRecipe(getKey(), result);
        recipe.shape("NRN", "NBN", "NRN");
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        recipe.setIngredient('B', Material.NETHERITE_SWORD);
        return recipe;
    }
}
