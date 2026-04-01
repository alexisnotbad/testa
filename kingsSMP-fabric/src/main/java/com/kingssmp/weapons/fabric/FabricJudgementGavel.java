package com.kingssmp.weapons.fabric;

import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class FabricJudgementGavel extends FabricLegendaryWeapon {

    public FabricJudgementGavel() {
        super("judgement_gavel", "Judgement Gavel");
    }

    @Override
    public ItemStack buildStack() {
        return buildStackWithTier(0, 0);
    }

    public ItemStack buildStackWithTier(int kills, int tier) {
        ItemStack s = new ItemStack(Items.MACE);
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Judgement Gavel " + tierSuffix(tier)).formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(FabricWeaponUtils.loreLine("§6Legendary Mace"));
        lore.add(FabricWeaponUtils.loreLine(""));
        lore.add(FabricWeaponUtils.loreLine("§6» §eUpgrades every 2 kills (4 tiers)"));
        lore.add(FabricWeaponUtils.loreLine("§6Kills: §e" + kills + " §7| §6Tier: §e" + tier + "/4"));
        lore.add(FabricWeaponUtils.loreLine(""));
        lore.add(FabricWeaponUtils.loreLine(tier >= 1 ? "§a✔ Tier I:   Wind Burst III"   : "§8✗ Tier I:   Wind Burst III (2 kills)"));
        lore.add(FabricWeaponUtils.loreLine(tier >= 2 ? "§a✔ Tier II:  Breach IV"        : "§8✗ Tier II:  Breach IV (4 kills)"));
        lore.add(FabricWeaponUtils.loreLine(tier >= 3 ? "§a✔ Tier III: Shockwave"        : "§8✗ Tier III: Shockwave (6 kills)"));
        lore.add(FabricWeaponUtils.loreLine(tier >= 4 ? "§a✔ Tier IV:  Density + Breach" : "§8✗ Tier IV:  Density (8 kills)"));
        lore.add(FabricWeaponUtils.loreLine(""));
        lore.add(FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]"));
        s.set(DataComponentTypes.LORE, new LoreComponent(lore));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1005));
        FabricWeaponUtils.setWeaponId(s, "judgement_gavel");
        FabricWeaponUtils.setInt(s, FabricWeaponUtils.GAVEL_KILLS_KEY, kills);
        FabricWeaponUtils.setInt(s, FabricWeaponUtils.GAVEL_TIER_KEY, tier);
        return s;
    }

    private String tierSuffix(int t) {
        return switch (t) {
            case 1 -> "§a[Tier I]";
            case 2 -> "§b[Tier II]";
            case 3 -> "§d[Tier III]";
            case 4 -> "§6[JUDGEMENT]";
            default -> "§7[Unawakened]";
        };
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
