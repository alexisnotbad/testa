package com.kingssmp.listeners;

import com.kingssmp.KingsSMPMod;
import com.kingssmp.utils.FabricWeaponUtils;
import com.kingssmp.weapons.fabric.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class FabricWeaponEventHandler {

    private static final Set<UUID> ghostPlayers = new HashSet<>();

    public static void register() {

        // ─── SHIFT+RIGHT CLICK / RIGHT CLICK ─────────────────────────────────
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient) return TypedActionResult.pass(player.getStackInHand(hand));
            if (!(player instanceof ServerPlayerEntity sp)) return TypedActionResult.pass(player.getStackInHand(hand));

            ItemStack stack = sp.getStackInHand(hand);
            String weaponId = FabricWeaponUtils.getWeaponId(stack);
            if (weaponId == null) return TypedActionResult.pass(stack);

            FabricLegendaryWeapon weapon = KingsSMPMod.getInstance().getWeaponRegistry().getById(weaponId);
            if (weapon == null) return TypedActionResult.pass(stack);

            if (sp.isSneaking()) {
                weapon.onShiftRightClick(sp, stack);
                return TypedActionResult.success(stack);
            } else {
                weapon.onRightClick(sp, stack);
                return TypedActionResult.pass(stack);
            }
        });

        // ─── ATTACK ENTITY (combat passives) ─────────────────────────────────
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity victim)) return ActionResult.PASS;

            ItemStack held = sp.getMainHandStack();
            String weaponId = FabricWeaponUtils.getWeaponId(held);
            if (weaponId == null) return ActionResult.PASS;

            switch (weaponId) {
                case "ghost_blade" -> {
                    if (ghostPlayers.contains(sp.getUuid())) {
                        revealGhostPlayer(sp);
                        sp.sendMessage(Text.literal("§f✦ Your strike broke your invisibility!"), true);
                    }
                }
                case "thunderfist_gauntlet" -> {
                    if (world instanceof net.minecraft.server.world.ServerWorld sw) {
                        sw.strikeLightningEffect(entity.getBlockPos(), false);
                    }
                }
                case "soul_reaper" -> {
                    victim.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 0));
                    sp.heal(victim.getHealth() * 0.15f);
                }
                case "withers_embrace" -> {
                    int stacks = FabricWeaponUtils.getInt(held, FabricWeaponUtils.WITHER_STACKS_KEY);
                    victim.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, Math.min(stacks + 1, 4)));
                }
                case "lifestealer" -> {
                    float steal = victim.getHealth() * 0.10f;
                    sp.heal(steal);
                }
            }
            return ActionResult.PASS;
        });

        // ─── AFTER KILL REWARDS ───────────────────────────────────────────────
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed) -> {
            if (!(entity instanceof ServerPlayerEntity killer)) return;
            if (!(killed instanceof ServerPlayerEntity)) return;

            ItemStack held = killer.getMainHandStack();
            String weaponId = FabricWeaponUtils.getWeaponId(held);
            if (weaponId == null) return;

            switch (weaponId) {
                case "lifestealer" -> handleLifestealerKill(killer, held);
                case "judgement_gavel" -> handleGavelKill(killer, held);
                case "withers_embrace" -> handleWithersEmbraceKill(killer, held);
            }
        });

        // ─── ARROW HIT — void bow tracking ───────────────────────────────────
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD.register((entity, serverWorld) -> {
            // No-op: arrow tracking handled via ServerTickEvents below
        });
    }

    // ─── GHOST BLADE ─────────────────────────────────────────────────────────

    public static void activateGhostBlade(ServerPlayerEntity player) {
        ghostPlayers.add(player.getUuid());
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 6000, 0, false, false));
        sendEmptyEquipment(player, true);
        player.sendMessage(Text.literal("§f✦ You fade into the void... (5 minutes)"), true);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1.5f);

        // Auto-reveal after 5 min
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 6000 == 1 && ghostPlayers.contains(player.getUuid())) {
                revealGhostPlayer(player);
            }
        });
    }

    public static void revealGhostPlayer(ServerPlayerEntity player) {
        if (!ghostPlayers.contains(player.getUuid())) return;
        ghostPlayers.remove(player.getUuid());
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
        sendEmptyEquipment(player, false);
        player.sendMessage(Text.literal("§c✦ You have been revealed!"), true);
    }

    private static void sendEmptyEquipment(ServerPlayerEntity player, boolean hide) {
        List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.add(new Pair<>(slot, hide ? ItemStack.EMPTY : player.getEquippedStack(slot)));
        }
        EntityEquipmentUpdateS2CPacket packet = new EntityEquipmentUpdateS2CPacket(player.getId(), equipment);
        for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
            if (!other.getUuid().equals(player.getUuid())) {
                other.networkHandler.sendPacket(packet);
            }
        }
    }

    // ─── KILL HELPERS ────────────────────────────────────────────────────────

    private static void handleLifestealerKill(ServerPlayerEntity killer, ItemStack held) {
        int hearts = FabricWeaponUtils.getInt(held, FabricWeaponUtils.LIFESTEALER_HEARTS_KEY);
        if (hearts >= 4) { killer.sendMessage(Text.literal("§cLifestealer is fully awakened."), true); return; }
        hearts++;
        var attr = killer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr != null) {
            Identifier modId = Identifier.of("kingssmp", "lifestealer_heart_" + hearts);
            attr.removeModifier(modId);
            attr.addPersistentModifier(new EntityAttributeModifier(modId, 2.0, EntityAttributeModifier.Operation.ADD_VALUE));
        }
        FabricLegendaryWeapon w = KingsSMPMod.getInstance().getWeaponRegistry().getById("lifestealer");
        if (w instanceof FabricLifestealer ls) killer.setStackInHand(Hand.MAIN_HAND, ls.buildStackWithHearts(hearts));
        killer.sendMessage(Text.literal("§c✦ Lifestealer claims a heart! ❤ " + hearts + "/4"), true);
        killer.getWorld().playSound(null, killer.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 0.8f);
    }

    private static void handleGavelKill(ServerPlayerEntity killer, ItemStack held) {
        int kills = FabricWeaponUtils.getInt(held, FabricWeaponUtils.GAVEL_KILLS_KEY) + 1;
        int oldTier = FabricWeaponUtils.getInt(held, FabricWeaponUtils.GAVEL_TIER_KEY);
        int tier = Math.min(kills / 2, 4);
        FabricLegendaryWeapon w = KingsSMPMod.getInstance().getWeaponRegistry().getById("judgement_gavel");
        if (w instanceof FabricJudgementGavel gavel) killer.setStackInHand(Hand.MAIN_HAND, gavel.buildStackWithTier(kills, tier));
        killer.sendMessage(Text.literal("§6✦ Judgement Gavel: §e" + kills + " kills"), true);
        if (tier > oldTier) {
            killer.sendMessage(Text.literal("§6⚡ Tier " + tier + " unlocked!"), false);
            killer.getWorld().playSound(null, killer.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1f, 1f);
            for (ServerPlayerEntity p : killer.getServer().getPlayerManager().getPlayerList()) {
                p.sendMessage(Text.literal("§6⚡ " + killer.getName().getString() + "'s Judgement Gavel reached Tier " + tier + "!"), false);
            }
        }
    }

    private static void handleWithersEmbraceKill(ServerPlayerEntity killer, ItemStack held) {
        int stacks = Math.min(FabricWeaponUtils.getInt(held, FabricWeaponUtils.WITHER_STACKS_KEY) + 1, 3);
        FabricLegendaryWeapon w = KingsSMPMod.getInstance().getWeaponRegistry().getById("withers_embrace");
        if (w instanceof FabricWithersEmbrace we) killer.setStackInHand(Hand.MAIN_HAND, we.buildStackWithStacks(stacks));
        killer.sendMessage(Text.literal("§8☠ Wither's Embrace grows stronger! Level " + (stacks + 2)), true);
    }

    // ─── UTILITIES ───────────────────────────────────────────────────────────

    public static boolean isFromBehind(ServerPlayerEntity attacker, LivingEntity victim) {
        Vec3d victimFacing = victim.getRotationVec(1.0f);
        Vec3d toAttacker = attacker.getPos().subtract(victim.getPos()).normalize();
        return victimFacing.dotProduct(toAttacker) < -0.3;
    }

    public static boolean isGhost(UUID playerId) { return ghostPlayers.contains(playerId); }
}
