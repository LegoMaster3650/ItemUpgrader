package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
	
	@Invoker("calculateFallDamage")
	public abstract int callCalculateFallDamage(float fallDistance, float damageMultiplier);
	
	@Invoker("getDamageAfterMagicAbsorb")
	public abstract float callGetDamageAfterMagicAbsorb(DamageSource damageSource, float damageAmount);
	
}