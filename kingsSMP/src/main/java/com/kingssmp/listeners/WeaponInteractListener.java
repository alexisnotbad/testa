package com.kingssmp.listeners;

import com.kingssmp.KingsSMP;
import com.kingssmp.managers.DragonBoneManager;
import com.kingssmp.utils.WeaponUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class WeaponInteractListener implements Listener {

    private final KingsSMP plugin;
    // Cooldown maps (player UUID -> expiry ms)
    private final Map<UUID, Long> ghostbladeCooldowns = new HashMap<>();
    private final Map<UUID, Long> kittyBladeCooldowns = new HashMap<>();
    // Active ghost players (UUID -> expiry tick)
    private final Map<UUID, Long> activeGhosts = new HashMap<>();
    // Void bow: track last hit entity (UUID -> entity UUID)
    private final Map<UUID, UUID> voidBowHitEntity = new HashMap<>();
    private final Map<UUID, Long> voidBowHitExpiry = new HashMap<>();

    public WeaponInteractListener(KingsSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        String weaponId = WeaponUtils.getWeaponId(held);
        if (weaponId == null) return;

        boolean shiftRight = player.isSneaking()
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);
        boolean rightClick = !player.isSneaking()
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);

        switch (weaponId) {
            case "void_bow" -> {
                if (shiftRight) {
                    event.setCancelled(true);
                    handleVoidBowTeleport(player, held);
                }
            }
            case "ghost_blade" -> {
                if (shiftRight) {
                    event.setCancelled(true);
                    activateGhostBlade(player);
                }
            }
            case "dragonbone_blade" -> {
                if (shiftRight) {
                    event.setCancelled(true);
                    handleDragonSummon(player);
                } else if (rightClick) {
                    event.setCancelled(true);
                    handleDragonBreath(player);
                }
            }
            case "pretty_kitty_blade" -> {
                if (shiftRight) {
                    event.setCancelled(true);
                    summonCatHorde(player);
                }
            }
        }
    }

    // ==================== VOID BOW ====================

    public void recordVoidBowHit(Player shooter, Entity target) {
        voidBowHitEntity.put(shooter.getUniqueId(), target.getUniqueId());
        voidBowHitExpiry.put(shooter.getUniqueId(), System.currentTimeMillis() + 10000L); // 10s window
    }

    private void handleVoidBowTeleport(Player player, ItemStack bow) {
        UUID pid = player.getUniqueId();
        Long entityExpiry = voidBowHitExpiry.get(pid);

        if (entityExpiry != null && System.currentTimeMillis() < entityExpiry) {
            // Teleport hit entity to stored arrow location
            UUID targetId = voidBowHitEntity.get(pid);
            Entity target = findEntity(player.getWorld(), targetId);
            if (target != null) {
                // Find the last void arrow landing spot
                Location arrowLoc = getStoredArrowLocation(player);
                if (arrowLoc != null) {
                    target.teleport(arrowLoc);
                    player.sendMessage(Component.text("✦ Entity teleported to the arrow's location!").color(NamedTextColor.DARK_PURPLE));
                } else {
                    player.sendMessage(Component.text("✦ No recent arrow location found.").color(NamedTextColor.RED));
                }
            }
            voidBowHitEntity.remove(pid);
            voidBowHitExpiry.remove(pid);
        } else {
            // Teleport self to last arrow location
            Location arrowLoc = getStoredArrowLocation(player);
            if (arrowLoc != null) {
                player.teleport(arrowLoc);
                player.sendMessage(Component.text("✦ Teleported to arrow location!").color(NamedTextColor.DARK_PURPLE));
            } else {
                player.sendMessage(Component.text("✦ No recent arrow location to teleport to.").color(NamedTextColor.RED));
            }
        }
    }

    private Location getStoredArrowLocation(Player player) {
        if (!player.hasMetadata("void_bow_last_arrow")) return null;
        Object val = player.getMetadata("void_bow_last_arrow").get(0).value();
        if (val instanceof Location loc) return loc;
        return null;
    }

    private Entity findEntity(World world, UUID id) {
        for (Entity e : world.getEntities()) {
            if (e.getUniqueId().equals(id)) return e;
        }
        return null;
    }

    // ==================== GHOST BLADE ====================

    private void activateGhostBlade(Player player) {
        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long cooldown = ghostbladeCooldowns.get(pid);
        if (cooldown != null && now < cooldown) {
            long remaining = (cooldown - now) / 1000;
            player.sendMessage(Component.text("✦ Ghostblade on cooldown: " + remaining + "s").color(NamedTextColor.RED));
            return;
        }

        int durationSec = plugin.getConfig().getInt("ghostblade.invisibility-duration-seconds", 300);

        // Invisibility potion effect — silent, no particles
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationSec * 20, 0, false, false));

        // Hide the player entity entirely from all other online players
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.getUniqueId().equals(pid)) {
                other.hidePlayer(plugin, player);
            }
        }

        // Hide armor and held item by sending empty equipment to all other players
        hidePlayerEquipment(player);

        activeGhosts.put(pid, System.currentTimeMillis() + (durationSec * 1000L));
        ghostbladeCooldowns.put(pid, System.currentTimeMillis() + (durationSec * 1000L) + 5000L);

        player.sendMessage(Component.text("✦ You fade into the void... (5 minutes)").color(NamedTextColor.WHITE));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);

        // Schedule auto-reveal after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> revealGhostPlayer(player), (long) durationSec * 20L);
    }

    /**
     * Sends empty equipment slots to all other online players so they cannot
     * see the ghost player's armor or held item even if the entity becomes
     * partially visible (e.g. through mods or edge cases).
     */
    private void hidePlayerEquipment(Player player) {
        // Build a fake equipment packet with all slots empty using Paper's API
        java.util.List<com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType> slots =
                java.util.Arrays.asList(com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType.values());

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(player.getUniqueId())) continue;
            // Paper exposes sendEquipmentChange to send fake equipment to a specific viewer
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.HEAD,   new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.CHEST,  new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.LEGS,   new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.FEET,   new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.HAND,   new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.OFF_HAND, new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
        }
    }

    /**
     * Restores the real equipment visually for all players after ghost ends.
     */
    private void restorePlayerEquipment(Player player) {
        org.bukkit.inventory.EntityEquipment eq = player.getEquipment();
        if (eq == null) return;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(player.getUniqueId())) continue;
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.HEAD,    eq.getHelmet()     != null ? eq.getHelmet()     : new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.CHEST,   eq.getChestplate() != null ? eq.getChestplate() : new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.LEGS,    eq.getLeggings()   != null ? eq.getLeggings()   : new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.FEET,    eq.getBoots()      != null ? eq.getBoots()      : new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.HAND,    eq.getItemInMainHand());
            other.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.OFF_HAND, eq.getItemInOffHand());
        }
    }

    public void revealGhostPlayer(Player player) {
        UUID pid = player.getUniqueId();
        if (!activeGhosts.containsKey(pid)) return;
        activeGhosts.remove(pid);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(plugin, player);
        }
        // Restore real armor/item visibility
        restorePlayerEquipment(player);
        if (player.isOnline()) {
            player.sendMessage(Component.text("✦ You have been revealed!").color(NamedTextColor.RED));
        }
    }

    public boolean isGhostActive(UUID playerId) {
        Long expiry = activeGhosts.get(playerId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) { activeGhosts.remove(playerId); return false; }
        return true;
    }

    // ==================== DRAGONBONE BLADE ====================

    private void handleDragonSummon(Player player) {
        DragonBoneManager dbm = plugin.getDragonBoneManager();
        if (dbm.isOnCooldown(player.getUniqueId())) {
            long remaining = dbm.getCooldownRemaining(player.getUniqueId());
            player.sendMessage(Component.text("✦ Dragon cooldown: " + remaining + "s").color(NamedTextColor.RED));
            return;
        }
        if (dbm.hasActiveDragon(player.getUniqueId())) {
            dbm.dismissDragon(player.getUniqueId());
            player.sendMessage(Component.text("✦ Your dragon has been dismissed.").color(NamedTextColor.DARK_PURPLE));
            return;
        }
        player.sendMessage(Component.text("✦ Summoning your dragon...").color(NamedTextColor.DARK_PURPLE));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.8f);
        dbm.summonDragon(player);
    }

    private void handleDragonBreath(Player player) {
        DragonBoneManager dbm = plugin.getDragonBoneManager();
        if (!dbm.hasActiveDragon(player.getUniqueId())) return;
        // Check if player is riding the dragon
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof EnderDragon dragon) {
            dbm.launchDragonBreath(player, dragon);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1f, 0.9f);
        } else {
            player.sendMessage(Component.text("✦ You must be riding your dragon to use Dragon's Breath!").color(NamedTextColor.RED));
        }
    }

    // ==================== PRETTY KITTY BLADE ====================

    private void summonCatHorde(Player player) {
        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long cooldown = kittyBladeCooldowns.get(pid);
        if (cooldown != null && now < cooldown) {
            long remaining = (cooldown - now) / 1000;
            player.sendMessage(Component.text("✦ Kitty Horde on cooldown: " + remaining + "s").color(NamedTextColor.RED));
            return;
        }

        int catCount = plugin.getConfig().getInt("pretty-kitty.cat-horde-count", 6);
        Location loc = player.getLocation();
        Random rand = new Random();

        for (int i = 0; i < catCount; i++) {
            Location spawnLoc = loc.clone().add(
                    (rand.nextDouble() - 0.5) * 4,
                    0.5,
                    (rand.nextDouble() - 0.5) * 4
            );
            Cat cat = (Cat) player.getWorld().spawnEntity(spawnLoc, EntityType.CAT);
            cat.setTamed(true);
            cat.setOwner(player);
            cat.customName(Component.text("✦ Kitty").color(NamedTextColor.LIGHT_PURPLE));
            cat.setCustomNameVisible(true);
            cat.setAgeLock(true);
            cat.setTarget(null);

            // Auto-despawn cats after 60 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (cat.isValid()) cat.remove();
            }, 1200L);
        }

        kittyBladeCooldowns.put(pid, System.currentTimeMillis() + 30000L); // 30s cooldown
        player.sendMessage(Component.text("✦ Your kitty horde has arrived! Meow.").color(NamedTextColor.LIGHT_PURPLE));
        player.getWorld().playSound(loc, Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
    }
}
