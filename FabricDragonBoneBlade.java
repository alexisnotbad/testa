package com.kingssmp.weapons.fabric;

import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FabricDragonBoneBlade extends FabricLegendaryWeapon {

    private static final Map<UUID, EnderDragonEntity> activeDragons = new HashMap<>();
    private static final Map<UUID, Long> summonCooldowns = new HashMap<>();

    public FabricDragonBoneBlade() {
        super("dragonbone_blade", "Dragonbone Blade");
    }

    @Override
    public ItemStack buildStack() {
        ItemStack s = new ItemStack(Items.NETHERITE_SWORD);
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Dragonbone Blade").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§7Legendary Dragon Sword"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§5» §dShift+RClick to summon a rideable Ender Dragon"),
                FabricWeaponUtils.loreLine("§5» §dRClick while riding to launch Dragon's Breath"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8Drops near the End Portal on first Dragon kill"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1004));
        FabricWeaponUtils.setWeaponId(s, "dragonbone_blade");
        return s;
    }

    @Override
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {
        UUID pid = player.getUuid();
        long now = System.currentTimeMillis();

        // Dismiss existing dragon
        EnderDragonEntity existing = activeDragons.get(pid);
        if (existing != null && !existing.isRemoved()) {
            existing.getPassengerList().forEach(p -> existing.removePassenger(p));
            existing.discard();
            activeDragons.remove(pid);
            player.sendMessage(Text.literal("§5✦ Your dragon has been dismissed."), true);
            return;
        }

        // Cooldown check
        long cooldownEnd = summonCooldowns.getOrDefault(pid, 0L);
        if (now < cooldownEnd) {
            player.sendMessage(Text.literal("§cDragon cooldown: " + (cooldownEnd - now) / 1000 + "s"), true);
            return;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d spawnPos = player.getPos().add(0, 5, 0);

        EnderDragonEntity dragon = EntityType.ENDER_DRAGON.create(world);
        if (dragon == null) return;
        dragon.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, 0, 0);
        dragon.setCustomName(Text.literal("✦ King's Dragon ✦").formatted(Formatting.LIGHT_PURPLE));
        dragon.setCustomNameVisible(true);
        dragon.setNoAi(true);
        world.spawnEntity(dragon);
        activeDragons.put(pid, dragon);
        summonCooldowns.put(pid, now + 60000L);

        // Mount after 5 ticks with retry
        final int[] attempts = {0};
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            attempts[0]++;
            if (attempts[0] > 60 || dragon.isRemoved()) return;
            if (attempts[0] % 5 == 0) {
                player.teleport(world, dragon.getX(), dragon.getY() + 1, dragon.getZ(), player.getYaw(), player.getPitch());
                dragon.addPassenger(player);
                if (player.getVehicle() != null) {
                    player.sendMessage(Text.literal("§5✦ Your dragon awaits! (RClick to launch Dragon's Breath)"), true);
                    world.playSound(null, dragon.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 1f, 1f);
                }
            }
        });

        // Auto-dismiss after 5 minutes
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 6000 == 1 && !dragon.isRemoved()) {
                dragon.getPassengerList().forEach(p -> dragon.removePassenger(p));
                dragon.discard();
                activeDragons.remove(pid);
                if (player.isAlive()) {
                    player.sendMessage(Text.literal("§7✦ Your dragon has returned to the void."), true);
                }
            }
        });
    }

    @Override
    public void onRightClick(ServerPlayerEntity player, ItemStack stack) {
        // Dragon's breath while riding
        if (!(player.getVehicle() instanceof EnderDragonEntity)) return;
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d dir = player.getRotationVec(1.0f).multiply(3.0);
        Vec3d origin = player.getEyePos().add(dir);

        DragonFireballEntity fireball = new DragonFireballEntity(world, player, dir);
        fireball.setPosition(origin);
        world.spawnEntity(fireball);
        world.spawnParticles(ParticleTypes.DRAGON_BREATH, origin.x, origin.y, origin.z, 20, 0.5, 0.5, 0.5, 0.05);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 1f, 1f);
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
