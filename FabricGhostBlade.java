package com.kingssmp.weapons.fabric;

import com.kingssmp.listeners.FabricWeaponEventHandler;
import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class FabricGhostBlade extends FabricLegendaryWeapon {

    public FabricGhostBlade() {
        super("ghost_blade", "Ghostblade");
    }

    @Override
    public ItemStack buildStack() {
        ItemStack s = new ItemStack(Items.DIAMOND_SWORD);
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Ghostblade").formatted(Formatting.WHITE, Formatting.BOLD));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§7Legendary Sword"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§f» §7Shift+RClick for true invisibility (5 min)"),
                FabricWeaponUtils.loreLine("  §7Armor is hidden from other players."),
                FabricWeaponUtils.loreLine("§f» §7Hitting anything breaks invisibility"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1003));
        FabricWeaponUtils.setWeaponId(s, "ghost_blade");
        return s;
    }

    @Override
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {
        if (FabricWeaponEventHandler.isGhost(player.getUuid())) {
            FabricWeaponEventHandler.revealGhostPlayer(player);
        } else {
            FabricWeaponEventHandler.activateGhostBlade(player);
        }
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
