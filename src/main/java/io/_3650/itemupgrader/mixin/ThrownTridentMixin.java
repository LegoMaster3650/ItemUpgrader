package io._3650.itemupgrader.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
import io._3650.itemupgrader.registry.types.UpgradeHitLimited;
import io._3650.itemupgrader.registry.types.UpgradeInventoryHolder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow implements UpgradeInventoryHolder, UpgradeHitLimited {
	
	protected ThrownTridentMixin(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}
	
	@Shadow
	@Final
	static EntityDataAccessor<Byte> ID_LOYALTY;

	@Unique
	private static final EntityDataAccessor<Byte> UPGRADE_HITS = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BYTE);
	
	@Shadow
	boolean dealtDamage;
	
	@Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
	private void itemupgrader_newThrownTrident(Level level, LivingEntity shooter, ItemStack stack, CallbackInfo ci) {
		this.entityData.set(UPGRADE_HITS, (byte)3);
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
	
	@Unique
	private final List<ItemStack> tridentInventory = new ArrayList<>();
	
	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void itemupgrader_defineSynchedData(CallbackInfo ci) {
		this.entityData.define(UPGRADE_HITS, (byte)0);
	}
	
	@Unique
	@Override
	public void itemupgrader_setHits(byte hits) {
		this.entityData.set(UPGRADE_HITS, hits);
	}
	
	@Unique
	@Override
	public byte itemupgrader_getHits() {
		return this.entityData.get(UPGRADE_HITS);
	}
	
	@Unique
	private int tridentInventoryPush(ItemStack stack) {
		int initialCount = stack.getCount();
		if (stack.isEmpty()) return -1;
		do {
			ItemStack target = this.itemupgrader_getFirstOpenSlot(stack);
			if (target == null) break;
			int i = stack.getCount();
			int j = i;
			if (i > target.getMaxStackSize() - target.getCount()) {
				j = target.getMaxStackSize() - target.getCount();
			}
			i -= j;
			target.grow(j);
			stack.setCount(i);
		} while (stack.getCount() > 0);
		if (stack.getCount() <= 0) return initialCount;
		else if (this.tridentInventory.size() < 9) {
			ItemStack stack1 = stack.copy();
			stack.setCount(0);
			this.tridentInventory.add(stack1);
			return initialCount;
		} else if (stack.getCount() < initialCount) return initialCount - stack.getCount();
		else return -1;
	}
	
	@Unique
	@Nullable
	private ItemStack itemupgrader_getFirstOpenSlot(ItemStack stack) {
		for (ItemStack target : this.tridentInventory) {
			if (ItemStack.isSameItemSameTags(stack, target) && target.isStackable() && target.getCount() < target.getMaxStackSize()) return target;
		}
		return null;
	}
	
	@Unique
	@Override
	public UpgradeInventoryHolder.ItemConsumer<ItemStack> itemupgrader_getInventoryPusher() {
		return this::tridentInventoryPush;
	}
	
	@Unique
	@Override
	public Supplier<ItemStack> itemupgrader_getInventoryPopper() {
		return () -> this.tridentInventory.remove(0);
	}
	
	@Unique
	@Override
	public int itemupgrader_getInventorySize() {
		return this.tridentInventory.size();
	}
	
	@Unique
	@Override
	public void itemupgrader_dropAllItems() {
		if (this.level.isClientSide) return;
		var popper = this.itemupgrader_getInventoryPopper();
		while (this.tridentInventory.size() > 0) {
			ItemStack stack = popper.get();
			ItemEntity entity = this.spawnAtLocation(stack, 0.1F);
			if (entity != null) {
				Entity owner = this.getOwner();
				if (owner != null) entity.setOwner(owner.getUUID());
				entity.setNoPickUpDelay();
			}
		}
	}
	
	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void itemupgrader_addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		if (this.tridentInventory.size() > 0) {
			ListTag list = new ListTag();
			for (var stack : this.tridentInventory) {
				list.add(stack.save(new CompoundTag()));
			}
			tag.put("UpgradeTridentInventory", list);
		}
		tag.putByte("UpgradeHitsRemaining", this.itemupgrader_getHits());
	}
	
	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void itemupgrader_readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("UpgradeTridentInventory", CompoundTag.TAG_LIST)) {
			ListTag list = tag.getList("UpgradeTridentInventory", CompoundTag.TAG_COMPOUND);
			for (var i = 0; i < list.size(); i++) {
				CompoundTag itemTag = list.getCompound(i);
				this.tridentInventory.add(ItemStack.of(itemTag));
			}
		}
		this.itemupgrader_setHits(tag.getByte("UpgradeHitsRemaining"));
	}
	
}