package com.kingssmp.listeners;

import com.kingssmp.KingsSMP;
import com.kingssmp.utils.WeaponUtils;
import com.kingssmp.weapons.JudgementGavel;
import com.kingssmp.weapons.Lifestealer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class WeaponCombatListener implements Listener {

    private final KingsSMP plugin;
    private WeaponInteractListener interactListener;

    // Gavel shockwave cooldown
    private final Map<UUID, Long> gavelShockwaveCooldown = new HashMap<>();

    public WeaponCombatListener(KingsSMP plugin) {
        this.plugin = plugin;
    }

    public void setInteractListener(WeaponInteractListener il) {
        this.interactListener = il;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Resolve actual attacker (could be a projectile)
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) return;

        ItemStack held = attacker.getInventory().getItemInMainHand();
        String weaponId = WeaponUtils.getWeaponId(held);
        if (weaponId == null) return;

        Entity victim = event.getEntity();
        double baseDamage = event.getDamage();

        switch (weaponId) {
            case "lifestealer" -> handleLifestealerHit(event, attacker, victim, baseDamage);
            case "judgement_gavel" -> handleGavelHit(event, attacker, held, victim, baseDamage);
            case "pretty_kitty_blade" -> handleKittyBackstab(event, attacker, victim, baseDamage);
            case "ghost_blade" -> {
                // Reveal on hitting ANY entity — mobs, players, wardens, etc.
                if (interactListener != null && interactListener.isGhostActive(attacker.getUniqueId())) {
                    interactListener.revealGhostPlayer(attacker);
                    attacker.sendMessage(Component.text("✦ Your strike broke your invisibility!").color(NamedTextColor.RED));
                    attacker.getWorld().playSound(attacker.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
                }
            }
        }

        // Void bow: record last hit entity for teleport targeting
        if (weaponId.equals("void_bow") && victim != null && interactListener != null) {
            if (event.getDamager() instanceof Arrow) {
                interactListener.recordVoidBowHit(attacker, victim);
            }
        }
    }

    // ==================== LIFESTEALER ====================

    private void handleLifestealerHit(EntityDamageByEntityEvent event, Player attacker, Entity victim, double baseDamage) {
        if (!(victim instanceof LivingEntity livingVictim)) return;

        // 10% lifesteal
        double stealAmount = livingVictim.getHealth() * 0.10;
        var maxHealthAttr = attacker.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        double maxHp = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
        double healAmount = Math.min(stealAmount, maxHp - attacker.getHealth());
        if (healAmount > 0) {
            attacker.setHealth(attacker.getHealth() + healAmount);
            attacker.spawnParticle(Particle.HEART, attacker.getLocation().add(0, 2, 0), 3, 0.3, 0.3, 0.3, 0);
        }
    }

    // ==================== JUDGEMENT GAVEL ====================

    private void handleGavelHit(EntityDamageByEntityEvent event, Player attacker, ItemStack gavelItem, Entity victim, double baseDamage) {
        int tier = gavelItem.getItemMeta().getPersistentDataContainer()
                .getOrDefault(WeaponUtils.GAVEL_TIER_KEY, PersistentDataType.INTEGER, 0);

        // Tier 3: Shockwave — launch nearby enemies upward
        if (tier >= 3) {
            UUID pid = attacker.getUniqueId();
            long now = System.currentTimeMillis();
            Long cooldown = gavelShockwaveCooldown.get(pid);
            if (cooldown == null || now > cooldown) {
                gavelShockwaveCooldown.put(pid, now + 8000L); // 8s cooldown
                triggerShockwave(attacker);
            }
        }
    }

    private void triggerShockwave(Player attacker) {
        Location loc = attacker.getLocation();
        double radius = 6.0;

        for (Entity nearby : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (nearby.equals(attacker)) continue;
            if (!(nearby instanceof LivingEntity)) continue;

            Vector dir = nearby.getLocation().toVector().subtract(loc.toVector()).normalize();
            dir.setY(1.2).normalize().multiply(2.5);
            nearby.setVelocity(dir);

            if (nearby instanceof Player p) {
                p.sendMessage(Component.text("✦ The Judgement Gavel's shockwave launches you!").color(NamedTextColor.GOLD));
            }
        }

        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5, 2, 0.5, 2, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
        attacker.sendMessage(Component.text("✦ SHOCKWAVE!").color(NamedTextColor.GOLD));
    }

    // ==================== PRETTY KITTY BACKSTAB ====================

    private void handleKittyBackstab(EntityDamageByEntityEvent event, Player attacker, Entity victim, double baseDamage) {
        if (!(victim instanceof LivingEntity)) return;

        // Check if attacker is behind victim
        if (isAttackingFromBehind(attacker, victim)) {
            double multiplier = plugin.getConfig().getDouble("pretty-kitty.backstab-multiplier", 2.0);
            event.setDamage(baseDamage * multiplier);
            attacker.sendMessage(Component.text("✦ Backstab! §5" + (int)(multiplier * 100) + "% damage!").color(NamedTextColor.LIGHT_PURPLE));
            attacker.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.1);
        }
    }

    /**
     * Returns true if the attacker is behind the victim (within ~120° behind facing direction).
     */
    private boolean isAttackingFromBehind(Player attacker, Entity victim) {
        Vector victimFacing = victim.getLocation().getDirection().normalize();
        Vector toAttacker = attacker.getLocation().toVector()
                .subtract(victim.getLocation().toVector()).normalize();
        // Dot product: if negative, attacker is behind victim
        double dot = victimFacing.dot(toAttacker);
        return dot < -0.3; // behind if facing away from attacker
    }

    // ==================== VOID BOW ====================

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack bow = event.getBow();
        if (bow == null || !"void_bow".equals(WeaponUtils.getWeaponId(bow))) return;

        if (event.getProjectile() instanceof Arrow arrow) {
            arrow.setMetadata("void_bow_arrow", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        }
    }

    @EventHandler
    public void onArrowHitGround(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!arrow.hasMetadata("void_bow_arrow")) return;

        String ownerIdStr = arrow.getMetadata("void_bow_arrow").get(0).asString();
        UUID ownerId;
        try { ownerId = UUID.fromString(ownerIdStr); } catch (Exception e) { return; }

        Player player = Bukkit.getPlayer(ownerId);
        if (player == null) return;

        Location hitLoc = arrow.getLocation().clone();

        // Store last arrow location on player metadata
        player.setMetadata("void_bow_last_arrow", new FixedMetadataValue(plugin, hitLoc));

        // Particle effect at landing spot
        hitLoc.getWorld().spawnParticle(Particle.PORTAL, hitLoc, 30, 0.3, 0.5, 0.3, 0.1);
        hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
    }

    // ==================== HELPERS ====================

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }
}
