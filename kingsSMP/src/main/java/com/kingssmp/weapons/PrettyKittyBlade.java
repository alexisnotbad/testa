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

public class PrettyKittyBlade extends LegendaryWeapon {

    public PrettyKittyBlade(KingsSMP plugin) {
        super(plugin, "pretty_kitty_blade", "Pretty Kitty Princess Blade");
    }

    @Override
    public ItemStack buildItem() {
        ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.displayName(WeaponUtils.legendaryName("✦ Pretty Kitty Princess Blade", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(
                WeaponUtils.loreLine("§7Legendary Sword", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§d» §5Shift + Right Click §dto summon", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §da §5horde of cats§d to your aid.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§d» §5Backstab Passive§d: Deal §52x damage", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §dwhen hitting from behind.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§8[Legendary] [KingsSMP]", NamedTextColor.DARK_GRAY)
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 7, true);
        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        meta.addEnchant(Enchantment.LOOTING, 3, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        WeaponUtils.applyCustomModelData(meta, 1006);
        meta.getPersistentDataContainer().set(WeaponUtils.WEAPON_ID_KEY, PersistentDataType.STRING, "pretty_kitty_blade");
        sword.setItemMeta(meta);
        return sword;
    }

    @Override
    protected ShapedRecipe buildRecipe() {
        ItemStack result = buildItem();
        ShapedRecipe recipe = new ShapedRecipe(getKey(), result);
        // G = Gold Block, S = String (cats drop/relate to), P = Pink Dye
        recipe.shape("GSG", "GPG", "GSG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('P', Material.PINK_DYE);
        return recipe;
    }
}
