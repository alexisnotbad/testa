package com.kingssmp.weapons;

import com.kingssmp.KingsSMP;
import com.kingssmp.utils.WeaponUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class VoidBow extends LegendaryWeapon {

    public VoidBow(KingsSMP plugin) {
        super(plugin, "void_bow", "Void Bow");
    }

    @Override
    public ItemStack buildItem() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        meta.displayName(WeaponUtils.legendaryName("✦ Void Bow", NamedTextColor.DARK_PURPLE));
        meta.lore(List.of(
                WeaponUtils.loreLine("§7Legendary Ranged Weapon", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§5» §dTeleports you to the arrow's", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §dlanding location.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§5» §dHit an entity then §5Shift+RClick", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §dto teleport it to the arrow.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§8[Legendary] [KingsSMP]", NamedTextColor.DARK_GRAY)
        ));
        meta.addEnchant(Enchantment.POWER, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);
        meta.addEnchant(Enchantment.INFINITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        WeaponUtils.applyCustomModelData(meta, 1001);
        meta.getPersistentDataContainer().set(WeaponUtils.WEAPON_ID_KEY,
                org.bukkit.persistence.PersistentDataType.STRING, "void_bow");
        bow.setItemMeta(meta);
        return bow;
    }

    @Override
    protected ShapedRecipe buildRecipe() {
        ItemStack result = buildItem();
        ShapedRecipe recipe = new ShapedRecipe(getKey(), result);
        recipe.shape("EDE", "ESE", "EDE");
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('D', Material.DRAGON_BREATH);
        recipe.setIngredient('S', Material.BOW);
        return recipe;
    }
}
