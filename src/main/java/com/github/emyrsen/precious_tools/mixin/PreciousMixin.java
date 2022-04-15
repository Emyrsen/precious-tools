package com.github.emyrsen.precious_tools.mixin;

import com.github.emyrsen.precious_tools.Registries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class PreciousMixin {

	@Shadow
	public ClientPlayerInteractionManager interactionManager;

	@Shadow
	public ClientPlayerEntity player;
  
  /** Does the item stack have the enchantment? */
  private static boolean hasEnchantment(ItemStack iStack) {
    return EnchantmentHelper.getLevel(Registries.PRECIOUS, iStack) > 0;
  }

  /** Should use of the item stack be prevented? */
  private static boolean doPreventUsage(ItemStack iStack) {
    if (!iStack.isDamageable()) return false;
    if (!hasEnchantment(iStack)) return false;
    
    int remainingDurability = iStack.getMaxDamage() - iStack.getDamage();
    if (remainingDurability > 1) return false;

    return true;
  }
  
	@Inject(method = "handleBlockBreaking(Z)V", at = @At("HEAD"), cancellable = true)
	public void onHandleBlockBreaking(boolean isBreakPressed, CallbackInfo info) {
		if (isBreakPressed && doPreventUsage(player.getInventory().getMainHandStack())) {
			interactionManager.cancelBlockBreaking();
			info.cancel();
		}
	}

	@Inject(method = "doAttack()Z", at = @At("HEAD"), cancellable = true)
	public void onDoAttack(CallbackInfo info) {
		if (doPreventUsage(player.getInventory().getMainHandStack())) {
			info.cancel();
		}
	}

	@Inject(method = "doItemUse()V", at = @At("HEAD"), cancellable = true)
	public void onDoItemUse(CallbackInfo info) {
		if (doPreventUsage(player.getInventory().getMainHandStack())) {
			info.cancel();
		}
	}
}
