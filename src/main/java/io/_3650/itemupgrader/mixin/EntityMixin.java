package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io._3650.itemupgrader.registry.types.UpgradeInventoryHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;

@Mixin(Entity.class)
public abstract class EntityMixin {
	
	@Inject(method = "discard", at = @At("HEAD"))
	private void itemupgrader_discard(CallbackInfo ci) {
		if ((Entity)(Object)this instanceof ThrownTrident) {
			((UpgradeInventoryHolder)this).itemupgrader_dropAllItems();
		}
	}
	
}