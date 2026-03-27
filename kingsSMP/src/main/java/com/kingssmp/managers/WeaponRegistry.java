package com.kingssmp.managers;

import com.kingssmp.KingsSMP;
import com.kingssmp.weapons.LegendaryWeapon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WeaponRegistry {

    private final KingsSMP plugin;
    private final Map<String, LegendaryWeapon> weapons = new LinkedHashMap<>();
    private final Set<String> craftedWeapons = new HashSet<>();

    // Weapons whose recipes are currently active in the server
    private final Set<String> enabledRecipes = new HashSet<>();

    private File dataFile;
    private YamlConfiguration dataConfig;

    public WeaponRegistry(KingsSMP plugin) {
        this.plugin = plugin;
        loadData();
    }

    // ─── PERSISTENCE ─────────────────────────────────────────────────────────

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "crafted_weapons.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        craftedWeapons.addAll(dataConfig.getStringList("crafted"));
        enabledRecipes.addAll(dataConfig.getStringList("enabled_recipes"));
    }

    public void saveData() {
        dataConfig.set("crafted", new ArrayList<>(craftedWeapons));
        dataConfig.set("enabled_recipes", new ArrayList<>(enabledRecipes));
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── REGISTRATION ─────────────────────────────────────────────────────────

    /**
     * Registers a weapon. Its recipe is only added if it was previously enabled
     * (persisted), or if it has never been seen before AND the default in config
     * is enabled (defaults to false — recipes start locked).
     */
    public void register(LegendaryWeapon weapon) {
        weapons.put(weapon.getId(), weapon);

        boolean defaultEnabled = plugin.getConfig()
                .getBoolean("recipes.default-enabled", false);

        // First time seeing this weapon — apply the default
        if (!dataConfig.contains("enabled_recipes_seen." + weapon.getId())) {
            dataConfig.set("enabled_recipes_seen." + weapon.getId(), true);
            if (defaultEnabled) enabledRecipes.add(weapon.getId());
            saveData();
        }

        if (enabledRecipes.contains(weapon.getId())) {
            weapon.registerRecipe();
        }
        // else: recipe stays locked until /kingssmp recipe enable <weapon>
    }

    // ─── RECIPE TOGGLE ───────────────────────────────────────────────────────

    /**
     * Enables a weapon's crafting recipe at runtime and persists the state.
     * Returns false if the weapon doesn't exist or has no recipe.
     */
    public boolean enableRecipe(String weaponId) {
        LegendaryWeapon weapon = weapons.get(weaponId);
        if (weapon == null) return false;

        // Remove old recipe first to avoid duplicates, then re-add
        Bukkit.removeRecipe(weapon.getKey());
        weapon.registerRecipe();

        enabledRecipes.add(weaponId);
        saveData();
        return true;
    }

    /**
     * Disables a weapon's crafting recipe at runtime and persists the state.
     * Returns false if the weapon doesn't exist.
     */
    public boolean disableRecipe(String weaponId) {
        LegendaryWeapon weapon = weapons.get(weaponId);
        if (weapon == null) return false;

        Bukkit.removeRecipe(weapon.getKey());
        enabledRecipes.remove(weaponId);
        saveData();
        return true;
    }

    public boolean isRecipeEnabled(String weaponId) {
        return enabledRecipes.contains(weaponId);
    }

    // ─── LOOKUP ──────────────────────────────────────────────────────────────

    public LegendaryWeapon getById(String id) {
        return weapons.get(id);
    }

    public LegendaryWeapon getByName(String name) {
        return weapons.values().stream()
                .filter(w -> w.getId().equalsIgnoreCase(name)
                        || w.getDisplayName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public Collection<LegendaryWeapon> getAll() {
        return weapons.values();
    }

    // ─── CRAFTED TRACKING ────────────────────────────────────────────────────

    public boolean hasBeenCrafted(String weaponId) {
        return craftedWeapons.contains(weaponId);
    }

    public void markCrafted(String weaponId) {
        craftedWeapons.add(weaponId);
        saveData();
    }
}
