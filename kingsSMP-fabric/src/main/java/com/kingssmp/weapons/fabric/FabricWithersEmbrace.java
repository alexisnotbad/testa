package com.kingssmp.weapons.fabric;

import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class FabricWithersEmbrace extends FabricLegendaryWeapon {

    public FabricWithersEmbrace() {
        super("withers_embrace", "Wither's Embrace");
    }

    @Override
    public ItemStack buildStack() {
        return buildStackWithStacks(0);
    }

    public ItemStack buildStackWithStacks(int witherStacks) {
        ItemStack s = new ItemStack(Items.NETHERITE_SWORD);
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Wither's Embrace").formatted(Formatting.BLACK, Formatting.BOLD));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§8Legendary Cursed Sword"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8» §7Passive: Apply §8Wither II§7 on every hit."),
                FabricWeaponUtils.loreLine("§8» §7Each player kill permanently upgrades"),
                FabricWeaponUtils.loreLine("  §7the Wither level (max Wither V)."),
                FabricWeaponUtils.loreLine("§8» §7Shift+RClick: Explode in a burst of"),
                FabricWeaponUtils.loreLine("  §7Wither skulls in all directions."),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8☠ Wither Level: §7" + (witherStacks + 2) + " (§8" + witherStacks + "§7 upgrades)"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1009));
        FabricWeaponUtils.setWeaponId(s, "withers_embrace");
        FabricWeaponUtils.setInt(s, FabricWeaponUtils.WITHER_STACKS_KEY, witherStacks);
        return s;
    }

    @Override
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {
        // Burst wither skulls in 8 directions
        net.minecraft.server.world.ServerWorld world = (net.minecraft.server.world.ServerWorld) player.getWorld();
        net.minecraft.util.math.Vec3d pos = player.getEyePos();

        double[][] directions = {
            {1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1},
            {1,0,1},{-1,0,-1}
        };

        for (double[] dir : directions) {
            net.minecraft.entity.projectile.WitherSkullEntity skull =
                new net.minecraft.entity.projectile.WitherSkullEntity(world, player,
                    pos.x, pos.y, pos.z,
                    new net.minecraft.util.math.Vec3d(dir[0], dir[1], dir[2]));
            skull.setCharged(false);
            world.spawnEntity(skull);
        }

        world.playSound(null, player.getBlockPos(),
            net.minecraft.sound.SoundEvents.ENTITY_WITHER_SHOOT,
            net.minecraft.sound.SoundCategory.PLAYERS, 1f, 0.7f);
        player.sendMessage(Text.literal("§8☠ Wither's Embrace unleashed!"), true);
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
