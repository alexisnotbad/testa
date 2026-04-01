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

public class FabricThunderfistGauntlet extends FabricLegendaryWeapon {

    private final Map<UUID, Long> thunderCooldowns = new HashMap<>();

    public FabricThunderfistGauntlet() {
        super("thunderfist_gauntlet", "Thunderfist Gauntlet");
    }

    @Override
    public ItemStack buildStack() {
        ItemStack s = new ItemStack(Items.MACE);
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Thunderfist Gauntlet").formatted(Formatting.YELLOW, Formatting.BOLD));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§eĽegendary Gauntlet"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§e» §6Passive: Strike lightning at every hit target."),
                FabricWeaponUtils.loreLine("§e» §6Shift+RClick: Thunder Clap — launches"),
                FabricWeaponUtils.loreLine("  §6all nearby enemies upward with lightning."),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1008));
        FabricWeaponUtils.setWeaponId(s, "thunderfist_gauntlet");
        return s;
    }

    @Override
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {
        UUID pid = player.getUuid();
        long now = System.currentTimeMillis();
        if (thunderCooldowns.getOrDefault(pid, 0L) > now) {
            long rem = (thunderCooldowns.get(pid) - now) / 1000;
            player.sendMessage(Text.literal("§eThunder Clap on cooldown: " + rem + "s"), true);
            return;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d pos = player.getPos();

        // Strike lightning at player's position
        world.strikeLightningEffect(player.getBlockPos(), false);

        // Launch and zap all nearby entities within 8 blocks
        Box box = new Box(pos.x - 8, pos.y - 8, pos.z - 8, pos.x + 8, pos.y + 8, pos.z + 8);
        for (Entity e : world.getEntitiesByClass(LivingEntity.class, box, en -> en != player && en.isAlive())) {
            // Lightning strike on each
            world.strikeLightningEffect(e.getBlockPos(), false);
            // Launch upward
            e.setVelocity(e.getVelocity().add(0, 2.5, 0));
            // Slowness after landing
            ((LivingEntity) e).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
        }

        // Shockwave particles
        world.spawnParticles(ParticleTypes.FLASH, pos.x, pos.y + 1, pos.z, 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1, pos.z, 80, 3, 1, 3, 0.3);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                SoundCategory.PLAYERS, 1f, 0.8f);

        player.sendMessage(Text.literal("§e⚡ THUNDER CLAP!"), true);
        thunderCooldowns.put(pid, now + 20000L); // 20s cooldown
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
