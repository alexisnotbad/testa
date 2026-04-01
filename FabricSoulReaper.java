package com.kingssmp.weapons.fabric;

import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.WitherSkullEntity;
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

public class FabricSoulReaper extends FabricLegendaryWeapon {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public FabricSoulReaper() {
        super("soul_reaper", "Soul Reaper");
    }

    @Override
    public ItemStack buildStack() {
        ItemStack s = new ItemStack(Items.NETHERITE_HOE); // Scythe appearance via model
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Soul Reaper").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§8Legendary Scythe"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8» §7Shift+RClick to launch a §8Homing Soul§7."),
                FabricWeaponUtils.loreLine("  §7The soul seeks the nearest enemy and"),
                FabricWeaponUtils.loreLine("  §7drains their life force into you."),
                FabricWeaponUtils.loreLine("§8» §7Passive: Apply §8Wither I§7 on every hit."),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1007));
        FabricWeaponUtils.setWeaponId(s, "soul_reaper");
        return s;
    }

    @Override
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {
        UUID pid = player.getUuid();
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(pid, 0L) > now) {
            long rem = (cooldowns.get(pid) - now) / 1000;
            player.sendMessage(Text.literal("§cSoul Reaper on cooldown: " + rem + "s"), true);
            return;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d eyePos = player.getEyePos();

        // Find nearest non-player living entity within 30 blocks
        LivingEntity target = null;
        double minDist = Double.MAX_VALUE;
        Box searchBox = new Box(eyePos.x - 30, eyePos.y - 30, eyePos.z - 30,
                                eyePos.x + 30, eyePos.y + 30, eyePos.z + 30);
        for (Entity e : world.getEntitiesByClass(LivingEntity.class, searchBox,
                en -> en != player && en.isAlive())) {
            double d = e.squaredDistanceTo(player);
            if (d < minDist) { minDist = d; target = (LivingEntity) e; }
        }

        if (target == null) {
            player.sendMessage(Text.literal("§cNo target nearby for the Soul Reaper."), true);
            return;
        }

        final LivingEntity finalTarget = target;

        // Launch a wither skull as the "soul projectile"
        WitherSkullEntity skull = new WitherSkullEntity(world, player,
                eyePos.x, eyePos.y, eyePos.z,
                finalTarget.getPos().subtract(eyePos).normalize());
        skull.setCharged(false);
        world.spawnEntity(skull);

        // Homing: tick toward target every 5 ticks for 3 seconds
        final int[] ticks = {0};
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks[0]++;
            if (ticks[0] > 60 || !skull.isAlive() || !finalTarget.isAlive()) return;
            if (ticks[0] % 5 == 0) {
                Vec3d dir = finalTarget.getEyePos().subtract(skull.getPos()).normalize().multiply(0.8);
                skull.setVelocity(dir);
            }
            // Particle trail
            world.spawnParticles(ParticleTypes.SOUL, skull.getX(), skull.getY(), skull.getZ(),
                    3, 0.1, 0.1, 0.1, 0.01);
        });

        // Life drain on hit - handled in event handler via soul_reaper weapon id
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_SHOOT,
                SoundCategory.PLAYERS, 1f, 1.5f);
        player.sendMessage(Text.literal("§8✦ A homing soul has been unleashed!"), true);
        cooldowns.put(pid, now + 15000L); // 15s cooldown
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
