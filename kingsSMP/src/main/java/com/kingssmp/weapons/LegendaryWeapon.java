package com.kingssmp.weapons;

import com.kingssmp.KingsSMP;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public abstract class LegendaryWeapon {

    protected final KingsSMP plugin;
    protected final String id;
    protected final String displayName;

    public LegendaryWeapon(KingsSMP plugin, String id, String displayName) {
        this.plugin = plugin;
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * Build the ItemStack for this weapon.
     */
    public abstract ItemStack buildItem();

    /**
     * Register the crafting recipe (if applicable).
     * DragonBoneBlade overrides this to skip recipe registration.
     */
    public void registerRecipe() {
        ShapedRecipe recipe = buildRecipe();
        if (recipe != null) {
            plugin.getServer().addRecipe(recipe);
        }
    }

    protected abstract ShapedRecipe buildRecipe();

    public NamespacedKey getKey() {
        return new NamespacedKey(plugin, id);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
}
