package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import io._3650.itemupgrader.events.ModSpecialEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;

@Mixin(value = IForgeItemStack.class)
public interface IForgeItemStackMixin extends IForgeItemStack {
	
	/**
	 * Does a little trolling
	 * @return Trolling complete
	 * @author your mom
	 * @reason What
	 * @see {@link IForgeItemStack#getItemEnchantability()}
	 */
	@Overwrite(remap = false)
	default int getItemEnchantability() {
		ItemStack stack = (ItemStack) (Object) this;
		return ModSpecialEvents.itemEnchantability(stack, stack.getItem().getItemEnchantability(stack));
	}
	
}