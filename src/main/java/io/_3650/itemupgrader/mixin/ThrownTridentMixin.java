package io._3650.itemupgrader.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.events.ModSpecialEvents;
import io._3650.itemupgrader.registry.ModUpgradeActions;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {
	
	protected ThrownTridentMixin(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}
	
	@Shadow
	@Final
	static EntityDataAccessor<Byte> ID_LOYALTY;
	
	@Shadow
	boolean dealtDamage;
	
	@Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
	private void itemupgrader_newThrownTrident(Level level, LivingEntity shooter, ItemStack stack, CallbackInfo ci) {
		byte loyalty = this.entityData.get(ID_LOYALTY);
		if (loyalty > 0) this.entityData.set(ID_LOYALTY, ModSpecialEvents.loyaltyBonus(stack, shooter, loyalty));
		ItemUpgraderApi.runActions(ModUpgradeActions.TRIDENT_THROW, new UpgradeEventData.Builder(shooter)
				.entry(UpgradeEntry.ITEM, stack)
				.entry(UpgradeEntry.PROJECTILE, (ThrownTrident)(Object)this));
	}
	
	@Redirect(method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;dealtDamage:Z", opcode = Opcodes.PUTFIELD))
	private void itemupgrader_onHitEntity_dealtDamage(ThrownTrident trident, boolean dealtDamage) {
		if (trident.getPierceLevel() > 0) {
			AbstractArrowAccessor access = (AbstractArrowAccessor)trident;
			IntOpenHashSet piercingIgnoreEntityIds = access.getPiercingIgnoreEntityIds();
			if (piercingIgnoreEntityIds == null) {
				access.setPiercingIgnoreEntityIds(piercingIgnoreEntityIds = new IntOpenHashSet(5));
			}
			
			if (piercingIgnoreEntityIds.size() >= trident.getPierceLevel()) this.dealtDamage = dealtDamage;
			else this.dealtDamage = false;
		} else this.dealtDamage = dealtDamage;
	}
	
	@Redirect(method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
	private void itemupgrader_onHitEntity_movement(ThrownTrident trident, Vec3 vec, EntityHitResult hit) {
		if (trident.getPierceLevel() > 0) {
			AbstractArrowAccessor access = (AbstractArrowAccessor)trident;
			IntOpenHashSet piercingIgnoreEntityIds = access.getPiercingIgnoreEntityIds();
			if (piercingIgnoreEntityIds == null) {
				access.setPiercingIgnoreEntityIds(piercingIgnoreEntityIds = new IntOpenHashSet(5));
			}
			
			if (piercingIgnoreEntityIds.size() >= trident.getPierceLevel()) {
				trident.setDeltaMovement(vec);
				return;
			}
			
			piercingIgnoreEntityIds.add(hit.getEntity().getId());
		} else {
			trident.setDeltaMovement(vec);
		}
	}
	
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;setNoPhysics(Z)V", shift = Shift.AFTER))
	private void itemupgrader_loyaltyFix(CallbackInfo ci) {
		this.setPierceLevel((byte)0); //remove piercing if loyalty to avoid a while(true) in AbtractArrow
	}
	
}