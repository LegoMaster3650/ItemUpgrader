package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io._3650.itemupgrader.events.ModSpecialEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
	
	@ModifyVariable(method = "releaseUsing", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getRiptide(Lnet/minecraft/world/item/ItemStack;)I"), ordinal = 2)
	private int itemupgrader_riptide(int riptide, ItemStack stack, Level level, LivingEntity living, int timeLeft) {
		return riptide > 0 ? ModSpecialEvents.riptideBonus(stack, living, riptide) : 0;
	}
	
}