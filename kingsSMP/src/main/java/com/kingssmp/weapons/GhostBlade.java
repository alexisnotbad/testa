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

public class GhostBlade extends LegendaryWeapon {

    public GhostBlade(KingsSMP plugin) {
        super(plugin, "ghost_blade", "Ghostblade");
    }

    @Override
    public ItemStack buildItem() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.displayName(WeaponUtils.legendaryName("✦ Ghostblade", NamedTextColor.WHITE));
        meta.lore(List.of(
                WeaponUtils.loreLine("§7Legendary Sword", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§f» §7Grants §ftrue invisibility §7for 5 min.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §7All equipment is hidden from others.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§f» §7Hitting an entity breaks invisibility.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§f» §7Activate with §fShift + Right Click§7.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§8[Legendary] [KingsSMP]", NamedTextColor.DARK_GRAY)
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 4, true);
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        WeaponUtils.applyCustomModelData(meta, 1003);
        meta.getPersistentDataContainer().set(WeaponUtils.WEAPON_ID_KEY, PersistentDataType.STRING, "ghost_blade");
        sword.setItemMeta(meta);
        return sword;
    }

    @Override
    protected ShapedRecipe buildRecipe() {
        ItemStack result = buildItem();
        ShapedRecipe recipe = new ShapedRecipe(getKey(), result);
        recipe.shape("GIG", "GDG", "GIG");
        recipe.setIngredient('G', Material.GHAST_TEAR);
        recipe.setIngredient('I', Material.SPECTRAL_ARROW);
        recipe.setIngredient('D', Material.DIAMOND_SWORD);
        return recipe;
    }
}
