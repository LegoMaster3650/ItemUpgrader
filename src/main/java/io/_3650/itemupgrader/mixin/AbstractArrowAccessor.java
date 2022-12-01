package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.projectile.AbstractArrow;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
	
	@Accessor("piercingIgnoreEntityIds")
	public IntOpenHashSet getPiercingIgnoreEntityIds();

	@Accessor("piercingIgnoreEntityIds")
	public void setPiercingIgnoreEntityIds(IntOpenHashSet piercingIgnoreEntityIds);
	
	@Accessor("pickup")
	public AbstractArrow.Pickup getPickup();
	
}