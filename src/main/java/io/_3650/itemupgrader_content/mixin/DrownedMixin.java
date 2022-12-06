package io._3650.itemupgrader_content.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;

@Mixin(Drowned.class)
public class DrownedMixin {
	
	@ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE", target = "net/minecraft/world/entity/projectile/ThrownTrident.<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"))
	private ItemStack itemupgrader_drownedTrident(Level level, LivingEntity living, ItemStack oldstack) {
		ItemStack stack = living.getMainHandItem();
		if (!stack.isEmpty() && stack.getItem() instanceof TridentItem) return stack.copy();
		else return oldstack;
	}
	
}