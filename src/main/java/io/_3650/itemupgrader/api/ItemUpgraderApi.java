package io._3650.itemupgrader.api;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.event.UpgradeEvent;
import io._3650.itemupgrader.registry.Reference;
import io._3650.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

/**
 * API class for interacting with the Item Upgrader system
 * @author LegoMaster3650
 */
public class ItemUpgraderApi {
	
	/* ItemUpgrader event system */
	
	/**
	 * Runs an action, automatically getting the <b>REQUIRED</b> ItemStack from the event data passed in
	 * @param actionId The {@linkplain ResourceLocation} ID of the action to run (<i>Recommended to use RegistryObject.getId()</i>)
	 * @param data An {@linkplain UpgradeEventData} which requires at least the ITEM entry to be present
	 * @throws NoSuchElementException If no {@linkplain UpgradeEntry#ITEM} entry was present
	 * @see #runAction(ResourceLocation, UpgradeEventData, ItemStack)
	 */
	public static void runAction(ResourceLocation actionId, UpgradeEventData data) throws NoSuchElementException {
		ItemStack stack = data.getOptional(UpgradeEntry.ITEM).orElseThrow(() -> new NoSuchElementException("Missing Item Entry"));
		runAction(actionId, data, stack);
	}
	
	/**
	 * Runs an action on the given item with the given data
	 * @param actionId The {@linkplain ResourceLocation} ID of the action to run (<i>Recommended to use RegistryObject.getId()</i>)
	 * @param data An {@linkplain UpgradeEventData} containing the data that will be provided.
	 * @param stack The {@linkplain ItemStack} to run the upgrade on
	 * @apiNote If a valid {@linkplain EquipmentSlot} is provided in the data, the upgrade will verify that against the data.
	 */
	public static void runAction(ResourceLocation actionId, UpgradeEventData data, ItemStack stack) {
		if (stack == null) return;
		if (stack.isEmpty()) return; //better safe than sorry
		if (!hasUpgrade(stack)) return; //very safe
		ItemUpgrade upgrade = getUpgrade(stack); //get upgrade on item
		if (upgrade == null) return; //stop if no upgrade on item
		if (!upgrade.isValidItem(stack)) {removeUpgradeNoUpdate(stack); return;} //stop if item upgrade is invalid and remove it
		EquipmentSlot slot = data.getEntryOrNull(UpgradeEntry.SLOT); //get event slot
		if (slot != null && !upgrade.isValidSlot(slot)) return; //stop if the event slot is invalid
		if (!upgrade.hasAction(actionId)) return;
		
		upgrade.getActions(actionId).forEach(action -> {
			if (action.getSerializer().providedData().verify(data.getEntrySet())) action.run(data);
			else ItemUpgrader.LOGGER.error("Upgrade data failed entry set verification: " + actionId);
		});
	}
	
	/* ItemStack upgrade utils */
	
	/**
	 * Checks if an ItemStack has an upgrade.<br>
	 * (If true, also implies {@linkplain ItemStack#hasTag()} to be true)
	 * @param stack The {@linkplain ItemStack} to check
	 * @return Whether the stack has an upgrade applied
	 */
	public static boolean hasUpgrade(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains(Reference.UPGRADE_TAG);
	}
	
	/**
	 * Gets the upgrade key from an ItemStack, if present, and {@code null} if not.
	 * @param stack The {@linkplain ItemStack} to get the upgrade key for
	 * @return The {@linkplain ResourceLocation} for the stack's {@linkplain ItemUpgrade}, if present
	 */
	@Nullable
	public static ResourceLocation getUpgradeKey(ItemStack stack) {
		try {
			return !hasUpgrade(stack) ? null : new ResourceLocation(stack.getTag().getString(Reference.UPGRADE_TAG));
		} catch (ResourceLocationException ignored) {
			return null;
		}
	}
	
	/**
	 * Gets an Optional potentialy containing the upgrade key from an ItemStack
	 * @param stack The {@linkplain ItemStack} to get the upgrade key for
	 * @return An {@linkplain Optional} containing the {@linkplain ResourceLocation} for the stack's {@linkplain ItemUpgrade}, if present
	 */
	public static Optional<ResourceLocation> getOptionalUpgradeKey(ItemStack stack) {
		if (!stack.hasTag()) return Optional.empty();
		try {
			return Optional.ofNullable(new ResourceLocation(stack.getTag().getString(Reference.UPGRADE_TAG)));
		} catch (ResourceLocationException ignored) {
			return Optional.empty();
		}
	}
	
	/**
	 * Gets the upgrade from an ItemStack, if present, and {@code null} if not.
	 * @param stack The {@linkplain ItemStack} to get the upgrade for
	 * @return The {@linkplain ItemUpgrade} from the stack, if present
	 */
	@Nullable
	public static ItemUpgrade getUpgrade(ItemStack stack) {
		try {
			return !hasUpgrade(stack) ? null : ItemUpgradeManager.INSTANCE.getUpgrade(new ResourceLocation(stack.getTag().getString(Reference.UPGRADE_TAG)));
		} catch (ResourceLocationException ignored) {
			return null;
		}
	}
	
	/**
	 * Gets an Optional potentialy containing the upgrade from an ItemStack
	 * @param stack The {@linkplain ItemStack} to get the upgrade for
	 * @return An {@linkplain Optional} containing the {@linkplain ItemUpgrade} from the stack, if present
	 */
	public static Optional<ItemUpgrade> getOptionalUpgrade(ItemStack stack) {
		if (!stack.hasTag()) return Optional.empty();
		try {
			return Optional.ofNullable(ItemUpgradeManager.INSTANCE.getUpgrade(new ResourceLocation(stack.getTag().getString(Reference.UPGRADE_TAG))));
		} catch (ResourceLocationException ignored) {
			return Optional.empty();
		}
	}
	
	/**
	 * Applies the given upgrade to an ItemStack<br>
	 * This fires cancellable events, and will fail to apply the upgrade if they are cancelled.
	 * @param stack The {@linkplain ItemStack} to apply the upgrade to
	 * @param upgrade The {@linkplain ResourceLocation} corresponding to the {@linkplain ItemUpgrade} to apply to the stack
	 * @return The stack with the upgrade applied
	 * @see #applyUpgradeNoUpdate(ItemStack, ResourceLocation)
	 */
	public static ItemStack applyUpgrade(ItemStack stack, ResourceLocation upgradeId) {
		if (hasUpgrade(stack)) {
			CompoundTag tag = stack.getTag();
			ResourceLocation previousUpgradeId;
			try {
				previousUpgradeId = new ResourceLocation(tag.getString(Reference.UPGRADE_TAG));
			} catch (ResourceLocationException ignored) {
				return stack;
			}
			removeUpgradeEvent(stack, previousUpgradeId);
			if (replaceUpgradeEvent(stack, upgradeId, previousUpgradeId)) return stack;
			if (applyUpgradeEventPre(stack, upgradeId)) return stack;
			tag.putString(Reference.UPGRADE_TAG, upgradeId.toString());
			stack.setTag(tag);
			applyUpgradeEventPost(stack, upgradeId);

		} else {
			CompoundTag tag = stack.getOrCreateTag();
			boolean cancel = applyUpgradeEventPre(stack, upgradeId);
			if (!cancel) {
				tag.putString(Reference.UPGRADE_TAG, upgradeId.toString());
				stack.setTag(tag);
				applyUpgradeEventPost(stack, upgradeId);
			}
		}
		return stack;
	}
	
	/**
	 * Applies the given upgrade to an ItemStack WITHOUT calling any upgrade events
	 * @param stack The {@linkplain ItemStack} to apply the upgrade to
	 * @param upgradeId The {@linkplain ResourceLocation} corresponding to the {@linkplain ItemUpgrade} to apply to the stack
	 * @return The stack with the upgrade applied
	 */
	public static ItemStack applyUpgradeNoUpdate(ItemStack stack, ResourceLocation upgradeId) {
		CompoundTag tag = stack.getOrCreateTag();
		tag.putString(Reference.UPGRADE_TAG, upgradeId.toString());
		stack.setTag(tag);
		return stack;
	}
	
	/**
	 * Removes the upgrade from the ItemStack if present
	 * @param stack The {@linkplain ItemStack} to remove the upgrade from
	 * @return The stack with no upgrade applied
	 */
	public static ItemStack removeUpgrade(ItemStack stack) {
		if (hasUpgrade(stack)) {
			CompoundTag tag = stack.getTag();
			ResourceLocation previousUpgradeId;
			try {
				previousUpgradeId = new ResourceLocation(tag.getString(Reference.UPGRADE_TAG));
			} catch (ResourceLocationException ignored) {
				return stack;
			}
			removeUpgradeEvent(stack, previousUpgradeId);
			tag.remove(Reference.UPGRADE_TAG);
			stack.setTag(tag);
		}
		return stack;
	}
	
	/**
	 * Removes the upgrade from the ItemStack if present WITHOUT calling any upgrade update events<br>
	 * Mainly used to invalidate any upgrades that have found themselves somewhere they don't belong
	 * @param stack The {@linkplain ItemStack} to remove the upgrade from
	 * @return The stack with no upgrade applied
	 * @see {@linkplain #removeUpgrade(ItemStack)} for most use cases
	 */
	public static ItemStack removeUpgradeNoUpdate(ItemStack stack) {
		if (hasUpgrade(stack)) {
			CompoundTag tag = stack.getTag();
			tag.remove(Reference.UPGRADE_TAG);
			stack.setTag(tag);
		}
		return stack;
	}
	
	/**@return If the upgrade addition was cancelled*/
	private static boolean applyUpgradeEventPre(ItemStack stack, ResourceLocation upgradeId) {
		UpgradeEvent.Apply.Pre event = new UpgradeEvent.Apply.Pre(stack, upgradeId);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isCanceled();
	}
	
	private static void applyUpgradeEventPost(ItemStack stack, ResourceLocation upgradeId) {
		MinecraftForge.EVENT_BUS.post(new UpgradeEvent.Apply.Post(stack, upgradeId));
	}
	
	private static void removeUpgradeEvent(ItemStack stack, ResourceLocation previousUpgradeId) {
		MinecraftForge.EVENT_BUS.post(new UpgradeEvent.Remove(stack, previousUpgradeId));
	}
	
	/**@return If the upgrade replacement was cancelled*/
	private static boolean replaceUpgradeEvent(ItemStack stack, ResourceLocation upgradeId, ResourceLocation previousUpgradeId) {
		UpgradeEvent.Replace replace = new UpgradeEvent.Replace(stack, upgradeId, previousUpgradeId);
		MinecraftForge.EVENT_BUS.post(replace);
		return replace.isCanceled();
	}
	
}