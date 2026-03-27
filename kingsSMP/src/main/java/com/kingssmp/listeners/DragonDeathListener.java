package com.kingssmp.listeners;

import com.kingssmp.KingsSMP;
import com.kingssmp.managers.WeaponRegistry;
import com.kingssmp.weapons.DragonBoneBlade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DragonDeathListener implements Listener {

    private final KingsSMP plugin;

    public DragonDeathListener(KingsSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        World world = dragon.getWorld();

        // Only trigger for the natural end world dragon (not our summoned ones)
        if (dragon.hasMetadata("kingssmp_dragon")) {
            // This is a player-summoned dragon — no drop
            return;
        }

        WeaponRegistry registry = plugin.getWeaponRegistry();

        // Check if Dragonbone Blade has already been obtained (treat as "crafted")
        if (registry.hasBeenCrafted("dragonbone_blade")) {
            // Already exists in the world — no duplicate
            return;
        }

        // Mark it as obtained so it can't be obtained again via dragon kill
        registry.markCrafted("dragonbone_blade");

        // Find the end portal location (center of the End island — 0, ~64, 0 area)
        Location dropLocation = findEndPortalLocation(world);

        DragonBoneBlade blade = (DragonBoneBlade) registry.getById("dragonbone_blade");
        if (blade == null) return;

        ItemStack bladeItem = blade.buildItem();
        world.dropItemNaturally(dropLocation, bladeItem);

        // Fancy announcement
        Bukkit.broadcast(
                Component.text("☠ THE ENDER DRAGON HAS FALLEN! ").color(NamedTextColor.DARK_PURPLE)
                        .append(Component.text("The ").color(NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("✦ Dragonbone Blade").color(NamedTextColor.GOLD))
                        .append(Component.text(" has materialized near the End Portal!").color(NamedTextColor.LIGHT_PURPLE))
        );
        Bukkit.broadcast(
                Component.text("   → Location: ").color(NamedTextColor.GRAY)
                        .append(Component.text(
                                "(" + dropLocation.getBlockX() + ", " + dropLocation.getBlockY() + ", " + dropLocation.getBlockZ() + ")"
                        ).color(NamedTextColor.YELLOW))
                        .append(Component.text(" in The End").color(NamedTextColor.GRAY))
        );

        // Particle burst at drop location
        world.spawnParticle(Particle.DRAGON_BREATH, dropLocation, 80, 2, 1, 2, 0.05);
        world.playSound(dropLocation, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1f);
    }

    /**
     * Returns the approximate location of the End exit portal (main platform).
     * The End main island portal is always near 0, 64, 0 in the End dimension.
     */
    private Location findEndPortalLocation(World world) {
        // Search for the exit portal blocks near the center
        for (int r = 0; r <= 10; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int y = 55; y <= 75; y++) {
                        Location check = new Location(world, dx, y, dz);
                        if (check.getBlock().getType() == Material.END_PORTAL
                                || check.getBlock().getType() == Material.BEDROCK) {
                            return check.clone().add(0, 2, 0);
                        }
                    }
                }
            }
        }
        // Fallback: default End portal area
        return new Location(world, 0, 65, 0);
    }
}
