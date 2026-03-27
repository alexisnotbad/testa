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

public class DragonBoneBlade extends LegendaryWeapon {

    public DragonBoneBlade(KingsSMP plugin) {
        super(plugin, "dragonbone_blade", "Dragonbone Blade");
    }

    @Override
    public ItemStack buildItem() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.displayName(WeaponUtils.legendaryName("✦ Dragonbone Blade", NamedTextColor.DARK_PURPLE));
        meta.lore(List.of(
                WeaponUtils.loreLine("§7Legendary Dragon Sword", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§5» §dSummon a §5rideable Ender Dragon", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §dwith §5Shift + Right Click§d.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§5» §dWhile riding, §5Right Click", NamedTextColor.GRAY),
                WeaponUtils.loreLine("  §dto launch §5Dragon's Breath§d.", NamedTextColor.GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§8Drops near the End Portal", NamedTextColor.DARK_GRAY),
                WeaponUtils.loreLine("§8upon slaying the Ender Dragon.", NamedTextColor.DARK_GRAY),
                WeaponUtils.loreLine("", NamedTextColor.GRAY),
                WeaponUtils.loreLine("§8[Legendary] [KingsSMP]", NamedTextColor.DARK_GRAY)
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 6, true);
        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        WeaponUtils.applyCustomModelData(meta, 1004);
        meta.getPersistentDataContainer().set(WeaponUtils.WEAPON_ID_KEY, PersistentDataType.STRING, "dragonbone_blade");
        sword.setItemMeta(meta);
        return sword;
    }

    @Override
    public void registerRecipe() {
        // No crafting recipe — obtained only by slaying the Ender Dragon
    }

    @Override
    protected ShapedRecipe buildRecipe() {
        return null;
    }
}
