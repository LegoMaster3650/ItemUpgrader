package io._3650.itemupgrader.api.type;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for Upgrade Actions (The base of upgrades that represent an event firing)
 * @author LegoMaster3650
 */
public abstract class UpgradeAction extends IUpgradeType {
	
	private final Set<EquipmentSlot> validSlots;
	
	/**
	 * Constructs an {@linkplain UpgradeAction} using the given internals
	 * @param internals {@linkplain IUpgradeInternals} containing information for this type
	 * @param validSlots A {@linkplain Set} of the {@linkplain EquipmentSlot}s the upgrade is valid for
	 */
	public UpgradeAction(@Nonnull IUpgradeInternals internals, @Nonnull Set<EquipmentSlot> validSlots) {
		super(internals);
		this.validSlots = ImmutableSet.copyOf(validSlots);
	}
	
	/**
	 * Checks if the given slot is valid for this action<br>
	 * Note: {@code null} is always valid
	 * @param slot The {@linkplain EquipmentSlot} to check
	 * @return Whether the slot is valid for this action or not
	 */
	public boolean isValidSlot(@Nullable EquipmentSlot slot) {
		return slot == null || validSlots.isEmpty() || validSlots.contains(slot);
	}
	
	/**
	 * Gets the set of the valid slots for this upgrade
	 * @return The {@linkplain Set} of {@linkplain EquipmentSlot}s that are valid for this action
	 */
	public Set<EquipmentSlot> getValidSlots() {
		return this.validSlots;
	}
	
	@Nullable
	private String descriptionId;
	
	@Override
	protected String descriptorIdBase() {
		return "upgradeAction";
	}
	
	/**
	 * Allows post-processing logic to be applied to the given tooltip
	 * @param tooltip The pre-generated default tooltip to be modified
	 * @param stack The {@linkplain ItemStack} to get tooltip context from
	 * @return The tooltip after modification (you can overwrite it)
	 */
	public abstract MutableComponent applyTooltip(MutableComponent tooltip, ItemStack stack);
	
	/**
	 * Defines the behavior for an UpgradeAction after running
	 * @param data The {@linkplain UpgradeEventData} parameters passed in
	 */
	public abstract void run(UpgradeEventData data);
	
	/**
	 * Use this to return your class's serializer instance.<br>
	 * Ensure the return type is an UpgradeActionSerializer&lt;<b>This Class</b>&gt; in some form, whether just that or a subclass of that, just please make sure it's not the default Wildcard ? type
	 * @return Your own serializer instance
	 */
	public abstract UpgradeActionSerializer<?> getSerializer();
	
}