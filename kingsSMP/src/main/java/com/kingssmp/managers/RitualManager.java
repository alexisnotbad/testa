package com.kingssmp.managers;

import com.kingssmp.KingsSMP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RitualManager {

    private final KingsSMP plugin;

    // playerUUID -> completion task
    private final Map<UUID, BukkitTask> activeRituals = new HashMap<>();
    // playerUUID -> weapon id
    private final Map<UUID, String> ritualWeapon = new HashMap<>();
    // playerUUID -> floating item display entity
    private final Map<UUID, ItemDisplay> ritualDisplays = new HashMap<>();
    // playerUUID -> rotation animation task
    private final Map<UUID, BukkitTask> rotationTasks = new HashMap<>();

    public RitualManager(KingsSMP plugin) {
        this.plugin = plugin;
    }

    public boolean hasActiveRitual(UUID playerId) {
        return activeRituals.containsKey(playerId);
    }

    /**
     * Starts a ritual for the given player.
     *
     * @param player            The player performing the ritual
     * @param weaponId          The weapon's internal ID
     * @param weaponDisplayName The weapon's display name
     * @param weaponItem        The actual ItemStack to display above the crafting table
     * @param craftingTableLoc  Location of the crafting table used
     * @param onComplete        Callback run when the 10-minute ritual finishes
     */
    public void startRitual(Player player, String weaponId, String weaponDisplayName,
                            ItemStack weaponItem, Location craftingTableLoc, Runnable onComplete) {

        int durationSeconds = plugin.getConfig().getInt("ritual.duration-seconds", 600);

        // Broadcast start
        broadcastRitualStart(player, weaponDisplayName, craftingTableLoc);

        // Spawn the floating 3D item display above the crafting table
        spawnRitualDisplay(player, weaponItem, craftingTableLoc);

        // Periodic location broadcasts
        int broadcastInterval = plugin.getConfig().getInt("ritual.broadcast-interval-seconds", 60) * 20;
        BukkitTask broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            broadcastRitualProgress(player, weaponDisplayName, craftingTableLoc);
            // Pulse particles around the display each broadcast
            spawnRitualParticles(craftingTableLoc);
        }, broadcastInterval, broadcastInterval);

        // Completion task
        BukkitTask completionTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            broadcastTask.cancel();
            removeRitualDisplay(player.getUniqueId());
            activeRituals.remove(player.getUniqueId());
            ritualWeapon.remove(player.getUniqueId());

            // Completion fanfare at the crafting table
            craftingTableLoc.getWorld().playSound(craftingTableLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
            craftingTableLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, craftingTableLoc.clone().add(0.5, 2, 0.5), 80, 0.5, 1, 0.5, 0.3);
            craftingTableLoc.getWorld().spawnParticle(Particle.END_ROD, craftingTableLoc.clone().add(0.5, 2, 0.5), 40, 0.3, 1, 0.3, 0.1);

            onComplete.run();

            Bukkit.broadcast(Component.text("⚔ The ritual is complete! ")
                    .color(NamedTextColor.GOLD)
                    .append(Component.text(player.getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" has obtained ").color(NamedTextColor.GOLD))
                    .append(Component.text(weaponDisplayName).color(NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text("!").color(NamedTextColor.GOLD)));

            player.sendMessage(Component.text("✦ Your ritual is complete! The " + weaponDisplayName + " is yours.")
                    .color(NamedTextColor.LIGHT_PURPLE));

        }, (long) durationSeconds * 20L);

        activeRituals.put(player.getUniqueId(), completionTask);
        ritualWeapon.put(player.getUniqueId(), weaponId);

        player.sendMessage(Component.text("✦ Ritual begun! Wait 10 minutes for your " + weaponDisplayName + ".")
                .color(NamedTextColor.LIGHT_PURPLE));
    }

    // ─── ITEM DISPLAY ─────────────────────────────────────────────────────────

    private void spawnRitualDisplay(Player player, ItemStack weaponItem, Location tableLoc) {
        // Spawn slightly above the center of the crafting table
        Location displayLoc = tableLoc.clone().add(0.5, 1.6, 0.5);
        displayLoc.setYaw(0);
        displayLoc.setPitch(0);

        ItemDisplay display = (ItemDisplay) tableLoc.getWorld().spawnEntity(displayLoc, EntityType.ITEM_DISPLAY);
        display.setItemStack(weaponItem);
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
        display.setBillboard(Display.Billboard.VERTICAL);

        // Initial scale — make it bigger than a dropped item
        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),          // translation
                new AxisAngle4f(0, 0, 1, 0),    // left rotation
                new Vector3f(1.4f, 1.4f, 1.4f), // scale
                new AxisAngle4f(0, 0, 1, 0)     // right rotation
        ));
        display.setGlowing(true);

        ritualDisplays.put(player.getUniqueId(), display);

        // Spawn ambient particles immediately
        spawnRitualParticles(tableLoc);

        // Rotate the display every tick
        final float[] yaw = {0f};
        BukkitTask rotTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!display.isValid()) return;

            yaw[0] += 2f; // degrees per tick
            if (yaw[0] >= 360f) yaw[0] = 0f;

            float radians = (float) Math.toRadians(yaw[0]);
            display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(radians, 0, 1, 0), // rotate around Y axis
                    new Vector3f(1.4f, 1.4f, 1.4f),
                    new AxisAngle4f(0, 0, 1, 0)
            ));

            // Ambient particles every 20 ticks
            if (((int) yaw[0]) % 40 == 0) {
                tableLoc.getWorld().spawnParticle(Particle.END_ROD,
                        display.getLocation(), 4, 0.2, 0.3, 0.2, 0.02);
            }

        }, 1L, 1L);

        rotationTasks.put(player.getUniqueId(), rotTask);

        // Play ritual start sound at the table
        tableLoc.getWorld().playSound(tableLoc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.6f);
        tableLoc.getWorld().playSound(tableLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.4f, 1.5f);
    }

    private void spawnRitualParticles(Location tableLoc) {
        Location center = tableLoc.clone().add(0.5, 2.2, 0.5);
        tableLoc.getWorld().spawnParticle(Particle.PORTAL, center, 25, 0.3, 0.5, 0.3, 0.5);
        tableLoc.getWorld().spawnParticle(Particle.WITCH, center, 8, 0.4, 0.6, 0.4, 0.1);
    }

    private void removeRitualDisplay(UUID playerId) {
        // Cancel rotation task
        BukkitTask rotTask = rotationTasks.remove(playerId);
        if (rotTask != null) rotTask.cancel();

        // Remove the display entity
        ItemDisplay display = ritualDisplays.remove(playerId);
        if (display != null && display.isValid()) {
            // Burst particles on removal
            display.getWorld().spawnParticle(Particle.FLASH, display.getLocation(), 1, 0, 0, 0, 0);
            display.remove();
        }
    }

    // ─── BROADCASTS ───────────────────────────────────────────────────────────

    private void broadcastRitualStart(Player player, String weaponName, Location loc) {
        String coords = "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
        Bukkit.broadcast(Component.text("⚠ RITUAL STARTED! ").color(NamedTextColor.RED)
                .append(Component.text(player.getName()).color(NamedTextColor.YELLOW))
                .append(Component.text(" is performing a ritual for the ").color(NamedTextColor.RED))
                .append(Component.text(weaponName).color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" at " + coords + " in " + formatWorld(loc.getWorld().getName()) + "!").color(NamedTextColor.RED)));
    }

    private void broadcastRitualProgress(Player player, String weaponName, Location loc) {
        String coords = "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
        Bukkit.broadcast(Component.text("⚠ ONGOING RITUAL: ").color(NamedTextColor.YELLOW)
                .append(Component.text(player.getName()).color(NamedTextColor.WHITE))
                .append(Component.text(" is still performing the ").color(NamedTextColor.YELLOW))
                .append(Component.text(weaponName).color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" ritual at " + coords).color(NamedTextColor.YELLOW)));
    }

    private String formatWorld(String worldName) {
        return switch (worldName) {
            case "world" -> "Overworld";
            case "world_nether" -> "Nether";
            case "world_the_end" -> "The End";
            default -> worldName;
        };
    }

    // ─── LIFECYCLE ────────────────────────────────────────────────────────────

    public void cancelAll() {
        for (UUID id : activeRituals.keySet()) {
            removeRitualDisplay(id);
        }
        for (BukkitTask task : activeRituals.values()) task.cancel();
        activeRituals.clear();
        ritualWeapon.clear();
    }

    public void cancelRitual(UUID playerId) {
        removeRitualDisplay(playerId);
        BukkitTask task = activeRituals.remove(playerId);
        if (task != null) task.cancel();
        ritualWeapon.remove(playerId);
    }
}
