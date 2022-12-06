package io._3650.itemupgrader_content.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io._3650.itemupgrader_content.registry.config.Config;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin {
	
	@Inject(method = "shouldQuickMoveToAdditionalSlot(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private void itemupgrader_shouldQuickMoveToAdditionalSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (Config.COMMON.basePackEnabled.get() && Config.COMMON.basePackRecipes.get() && stack.is(Items.TOTEM_OF_UNDYING)) cir.setReturnValue(false);
	}
	
}