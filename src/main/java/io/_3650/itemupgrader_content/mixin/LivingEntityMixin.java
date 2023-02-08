package io._3650.itemupgrader_content.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader_content.registry.ModUpgradeActions;
import io._3650.itemupgrader_content.registry.config.Config;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
	
	@Redirect(method = "travel(Lnet/minecraft/world/phys/Vec3;)V", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"), slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"),
					to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")))
	private Vec3 itemupgrader_airFriction(Vec3 vec3, double x, double y, double z) {
		if (ItemUpgraderApi.runActions(ModUpgradeActions.ELYTRA_LOW_FRICTION, new UpgradeEventData.Builder((LivingEntity)(Object)(this), EquipmentSlot.CHEST).cancellable()).isCancelled()) {
			return vec3.multiply(0.994D, 0.982D, 0.994D);
//			return vec3; //No Air Friction :trollge:
		}
		return vec3.multiply(0.99D, 0.98D, 0.99D);
	}
	
}