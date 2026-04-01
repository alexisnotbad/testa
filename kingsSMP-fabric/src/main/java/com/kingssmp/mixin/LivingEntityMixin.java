package com.kingssmp.mixin;

import com.kingssmp.listeners.FabricWeaponEventHandler;
import com.kingssmp.utils.FabricWeaponUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Intercepts the final damage amount before it is applied to a LivingEntity.
     * Used to apply the Pretty Kitty Blade 2x backstab multiplier when the
     * attacker is hitting from behind.
     */
    @ModifyVariable(
        method = "damage",
        at = @At("HEAD"),
        argsOnly = true,
        index = 2
    )
    private float kingssmp_modifyDamage(float amount, DamageSource source) {
        if (!(source.getAttacker() instanceof ServerPlayerEntity attacker)) return amount;

        ItemStack held = attacker.getMainHandStack();
        if (!"pretty_kitty_blade".equals(FabricWeaponUtils.getWeaponId(held))) return amount;

        LivingEntity victim = (LivingEntity)(Object) this;
        if (FabricWeaponEventHandler.isFromBehind(attacker, victim)) {
            return amount * 2.0f;
        }
        return amount;
    }
}
