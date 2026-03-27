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

import java.util.ArrayList;
import java.util.List;

public class JudgementGavel extends LegendaryWeapon {

    public JudgementGavel(KingsSMP plugin) {
        super(plugin, "judgement_gavel", "Judgement Gavel");
    }

    @Override
    public ItemStack buildItem() {
        return buildItemWithTier(0, 0);
    }

    public ItemStack buildItemWithTier(int kills, int tier) {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();
        meta.displayName(WeaponUtils.legendaryName("✦ Judgement Gavel " + getTierSuffix(tier), NamedTextColor.GOLD));

        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(WeaponUtils.loreLine("§7Legendary Mace", NamedTextColor.GRAY));
        lore.add(WeaponUtils.loreLine("", NamedTextColor.GRAY));
        lore.add(WeaponUtils.loreLine("§6» §eUpgrades every 2 kills (4 tiers).", NamedTextColor.GRAY));
        lore.add(WeaponUtils.loreLine("§6Kills: §e" + kills + " §7| §6Tier: §e" + tier + "/4", NamedTextColor.GRAY));
        lore.add(WeaponUtils.loreLine("", NamedTextColor.GRAY));

        if (tier >= 1) lore.add(WeaponUtils.loreLine("§a✔ Tier 1: §fWind Burst III", NamedTextColor.GRAY));
        else          lore.add(WeaponUtils.loreLine("§8✗ Tier 1: §8Wind Burst III (2 kills)", NamedTextColor.DARK_GRAY));

        if (tier >= 2) lore.add(WeaponUtils.loreLine("§a✔ Tier 2: §fBreach IV", NamedTextColor.GRAY));
        else          lore.add(WeaponUtils.loreLine("§8✗ Tier 2: §8Breach IV (4 kills)", NamedTextColor.DARK_GRAY));

        if (tier >= 3) lore.add(WeaponUtils.loreLine("§a✔ Tier 3: §fShockwave (Launches nearby foes)", NamedTextColor.GRAY));
        else          lore.add(WeaponUtils.loreLine("§8✗ Tier 3: §8Shockwave (6 kills)", NamedTextColor.DARK_GRAY));

        if (tier >= 4) lore.add(WeaponUtils.loreLine("§a✔ Tier 4: §fDensity + Breach", NamedTextColor.GRAY));
        else          lore.add(WeaponUtils.loreLine("§8✗ Tier 4: §8Density Enchant (8 kills)", NamedTextColor.DARK_GRAY));

        lore.add(WeaponUtils.loreLine("", NamedTextColor.GRAY));
        lore.add(WeaponUtils.loreLine("§8[Legendary] [KingsSMP]", NamedTextColor.DARK_GRAY));
        meta.lore(lore);

        // Base enchants
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);

        // Tier enchants
        if (tier >= 1) meta.addEnchant(Enchantment.WIND_BURST, 3, true);
        if (tier >= 2) meta.addEnchant(Enchantment.BREACH, 4, true);
        if (tier >= 4) meta.addEnchant(Enchantment.DENSITY, 5, true);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        WeaponUtils.applyCustomModelData(meta, 1005);

        meta.getPersistentDataContainer().set(WeaponUtils.WEAPON_ID_KEY, PersistentDataType.STRING, "judgement_gavel");
        meta.getPersistentDataContainer().set(WeaponUtils.GAVEL_KILLS_KEY, PersistentDataType.INTEGER, kills);
        meta.getPersistentDataContainer().set(WeaponUtils.GAVEL_TIER_KEY, PersistentDataType.INTEGER, tier);
        mace.setItemMeta(meta);
        return mace;
    }

    private String getTierSuffix(int tier) {
        return switch (tier) {
            case 0 -> "§7[Unawakened]";
            case 1 -> "§a[Tier I]";
            case 2 -> "§b[Tier II]";
            case 3 -> "§d[Tier III]";
            case 4 -> "§6[JUDGEMENT]";
            default -> "";
        };
    }

    @Override
    protected ShapedRecipe buildRecipe() {
        ItemStack result = buildItem();
        ShapedRecipe recipe = new ShapedRecipe(getKey(), result);
        recipe.shape("INI", "IMI", "INI");
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('M', Material.MACE);
        return recipe;
    }
}
