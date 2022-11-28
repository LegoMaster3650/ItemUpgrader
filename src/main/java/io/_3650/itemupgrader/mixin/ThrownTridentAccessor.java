package io._3650.itemupgrader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;

@Mixin(ThrownTrident.class)
public interface ThrownTridentAccessor {
	
	@Accessor("tridentItem")
	public ItemStack getTridentItem();
	
	@Accessor("dealtDamage")
	public void setDealtDamage(boolean dealtDamage);
	
}