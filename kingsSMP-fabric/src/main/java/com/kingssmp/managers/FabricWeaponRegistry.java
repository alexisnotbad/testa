package com.kingssmp.managers;

import com.kingssmp.weapons.fabric.FabricLegendaryWeapon;

import java.util.*;

public class FabricWeaponRegistry {

    private final Map<String, FabricLegendaryWeapon> weapons = new LinkedHashMap<>();
    private final Set<String> craftedWeapons = new HashSet<>();
    private final Set<String> enabledRecipes = new HashSet<>();

    public void register(FabricLegendaryWeapon weapon) {
        weapons.put(weapon.getId(), weapon);
    }

    public FabricLegendaryWeapon getById(String id) {
        return weapons.get(id);
    }

    public Collection<FabricLegendaryWeapon> getAll() {
        return weapons.values();
    }

    public boolean hasBeenCrafted(String id) { return craftedWeapons.contains(id); }
    public void markCrafted(String id) { craftedWeapons.add(id); }

    public boolean isRecipeEnabled(String id) { return enabledRecipes.contains(id); }
    public void enableRecipe(String id) { enabledRecipes.add(id); }
    public void disableRecipe(String id) { enabledRecipes.remove(id); }
}
