package com.kingssmp.weapons.fabric;

import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

public class FabricPrettyKittyBlade extends FabricLegendaryWeapon {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Random random = new Random();

    public FabricPrettyKittyBlade() {
        super("pretty_kitty_blade", "Pretty Kitty Princess Blade");
    }

    @Override
    public ItemStack buildStack() {
        ItemStack s = new ItemStack(Items.GOLDEN_SWORD);
        s.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ Pretty Kitty Princess Blade").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricWeaponUtils.loreLine("§7Legendary Sword"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§d» §5Shift+RClick to summon a horde of cats"),
                FabricWeaponUtils.loreLine("§d» §5Backstab Passive: 2x damage from behind"),
                FabricWeaponUtils.loreLine(""),
                FabricWeaponUtils.loreLine("§8[Legendary] [KingsSMP]")
        )));
        s.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
        s.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1006));
        FabricWeaponUtils.setWeaponId(s, "pretty_kitty_blade");
        return s;
    }

    @Override
    public void onShiftRightClick(ServerPlayerEntity player, ItemStack stack) {
        UUID pid = player.getUuid();
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(pid, 0L) > now) {
            long rem = (cooldowns.get(pid) - now) / 1000;
            player.sendMessage(Text.literal("§cKitty Horde on cooldown: " + rem + "s"), true);
            return;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d pos = player.getPos();

        for (int i = 0; i < 6; i++) {
            CatEntity cat = EntityType.CAT.create(world);
            if (cat == null) continue;
            double ox = (random.nextDouble() - 0.5) * 4;
            double oz = (random.nextDouble() - 0.5) * 4;
            cat.refreshPositionAndAngles(pos.x + ox, pos.y + 0.5, pos.z + oz, 0, 0);
            cat.setTamed(true);
            cat.setOwner(player);
            cat.setCustomName(Text.literal("✦ Kitty").formatted(Formatting.LIGHT_PURPLE));
            cat.setCustomNameVisible(true);
            world.spawnEntity(cat);

            // Despawn after 60 seconds
            final CatEntity finalCat = cat;
            net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
                if (server.getTicks() % 1200 == 1 && !finalCat.isRemoved()) {
                    finalCat.discard();
                }
            });
        }

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_CAT_AMBIENT, SoundCategory.NEUTRAL, 1f, 1f);
        player.sendMessage(Text.literal("§d✦ Your kitty horde has arrived! Meow."), true);
        cooldowns.put(pid, now + 30000L);
    }

    @Override
    public net.minecraft.recipe.Recipe<?> buildRecipe() { return null; }
}
