package io._3650.itemupgrader_content.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.projectile.ThrownTrident;

@Mixin(ThrownTrident.class)
public interface ThrownTridentAccessor {
	
	@Accessor("dealtDamage")
	public void setDealtDamage(boolean dealtDamage);
	
}