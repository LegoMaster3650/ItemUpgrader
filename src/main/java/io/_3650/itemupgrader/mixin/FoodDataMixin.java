package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.food.FoodData;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	
	@ModifyArg(method = "addExhaustion(F)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"), index = 1)
	private float itemupgrader_addExhaustion(float maxValue) {
		return Float.MAX_VALUE;
	}
	
}