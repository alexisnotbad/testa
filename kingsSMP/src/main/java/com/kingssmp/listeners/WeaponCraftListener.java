package com.kingssmp.listeners;

import com.kingssmp.KingsSMP;
import com.kingssmp.managers.RitualManager;
import com.kingssmp.managers.WeaponRegistry;
import com.kingssmp.utils.WeaponUtils;
import com.kingssmp.weapons.LegendaryWeapon;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class WeaponCraftListener implements Listener {

    private final KingsSMP plugin;

    public WeaponCraftListener(KingsSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getRecipe().getResult();
        String weaponId = WeaponUtils.getWeaponId(result);
        if (weaponId == null) return;

        WeaponRegistry registry = plugin.getWeaponRegistry();

        // Block if already crafted
        if (registry.hasBeenCrafted(weaponId)) {
            event.setCancelled(true);
            player.sendMessage("§c✦ This legendary weapon has already been forged.");
            player.sendMessage("§7The only way to obtain it is to defeat its current wielder.");
            return;
        }

        RitualManager ritualManager = plugin.getRitualManager();

        if (ritualManager.hasActiveRitual(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§c✦ You already have an active ritual in progress.");
            return;
        }

        event.setCancelled(true);

        // Consume ingredients
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && ingredient.getType().isItem()) {
                ingredient.setAmount(ingredient.getAmount() - 1);
            }
        }

        LegendaryWeapon weapon = registry.getById(weaponId);
        String displayName = weapon != null ? weapon.getDisplayName() : weaponId;

        // Get crafting table location — use the inventory's location if it's a block inventory,
        // otherwise fall back to the player's feet location
        Location craftingTableLoc = getCraftingTableLocation(event, player);

        // Mark as crafted immediately to block duplicates during the ritual window
        registry.markCrafted(weaponId);

        // Build the display item (fresh copy with full meta)
        ItemStack displayItem = weapon != null ? weapon.buildItem() : result.clone();

        // Start the ritual with the crafting table location and display item
        ritualManager.startRitual(player, weaponId, displayName, displayItem, craftingTableLoc, () -> {
            if (!player.isOnline()) {
                Location dropLoc = player.getRespawnLocation() != null
                        ? player.getRespawnLocation()
                        : player.getWorld().getSpawnLocation();
                player.getWorld().dropItem(dropLoc, weapon.buildItem());
                return;
            }
            player.getInventory().addItem(weapon.buildItem());
        });
    }

    /**
     * Resolves the location of the crafting table used.
     * CraftItemEvent doesn't directly expose the block, but the inventory's
     * location gives us the crafting table's block position when it's a tile entity.
     * For portable crafting (2x2 in player inventory) we fall back to the player.
     */
    private Location getCraftingTableLocation(CraftItemEvent event, Player player) {
        if (event.getInventory().getType() == InventoryType.WORKBENCH) {
            Location invLoc = event.getInventory().getLocation();
            if (invLoc != null) {
                return invLoc;
            }
        }
        // Fallback: player's current location (e.g. 2x2 crafting grid)
        return player.getLocation();
    }
}
