package com.kingssmp.listeners;

import com.kingssmp.KingsSMP;
import com.kingssmp.utils.WeaponUtils;
import com.kingssmp.weapons.JudgementGavel;
import com.kingssmp.weapons.Lifestealer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PlayerDeathListener implements Listener {

    private final KingsSMP plugin;

    public PlayerDeathListener(KingsSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // --- DROP LEGENDARY ITEMS ON DEATH ---
        // Legendary items bypass keep-on-death — they always drop
        List<ItemStack> legendaryDrops = new ArrayList<>();
        for (ItemStack item : victim.getInventory().getContents()) {
            if (item != null && WeaponUtils.isLegendary(item)) {
                legendaryDrops.add(item.clone());
            }
        }
        // The default drop handling will include inventory items.
        // We ensure they're included in event drops explicitly.
        for (ItemStack legendary : legendaryDrops) {
            if (!event.getDrops().contains(legendary)) {
                event.getDrops().add(legendary);
            }
        }

        if (killer == null) return;

        ItemStack killerWeapon = killer.getInventory().getItemInMainHand();
        String weaponId = WeaponUtils.getWeaponId(killerWeapon);
        if (weaponId == null) return;

        switch (weaponId) {
            case "lifestealer" -> handleLifestealerKill(killer, killerWeapon);
            case "judgement_gavel" -> handleGavelKill(killer, killerWeapon);
        }
    }

    // ==================== LIFESTEALER ====================

    private void handleLifestealerKill(Player killer, ItemStack lifestealer) {
        var meta = lifestealer.getItemMeta();
        int currentHearts = meta.getPersistentDataContainer()
                .getOrDefault(WeaponUtils.LIFESTEALER_HEARTS_KEY, PersistentDataType.INTEGER, 0);

        int maxBonus = plugin.getConfig().getInt("lifestealer.max-bonus-hearts", 4);
        if (currentHearts >= maxBonus) {
            killer.sendMessage(Component.text("✦ Lifestealer is fully awakened. No more hearts to claim.")
                    .color(NamedTextColor.DARK_RED));
            return;
        }

        int newHearts = currentHearts + 1;

        // Update the item with new heart count
        Lifestealer lifestealerWeapon = (Lifestealer) plugin.getWeaponRegistry().getById("lifestealer");
        ItemStack updatedItem = lifestealerWeapon.buildItemWithHearts(newHearts);

        // Replace item in hand
        killer.getInventory().setItemInMainHand(updatedItem);

        // Grant permanent +2 max health (1 heart = 2 HP in Minecraft)
        var maxHealthAttr = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            NamespacedKey key = new NamespacedKey(plugin, "lifestealer_heart_" + newHearts);
            // Remove duplicate modifier if any
            maxHealthAttr.getModifiers().stream()
                    .filter(m -> m.key().equals(key))
                    .findFirst()
                    .ifPresent(maxHealthAttr::removeModifier);

            maxHealthAttr.addModifier(new AttributeModifier(
                    key,
                    2.0,
                    AttributeModifier.Operation.ADD_NUMBER
            ));

            // Heal up to fill new max
            killer.setHealth(Math.min(killer.getHealth() + 2, maxHealthAttr.getValue()));
        }

        killer.sendMessage(Component.text("✦ Lifestealer claims a heart! §c❤ " + newHearts + "/4 bonus hearts.")
                .color(NamedTextColor.DARK_RED));
        killer.getWorld().playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
    }

    // ==================== JUDGEMENT GAVEL ====================

    private void handleGavelKill(Player killer, ItemStack gavel) {
        var meta = gavel.getItemMeta();
        int kills = meta.getPersistentDataContainer()
                .getOrDefault(WeaponUtils.GAVEL_KILLS_KEY, PersistentDataType.INTEGER, 0);
        int currentTier = meta.getPersistentDataContainer()
                .getOrDefault(WeaponUtils.GAVEL_TIER_KEY, PersistentDataType.INTEGER, 0);

        kills++;
        int newTier = Math.min(kills / 2, 4); // 1 tier per 2 kills, max 4

        JudgementGavel gavelWeapon = (JudgementGavel) plugin.getWeaponRegistry().getById("judgement_gavel");
        ItemStack updatedGavel = gavelWeapon.buildItemWithTier(kills, newTier);
        killer.getInventory().setItemInMainHand(updatedGavel);

        killer.sendMessage(Component.text("✦ Judgement Gavel: §e" + kills + " kills")
                .color(NamedTextColor.GOLD));

        if (newTier > currentTier) {
            String tierMsg = switch (newTier) {
                case 1 -> "§aTier I Unlocked: §fWind Burst III!";
                case 2 -> "§bTier II Unlocked: §fBreach IV!";
                case 3 -> "§dTier III Unlocked: §fShockwave (launch nearby enemies)!";
                case 4 -> "§6FINAL TIER Unlocked: §fDensity + full Judgement power!";
                default -> "";
            };
            killer.sendMessage(Component.text("⚡ " + tierMsg).color(NamedTextColor.GOLD));
            killer.getWorld().playSound(killer.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

            // Broadcast the tier-up
            org.bukkit.Bukkit.broadcast(Component.text("⚡ ")
                    .color(NamedTextColor.GOLD)
                    .append(Component.text(killer.getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text("'s Judgement Gavel has reached ").color(NamedTextColor.GOLD))
                    .append(Component.text("Tier " + newTier + "!").color(NamedTextColor.LIGHT_PURPLE)));
        }
    }
}
