package com.kingssmp.weapons.fabric;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Base class for all Fabric-side legendary weapons.
 */
public abstract class FabricLegendaryWeapon {

    protected final String id;
    protected final String displayName;

    public FabricLegendaryWeapon(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /** Build a fresh ItemStack of this weapon. */
    public abstract ItemStack buildStack();

    /** Return the crafting recipe, or null if not craftable (e.g. DragonBone). */
    public abstract net.minecraft.recipe.Recipe<?> buildRecipe();

    /** Identifier used for recipe registration. */
    public Identifier getRecipeId() {
        return Identifier.of("kingssmp", id + "_recipe");
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }

    /** Called when the player shift+right-clicks with this weapon. */
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {}

    /** Called when the player right-clicks (non-shift) while riding a vehicle, etc. */
    public void onRightClick(ServerPlayerEntity player, ItemStack stack) {}
}
