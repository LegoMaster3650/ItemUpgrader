package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io._3650.itemupgrader.events.ModSpecialEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;

@Mixin(ApplyBonusCount.class)
public abstract class MixinApplyBonusCount {
	
	@Shadow
	@Final
	Enchantment enchantment;
	
	@ModifyVariable(method = "run", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I"))
	private int itemupgrader_run(int enchantmentLevel, ItemStack stack, LootContext context) {
		return ModSpecialEvents.lootEnchantmentBonus(this.enchantment, enchantmentLevel, stack, context);
	}
	
}