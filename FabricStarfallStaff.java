package com.kingssmp.weapons.fabric;

import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireballEntity;
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
import java.util.Random;
import java.util.UUID;

public class FabricStarfallStaff extends FabricLegendaryWeapon {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Random random = new Random();

    public FabricStarfallStaff() {
        super("starfall_staff", "Starfall Staff");
    }

    @Override
    public ItemStack buildStack() {
        ItemStack s = new ItemStack(Items.TRIDENT);
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Starfall Staff").formatted(Formatting.AQUA, Formatting.BOLD));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§bLegendary Staff"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§b» §3Shift+RClick to call down a Meteor Shower"),
                FabricWeaponUtils.loreLine("  §3of 12 fireballs from the sky around you."),
                FabricWeaponUtils.loreLine("§b» §3Passive: Thrown trident creates an"),
                FabricWeaponUtils.loreLine("  §3explosion of star particles on impact."),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1010));
        FabricWeaponUtils.setWeaponId(s, "starfall_staff");
        return s;
    }

    @Override
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {
        UUID pid = player.getUuid();
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(pid, 0L) > now) {
            long rem = (cooldowns.get(pid) - now) / 1000;
            player.sendMessage(Text.literal("§bStarfall on cooldown: " + rem + "s"), true);
            return;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d center = player.getPos();

        // Schedule 12 meteors falling from the sky with slight delays
        for (int i = 0; i < 12; i++) {
            final int idx = i;
            net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
                // Only fire once after the delay
                if (server.getTicks() % (idx * 4 + 1) != 0) return;

                double offsetX = (random.nextDouble() - 0.5) * 16;
                double offsetZ = (random.nextDouble() - 0.5) * 16;
                double spawnY = center.y + 40;

                Vec3d spawnPos = new Vec3d(center.x + offsetX, spawnY, center.z + offsetZ);
                Vec3d velocity = new Vec3d(0, -3, 0);

                FireballEntity fireball = new FireballEntity(world,
                        player,
                        velocity,
                        3); // explosion power
                fireball.setPosition(spawnPos);
                world.spawnEntity(fireball);

                // Star particles trailing the meteor
                world.spawnParticles(ParticleTypes.END_ROD,
                        spawnPos.x, spawnPos.y, spawnPos.z,
                        10, 0.3, 0.3, 0.3, 0.1);
            });
        }

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                SoundCategory.PLAYERS, 1f, 0.5f);
        world.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 2, center.z, 30, 1, 1, 1, 0.2);
        player.sendMessage(Text.literal("§b✦ Meteor Shower incoming!"), true);
        cooldowns.put(pid, now + 45000L); // 45s cooldown
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
