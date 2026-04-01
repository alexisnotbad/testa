package com.kingssmp.utils;

import com.kingssmp.KingsSMPMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Utility class for working with legendary weapon ItemStacks.
 * Uses Yarn 1.21.1+build.3 mappings throughout.
 */
public class FabricWeaponUtils {

    public static final String WEAPON_ID_KEY = "kingssmp_weapon_id";
    public static final String LIFESTEALER_HEARTS_KEY = "kingssmp_ls_hearts";
    public static final String GAVEL_KILLS_KEY = "kingssmp_gavel_kills";
    public static final String GAVEL_TIER_KEY = "kingssmp_gavel_tier";
    public static final String SOUL_STACKS_KEY = "kingssmp_soul_stacks";
    public static final String WITHER_STACKS_KEY = "kingssmp_wither_stacks";

    public static Identifier id(String path) {
        return Identifier.of(KingsSMPMod.MOD_ID, path);
    }

    // ─── WEAPON ID ────────────────────────────────────────────────────────────

    public static String getWeaponId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) return null;
        NbtCompound nbt = nbtComp.copyNbt();
        if (!nbt.contains(WEAPON_ID_KEY)) return null;
        return nbt.getString(WEAPON_ID_KEY);
    }

    public static boolean isLegendary(ItemStack stack) {
        return getWeaponId(stack) != null;
    }

    public static void setWeaponId(ItemStack stack, String id) {
        NbtComponent existing = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = existing.copyNbt();
        nbt.putString(WEAPON_ID_KEY, id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    // ─── GENERIC INT NBT ──────────────────────────────────────────────────────

    public static int getInt(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) return 0;
        NbtComponent nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) return 0;
        return nbtComp.copyNbt().getInt(key);
    }

    public static void setInt(ItemStack stack, String key, int value) {
        NbtComponent existing = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = existing.copyNbt();
        nbt.putInt(key, value);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    // ─── TEXT HELPERS ─────────────────────────────────────────────────────────

    public static Text legendaryName(String name) {
        return Text.literal(name)
                .styled(s -> s.withBold(true).withItalic(false));
    }

    public static Text loreLine(String text) {
        return Text.literal(text).styled(s -> s.withItalic(false));
    }
}
