package com.kingssmp.weapons.fabric;

import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class FabricLifestealer extends FabricLegendaryWeapon {

    public FabricLifestealer() {
        super("lifestealer", "Lifestealer");
    }

    public ItemStack buildStack() {
        return buildStackWithHearts(0);
    }

    public ItemStack buildStackWithHearts(int hearts) {
        ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Lifestealer").formatted(Formatting.DARK_RED, Formatting.BOLD));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§7Legendary Sword"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§c» §410% lifesteal on every hit"),
                FabricWeaponUtils.loreLine("§c» §4Kill player = +1 permanent heart (max 4)"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§c❤ Bonus Hearts: §4" + hearts + "/4"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        stack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1002));
        FabricWeaponUtils.setWeaponId(stack, "lifestealer");
        FabricWeaponUtils.setInt(stack, FabricWeaponUtils.LIFESTEALER_HEARTS_KEY, hearts);
        return stack;
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
