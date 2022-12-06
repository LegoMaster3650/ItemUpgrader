package io._3650.itemupgrader_content.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import io._3650.itemupgrader.api.event.LivingTotemEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	
	@Shadow
	protected ItemStack useItem;
	
	@Shadow
	protected int useItemRemaining;
	
	@Inject(method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void itemupgrader_checkTotemDeathProtectionPre(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir, ItemStack itemstack, InteractionHand[] hands, int idk, int idk2, InteractionHand hand, ItemStack itemstack1) {
		LivingEntity thisLiving = (LivingEntity) (Object) this; //a mediocre amount of trolling
		LivingTotemEvent event = new LivingTotemEvent.Pre(thisLiving, itemstack1, damageSource, hand);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) cir.setReturnValue(false);
	}
	
	@Inject(method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void itemupgrader_checkTotemDeathProtectionPost(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir, ItemStack itemstack) {
		LivingEntity thisLiving = (LivingEntity) (Object) this; //a moderate amount of trolling
		LivingTotemEvent event = new LivingTotemEvent.Post(thisLiving, itemstack, damageSource);
		MinecraftForge.EVENT_BUS.post(event);
	}
	
}