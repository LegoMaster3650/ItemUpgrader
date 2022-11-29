package io._3650.itemupgrader.registry.types;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;

public interface UpgradeInventoryHolder {
	
	public ItemConsumer<ItemStack> itemupgrader_getInventoryPusher();
	
	public Supplier<ItemStack> itemupgrader_getInventoryPopper();
	
	public int itemupgrader_getInventorySize();
	
	public void itemupgrader_dropAllItems();
	
	@FunctionalInterface
	public static interface ItemConsumer<T> {
		public int push(T stack);
	}
	
}