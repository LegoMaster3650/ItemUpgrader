package io._3650.itemupgrader.api;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.event.UpgradeEvent;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.registry.Reference;
import io._3650.itemupgrader.upgrades.EntryCategoryManager;
import io._3650.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.RegistryObject;

/**
 * API class for interacting with the Item Upgrader system
 * @author LegoMaster3650
 */
public class ItemUpgraderApi {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	/* ItemUpgrader event system */
	
	/**
	 * Runs an action, automatically building the data against the given action and getting the <b>REQUIRED</b> ItemStack from the event data passed in<br>
	 * <br>
	 * NOTE: If a valid {@linkplain EquipmentSlot} is provided in the data, the upgrade will verify that against the data.
	 * @param actionEntry A {@linkplain RegistryObject} for the {@linkplain UpgradeActionSerializer} of the action to run
	 * @param builder An {@linkplain UpgradeEventData.Builder} to be built against the given action
	 * @throws NoSuchElementException If no {@linkplain UpgradeEntry#ITEM ITEM} entry was present
	 * @return The built UpgradeEventData
	 * @see #runActions(ResourceLocation, UpgradeEventData)
	 */
	public static UpgradeEventData runActions(RegistryObject<? extends UpgradeActionSerializer<?>> actionEntry, UpgradeEventData.Builder builder) throws NoSuchElementException {
		UpgradeEventData data = builder.build(actionEntry.get().getProvidedData());
		ItemStack stack = data.getOptional(UpgradeEntry.ITEM).orElseThrow(() -> new NoSuchElementException("Missing Item Entry"));
		runActions(actionEntry.getId(), data, stack);
		return data;
	}
	
	/**
	 * Runs an action on the given item, automatically building the data against the given action<br>
	 * <br>
	 * NOTE: If a valid {@linkplain EquipmentSlot} is provided in the data, the upgrade will verify that against the data.
	 * @param actionEntry A {@linkplain RegistryObject} for the {@linkplain UpgradeActionSerializer} of the action to run
	 * @param builder An {@linkplain UpgradeEventData.Builder} to be built against the given action
	 * @param stack The {@linkplain ItemStack} to run the upgrade on
	 * @return The built UpgradeEventData
	 * @see #runActions(ResourceLocation, UpgradeEventData, ItemStack)
	 */
	public static UpgradeEventData runActions(RegistryObject<? extends UpgradeActionSerializer<?>> actionEntry, UpgradeEventData.Builder builder, ItemStack stack) {
		UpgradeEventData data = builder.build(actionEntry.get().getProvidedData(), stack);
		runActions(actionEntry.getId(), data, stack);
		return data;
	}
	
	/**
	 * Runs an action, automatically getting the <b>REQUIRED</b> ItemStack from the event data passed in<br>
	 * <br>
	 * NOTE: If a valid {@linkplain EquipmentSlot} is provided in the data, the upgrade will verify that against the data.
	 * @param actionEntry A {@linkplain RegistryObject} for the {@linkplain UpgradeActionSerializer} of the action to run
	 * @param data A pre-existing {@linkplain UpgradeEventData} with an owner item set
	 * @see #runActions(ResourceLocation, UpgradeEventData, ItemStack)
	 */
	public static void runActions(RegistryObject<? extends UpgradeActionSerializer<?>> actionEntry, UpgradeEventData data) {
		ItemStack stack = data.getOwnerItem();
		runActions(actionEntry.getId(), data, stack);
	}
	
//	/**
//	 * Runs an action on the given item with the given data<br>
//	 * <br>
//	 * NOTE: If a valid {@linkplain EquipmentSlot} is provided in the data, the upgrade will verify that against the data.
//	 * @param actionEntry A {@linkplain RegistryObject} for the {@linkplain UpgradeActionSerializer} of the action to run
//	 * @param data An {@linkplain UpgradeEventData} containing the data that will be provided.
//	 * @param stack The {@linkplain ItemStack} to run the upgrade on
//	 */
//	public static void runActions(RegistryObject<? extends UpgradeActionSerializer<?>> actionEntry, UpgradeEventData data, ItemStack stack) throws NoSuchElementException {
//		runActions(actionEntry.getId(), data, stack);
//	}
	
	/**
	 * Runs an action, automatically getting the <b>REQUIRED</b> ItemStack from the event data passed in<br>
	 * <br>
	 * NOTE: If a valid {@linkplain EquipmentSlot} is provided in the data, the upgrade will verify that against the data.
	 * @param actionId The {@linkplain ResourceLocation} ID of the action to run (<i>Recommended to use RegistryObject.getId()</i>)
	 * @param data An {@linkplain UpgradeEventData} which requires at least the ITEM entry to be present
	 * @throws NoSuchElementException If no {@linkplain UpgradeEntry#ITEM} entry was present
	 * @see #runActions(RegistryObject, UpgradeEventData, ItemStack)
	 */
	public static void runActions(ResourceLocation actionId, UpgradeEventData data) throws NoSuchElementException {
		ItemStack stack = data.getOptional(UpgradeEntry.ITEM).orElseThrow(() -> new NoSuchElementException("Missing Item Entry"));
		runActions(actionId, data, stack);
	}
	
	/**
	 * Runs an action on the given item with the given data<br>
	 * <br>
	 * NOTE: If a valid {@linkplain EquipmentSlot} is provided in the data, the upgrade will verify that against the data.
	 * @param actionId The {@linkplain ResourceLocation} ID of the action to run (<i>Recommended to use RegistryObject.getId()</i>)
	 * @param data An {@linkplain UpgradeEventData} containing the data that will be provided.
	 * @param stack The {@linkplain ItemStack} to run the upgrade on
	 * @see #runActions(RegistryObject, UpgradeEventData)
	 */
	public static void runActions(ResourceLocation actionId, UpgradeEventData data, ItemStack stack) {
		if (stack == null || stack.isEmpty()) return; //do nothing if no item is present
		ItemUpgrade upgrade = getUpgrade(stack); //get upgrade on item
		if (upgrade == null) return; //stop if no upgrade on item
		if (!upgrade.isValidItem(stack)) {removeUpgradeNoUpdate(stack); return;} //stop if item upgrade is invalid and remove it
		EquipmentSlot slot = data.getEntryOrNull(UpgradeEntry.SLOT); //get event slot
		if (slot != null && !upgrade.isValidSlot(slot)) return; //stop if the event slot is invalid
		if (!upgrade.hasAction(actionId)) return;
		
		upgrade.getActions(actionId).forEach(action -> {
			if (action.isValidSlot(slot)) {
				if (action.getSerializer().getProvidedData().verify(data.getEntrySet())) {
					action.run(data);
				} else LOGGER.error("Upgrade data failed entry set verification: " + actionId);
			}
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
	 * @param upgradeId The {@linkplain ResourceLocation} corresponding to the {@linkplain ItemUpgrade} to apply to the stack
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
			if (tag.isEmpty()) stack.setTag(null);
			else stack.setTag(tag);
		}
		return stack;
	}
	
	/**
	 * Removes the upgrade from a single item in an ItemStack if present, splitting off the new item if multiple are in one stack
	 * @param player The {@linkplain Player} to give the split item to
	 * @param stack The {@linkplain ItemStack} to remove the upgrade from 
	 * @return If the stack was not split
	 */
	public static boolean removeUpgradeFromStack(Player player, ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (!hasUpgrade(stack)) return false;
		if (stack.getCount() == 1) {
			removeUpgrade(stack);
			return true;
		} else {
			ItemStack stack1 = stack.copy();
			stack1.setCount(1);
			removeUpgrade(stack1);
			stack.shrink(1);
			if (!player.addItem(stack1)) player.drop(stack1, false);
			return false;
		}
	}
	
	/**
	 * Removes the upgrade from the ItemStack if present WITHOUT calling any upgrade update events<br>
	 * Mainly used to invalidate any upgrades that have found themselves somewhere they don't belong
	 * @param stack The {@linkplain ItemStack} to remove the upgrade from
	 * @return The stack with no upgrade applied
	 * @see #removeUpgrade(ItemStack) removeUpgrade(ItemStack) for most use cases
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
	
	/* A few stupid upgrade manager interactions */
	
	/**
	 * Checks if the given upgrade id has an associated upgrade
	 * @param id The {@linkplain ResourceLocation} of the upgrade to check for
	 * @return If an associated {@linkplain ItemUpgrade} is present
	 */
	public static boolean managerHasUpgrade(ResourceLocation id) {
		return ItemUpgradeManager.INSTANCE.getUpgrade(id) == null;
	}
	
	/**
	 * Gets a datapack-initialized upgrade by id if it exists
	 * @param id The {@linkplain ResourceLocation} of the upgrade to get
	 * @return The {@linkplain ItemUpgrade} with that id (or {@code null} if it doesn't exist)
	 */
	@Nullable
	public static ItemUpgrade managerGetUpgrade(ResourceLocation id) {
		return ItemUpgradeManager.INSTANCE.getUpgrade(id);
	}
	
	/* Categories */
	
	/**
	 * Gets an entry category by id
	 * @param id The {@linkplain ResourceLocation} of the category to get
	 * @return The {@linkplain UpgradeEntry} with the given id if present or <code>null</code> if not
	 */
	@Nullable
	public static EntryCategory<?> getCategory(ResourceLocation id) {
		return EntryCategoryManager.getCategory(id);
	}
	
}