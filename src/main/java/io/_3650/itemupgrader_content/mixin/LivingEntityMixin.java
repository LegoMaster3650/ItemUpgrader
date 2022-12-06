package io._3650.itemupgrader_content.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io._3650.itemupgrader_content.registry.config.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	
	@Shadow
	protected ItemStack useItem;
	
	@Shadow
	protected int useItemRemaining;
	
	@Inject(method = "isBlocking", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	private void itemupgrader_isBlocking(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(this.useItem.getItem().getUseDuration(this.useItem) - this.useItemRemaining >= Config.COMMON.shieldRaiseSpeed.get());
	}
	
}