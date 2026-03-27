package com.kingssmp.managers;

import com.kingssmp.KingsSMP;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DragonBoneManager {

    private final KingsSMP plugin;
    // playerUUID -> summoned dragon
    private final Map<UUID, EnderDragon> summonedDragons = new HashMap<>();
    // playerUUID -> cooldown expiry ms
    private final Map<UUID, Long> summonCooldowns = new HashMap<>();

    public DragonBoneManager(KingsSMP plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(UUID playerId) {
        Long expiry = summonCooldowns.get(playerId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() < expiry) return true;
        summonCooldowns.remove(playerId);
        return false;
    }

    public long getCooldownRemaining(UUID playerId) {
        Long expiry = summonCooldowns.get(playerId);
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis()) / 1000);
    }

    public boolean hasActiveDragon(UUID playerId) {
        EnderDragon d = summonedDragons.get(playerId);
        return d != null && d.isValid() && !d.isDead();
    }

    public EnderDragon getActiveDragon(UUID playerId) {
        return summonedDragons.get(playerId);
    }

    public void summonDragon(Player player) {
        // Dismiss any existing dragon first
        dismissDragon(player.getUniqueId());

        // Spawn dragon directly on top of the player
        Location spawnLoc = player.getLocation().clone().add(0, 5, 0);
        EnderDragon dragon = (EnderDragon) player.getWorld().spawnEntity(spawnLoc, EntityType.ENDER_DRAGON);
        dragon.setPhase(EnderDragon.Phase.HOVER);
        dragon.customName(net.kyori.adventure.text.Component.text("✦ King's Dragon ✦")
                .color(net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE));
        dragon.setCustomNameVisible(true);
        dragon.setMetadata("kingssmp_dragon", new org.bukkit.metadata.FixedMetadataValue(plugin, player.getUniqueId().toString()));

        summonedDragons.put(player.getUniqueId(), dragon);
        int cooldownSec = plugin.getConfig().getInt("dragonbone-blade.dragon-summon-cooldown-seconds", 60);
        summonCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSec * 1000L));

        // Attempt to mount the player repeatedly over 3 seconds until it sticks
        // The dragon needs several ticks to fully initialize before passengers work
        final int[] attempts = {0};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            attempts[0]++;

            if (!player.isOnline() || !dragon.isValid()) {
                task.cancel();
                return;
            }

            // Teleport player to dragon's location each attempt so they don't fall
            player.teleport(dragon.getLocation().clone().add(0, 1, 0));

            // Try to add passenger
            dragon.addPassenger(player);

            // Check if mounting succeeded
            if (player.getVehicle() != null && player.getVehicle().equals(dragon)) {
                // Successfully mounted — stop retrying
                task.cancel();
                player.sendMessage(net.kyori.adventure.text.Component.text("✦ Your dragon awaits! (Right-click to launch Dragon's Breath)")
                        .color(net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE));
                return;
            }

            // Give up after 3 seconds (60 ticks / 5 tick interval = 12 attempts)
            if (attempts[0] >= 12) {
                task.cancel();
                // Final forced attempt
                dragon.addPassenger(player);
                player.sendMessage(net.kyori.adventure.text.Component.text("✦ Your dragon awaits! (Right-click to launch Dragon's Breath)")
                        .color(net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE));
            }
        }, 5L, 5L); // start after 5 ticks, retry every 5 ticks

        // Auto-dismiss after 5 minutes
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            dismissDragon(player.getUniqueId());
            if (player.isOnline()) {
                player.sendMessage(net.kyori.adventure.text.Component.text("✦ Your dragon has returned to the void.")
                        .color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
            }
        }, 6000L);
    }

    public void dismissDragon(UUID playerId) {
        EnderDragon dragon = summonedDragons.remove(playerId);
        if (dragon != null && dragon.isValid()) {
            // Eject passengers
            dragon.getPassengers().forEach(p -> dragon.removePassenger(p));
            dragon.remove();
        }
    }

    public void launchDragonBreath(Player player, EnderDragon dragon) {
        Location eyeLoc = player.getEyeLocation();
        org.bukkit.util.Vector dir = eyeLoc.getDirection().normalize().multiply(3);

        // Spawn areaeffect cloud (dragon's breath)
        AreaEffectCloud cloud = (AreaEffectCloud) player.getWorld().spawnEntity(
                eyeLoc.add(dir.multiply(5)), EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(3.5f);
        cloud.setDuration(100);
        cloud.setRadiusOnUse(-0.1f);
        cloud.setWaitTime(0);
        cloud.setColor(Color.PURPLE);
        cloud.addCustomEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.INSTANT_DAMAGE, 1, 2), true);
        cloud.addCustomEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SLOWNESS, 60, 1), true);

        // Particle effect
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH,
                eyeLoc.add(dir.multiply(2)), 60, 1.5, 1.5, 1.5, 0.05);
    }

    public UUID getDragonOwner(EnderDragon dragon) {
        if (!dragon.hasMetadata("kingssmp_dragon")) return null;
        try {
            return UUID.fromString(dragon.getMetadata("kingssmp_dragon").get(0).asString());
        } catch (Exception e) {
            return null;
        }
    }

    public void cleanup() {
        for (UUID id : new HashSet<>(summonedDragons.keySet())) {
            dismissDragon(id);
        }
    }
}
