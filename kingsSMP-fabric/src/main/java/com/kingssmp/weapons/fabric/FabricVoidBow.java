package com.kingssmp.weapons.fabric;

import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FabricVoidBow extends FabricLegendaryWeapon {

    // Stores last arrow landing position per player
    public static final Map<UUID, Vec3d> lastArrowLoc = new HashMap<>();
    // Stores last hit entity per player (for entity teleport)
    public static final Map<UUID, UUID> lastHitEntity = new HashMap<>();
    public static final Map<UUID, Long> hitEntityExpiry = new HashMap<>();

    public FabricVoidBow() {
        super("void_bow", "Void Bow");
    }

    @Override
    public ItemStack buildStack() {
        ItemStack s = new ItemStack(Items.BOW);
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Void Bow").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§7Legendary Ranged Weapon"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§5» §dShift+RClick to teleport to the arrow location"),
                FabricWeaponUtils.loreLine("§5» §dHit an entity then Shift+RClick to teleport it"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1001));
        FabricWeaponUtils.setWeaponId(s, "void_bow");
        return s;
    }

    @Override
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {
        UUID pid = player.getUuid();
        ServerWorld world = (ServerWorld) player.getWorld();

        Long expiry = hitEntityExpiry.get(pid);
        boolean hasHitEntity = expiry != null && System.currentTimeMillis() < expiry;

        if (hasHitEntity) {
            // Teleport the hit entity to the arrow location
            UUID targetId = lastHitEntity.get(pid);
            Vec3d arrowLoc = lastArrowLoc.get(pid);
            if (targetId != null && arrowLoc != null) {
                for (Entity e : world.iterateEntities()) {
                    if (e.getUuid().equals(targetId) && e instanceof LivingEntity) {
                        e.teleport(world, arrowLoc.x, arrowLoc.y, arrowLoc.z, e.getYaw(), e.getPitch());
                        world.spawnParticles(ParticleTypes.PORTAL, arrowLoc.x, arrowLoc.y, arrowLoc.z, 30, 0.3, 0.5, 0.3, 0.1);
                        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1.5f);
                        player.sendMessage(Text.literal("§5✦ Entity teleported to arrow location!"), true);
                        break;
                    }
                }
            }
            lastHitEntity.remove(pid);
            hitEntityExpiry.remove(pid);
        } else {
            // Teleport self to last arrow location
            Vec3d arrowLoc = lastArrowLoc.get(pid);
            if (arrowLoc != null) {
                player.teleport(world, arrowLoc.x, arrowLoc.y, arrowLoc.z, player.getYaw(), player.getPitch());
                world.spawnParticles(ParticleTypes.PORTAL, arrowLoc.x, arrowLoc.y, arrowLoc.z, 30, 0.3, 0.5, 0.3, 0.1);
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1.5f);
                player.sendMessage(Text.literal("§5✦ Teleported to arrow location!"), true);
            } else {
                player.sendMessage(Text.literal("§cNo arrow location stored yet. Shoot first!"), true);
            }
        }
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
