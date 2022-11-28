package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io._3650.itemupgrader.events.ModSpecialEvents;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	
	//this took me 2 hours to debug because mixin wanted to whine about trying to do the exact same stuff I did in ApplyBonusCountMixin but nooooo it doesn't work here for some reason BUT I WONT TELL YOU WHY I'LL JUST SAY "MIXIN FAILED OH NO"
	//I still have no idea which fix here solved it I think it was the index = 4 thing but I dont want to risk breaking it for another 500 years
	@ModifyVariable(method = "hurt(ILjava/util/Random;Lnet/minecraft/server/level/ServerPlayer;)Z", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I"), index = 4)
	private int itemupgrader_hurt(int unbreaking) {
		return ModSpecialEvents.unbreakingBonus((ItemStack) (Object) this, unbreaking);
	}
	
}