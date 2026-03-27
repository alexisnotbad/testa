package com.kingssmp.listeners;

import com.kingssmp.KingsSMP;
import com.kingssmp.utils.WeaponUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class StorageBlockListener implements Listener {

    private final KingsSMP plugin;

    private static final Set<InventoryType> BLOCKED_STORAGE = Set.of(
            InventoryType.CHEST,
            InventoryType.BARREL,
            InventoryType.SHULKER_BOX,
            InventoryType.ENDER_CHEST,
            InventoryType.HOPPER,
            InventoryType.DROPPER,
            InventoryType.DISPENSER
    );

    public StorageBlockListener(KingsSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfig().getBoolean("weapons.no-storage", true)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryType topType = event.getView().getTopInventory().getType();

        // Check if the destination inventory is a blocked storage type
        if (!BLOCKED_STORAGE.contains(topType)) return;

        // Check cursor item (being moved in)
        if (isLegendaryItem(event.getCursor())) {
            event.setCancelled(true);
            player.sendMessage("§c✦ Legendary weapons cannot be stored in containers.");
            return;
        }

        // Check shift-click from player inventory into storage
        if (event.isShiftClick() && event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (isLegendaryItem(event.getCurrentItem())) {
                event.setCancelled(true);
                player.sendMessage("§c✦ Legendary weapons cannot be stored in containers.");
            }
        }

        // Check clicking inside the storage — placing item there
        if (event.getClickedInventory() != null
                && BLOCKED_STORAGE.contains(event.getClickedInventory().getType())) {
            if (isLegendaryItem(event.getCursor())) {
                event.setCancelled(true);
                player.sendMessage("§c✦ Legendary weapons cannot be stored in containers.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!plugin.getConfig().getBoolean("weapons.no-storage", true)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryType topType = event.getView().getTopInventory().getType();
        if (!BLOCKED_STORAGE.contains(topType)) return;

        if (isLegendaryItem(event.getOldCursor())) {
            // Check if any slot dragged to is in the top inventory
            int topSize = event.getView().getTopInventory().getSize();
            boolean draggingIntoStorage = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);
            if (draggingIntoStorage) {
                event.setCancelled(true);
                player.sendMessage("§c✦ Legendary weapons cannot be stored in containers.");
            }
        }
    }

    private boolean isLegendaryItem(ItemStack item) {
        return item != null && WeaponUtils.isLegendary(item);
    }
}
