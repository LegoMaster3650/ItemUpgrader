package io._3650.itemupgrader.api;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.util.UpgradeSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Class representing a singular upgrade instance loaded from a datapack.
 * @author LegoMaster3650
 */
public class ItemUpgrade {
	
	private final ResourceLocation id;
	private final Ingredient base;
	private final Set<EquipmentSlot> validSlots;
	private final Set<ResourceLocation> validActions;
	private final ListMultimap<ResourceLocation, UpgradeAction> actions;
	private final boolean visible;
	private final int descriptionLines;
	private final TextColor color;
	
	/**
	 * Constructs an ItemUpgrade instance (you won't need to use this)
	 * @param upgradeId The {@linkplain ResourceLocation} that identifies this upgrade
	 * @param base The {@linkplain Ingredient} that defines which items are valid for this upgrade
	 * @param validSlots The {@linkplain Set} of {@linkplain EquipmentSlot}s that are valid for this upgrade
	 * @param actions The {@linkplain List} of {@linkplain UpgradeAction}s this upgrade runs
	 * @param visible Whether the upgrade is visible
	 * @param descriptionLines The upgrade's description line count
	 * @param color The upgrade's {@linkplain TextColor}
	 */
	public ItemUpgrade(ResourceLocation upgradeId, Ingredient base, Set<EquipmentSlot> validSlots, ListMultimap<ResourceLocation, UpgradeAction> actions, boolean visible, int descriptionLines, TextColor color) {
		this.id = upgradeId;
		this.base = base;
		this.validSlots = ImmutableSet.copyOf(validSlots);
		this.validActions = ImmutableSet.copyOf(actions.keySet());
		this.actions = ImmutableListMultimap.copyOf(actions);
		this.visible = visible;
		this.descriptionLines = descriptionLines;
		this.color = color;
	}
	
	/**
	 * Gets the upgrade's ID
	 * @return The {@linkplain ResourceLocation} that identifies this upgrade
	 */
	public ResourceLocation getId() {
		return this.id;
	}
	
	/**
	 * Tests if the given item is valid for this upgrade
	 * @param stack The {@linkplain ItemStack} to test
	 * @return If the stack is valid
	 */
	public boolean isValidItem(ItemStack stack) {
		return this.base.test(stack);
	}
	
	private List<ItemStack> validItemsCache;
	/**
	 * Gets a complete list of valid items<br>
	 * <b><u>WARNING: THIS LOOPS THROUGH EVERY ITEM IN THE GAME. ONLY USE THIS IF YOU HAVE TO</u></b>
	 * @return A {@linkplain List} of {@linkplain ItemStack}s of every registered item that is valid
	 * @see #isValidItem(ItemStack)
	 */
	public List<ItemStack> getValidItems() {
		if (this.validItemsCache == null) {
			this.validItemsCache = ForgeRegistries.ITEMS.getValues().stream().map(ItemStack::new).filter(this::isValidItem).toList(); //I hate this but it's the only way
		}
		return this.validItemsCache;
	}
	
	/**
	 * Gets the set of the valid slots for this upgrade
	 * @return The {@linkplain Set} of {@linkplain EquipmentSlot}s that are valid for this upgrade
	 * @see #isValidSlot(EquipmentSlot)
	 */
	public Set<EquipmentSlot> getValidSlots() {
		return this.validSlots;
	}
	
	/**
	 * Gets the set of action ids that are present in this upgrade
	 * @return The {@linkplain Set} of {@linkplain ResourceLocation} ids of every action present in this upgrade
	 * @see #hasAction(ResourceLocation)
	 */
	public Set<ResourceLocation> getValidActions() {
		return this.validActions;
	}
	
	/**
	 * Checks if the given slot is valid for this upgrade
	 * @param slot The {@linkplain EquipmentSlot} to test
	 * @return If the slot is valid
	 */
	public boolean isValidSlot(EquipmentSlot slot) {
		return this.validSlots.contains(slot);
	}
	
	/**
	 * Checks if the given action id is present in this upgrade
	 * @param actionId The {@linkplain ResourceLocation} id for the action to check for
	 * @return If the action is present
	 */
	public boolean hasAction(ResourceLocation actionId) {
		return this.validActions.contains(actionId);
	}
	
	/**
	 * Gets a list of actions present on this item for the given id
	 * @param actionId The {@linkplain ResourceLocation} id for the action type to get
	 * @return The {@linkplain List} of {@linkplain UpgradeAction}s this upgrade runs
	 */
	public List<UpgradeAction> getActions(ResourceLocation actionId) {
		return this.actions.get(actionId);
	}
	
	/**
	 * A utility function to quickly loop through every action in the upgrade
	 * @param consumer A {@linkplain BiConsumer} of the {@linkplain ResourceLocation} id for the action and the {@linkplain UpgradeAction} itself
	 */
	public void forEachAction(BiConsumer<ResourceLocation, UpgradeAction> consumer) {
		for (ResourceLocation actionId : this.getValidActions()) {
			for (UpgradeAction action : this.getActions(actionId)) {
				consumer.accept(actionId, action);
			}
		}
	}
	
	/**
	 * Gets whether this upgrade is marked as visible (intended for tooltips)
	 * @return Whether the upgrade is visible
	 */
	public boolean isVisible() {
		return this.visible;
	}
	
	/**
	 * Gets whether or not this upgrade is meant to display a description
	 * @return If the upgrade has a description
	 */
	public boolean hasDescription() {
		return this.descriptionLines > 0;
	}
	
	/**
	 * Gets the number of description lines this upgrade is meant to have (0 meaning no description)
	 * @return The upgrade's description line count
	 */
	public int getDescriptionLines() {
		return this.descriptionLines;
	}
	
	/**
	 * Gets the upgrade's text color for name/icon display in tooltips
	 * @return The upgrade's {@linkplain TextColor}
	 */
	public TextColor getColor() {
		return this.color;
	}
	
	/**
	 * Serializes this upgrade to the provided buffer
	 * @param buf The {@linkplain FriendlyByteBuf} to serialize this upgrade to
	 */
	public void toNetwork(FriendlyByteBuf buf) {
		//upgrade id
		buf.writeResourceLocation(this.id);
		//base items
		this.base.toNetwork(buf);
		//valid slots
		buf.writeInt(this.validSlots.size());
		for (var slot : this.validSlots) {
			buf.writeEnum(slot);
		}
		//actions
		buf.writeInt(this.validActions.size());
		for (var actionId : this.validActions) {
			List<UpgradeAction> actionsList = this.getActions(actionId);
			buf.writeResourceLocation(actionId);
			//actions of type
			buf.writeInt(actionsList.size());
			for (UpgradeAction action : actionsList) {
				UpgradeSerializer.actionToNetwork(action, buf);
			}
		}
		//is visible
		buf.writeBoolean(this.visible);
		//description lines
		buf.writeInt(this.descriptionLines);
		//color
		buf.writeInt(this.color.getValue());
	}
	
	/**
	 * Deserializes an upgrade from the provided buffer
	 * @param buf The {@linkplain FriendlyByteBuf} to deserialize the upgrade from
	 * @return The {@linkplain ItemUpgrade} from the buffer
	 */
	public static ItemUpgrade fromNetwork(FriendlyByteBuf buf) {
		//upgrade id
		ResourceLocation netId = buf.readResourceLocation();
		//base items
		Ingredient netBase = Ingredient.fromNetwork(buf);
		//valid slots
		int netValidSlotsCount = buf.readInt();
		LinkedHashSet<EquipmentSlot> netValidSlots = new LinkedHashSet<>(netValidSlotsCount);
		for (int i = 0; i < netValidSlotsCount; i++) {
			netValidSlots.add(buf.readEnum(EquipmentSlot.class));
		}
		//actions
		int netValidActionsCount = buf.readInt();
		ListMultimap<ResourceLocation, UpgradeAction> netActions = MultimapBuilder.hashKeys(netValidActionsCount).arrayListValues().build();
		for (int i = 0; i < netValidActionsCount; i++) {
			ResourceLocation actionId = buf.readResourceLocation();
			UpgradeActionSerializer<?> serializer = ItemUpgrader.ACTION_REGISTRY.get().getValue(actionId);
			//actions of type
			int actionsCount = buf.readInt();
			for (var j = 0; j < actionsCount; j++) {
				netActions.put(actionId, UpgradeSerializer.actionFromNetwork(actionId, serializer, buf));
			}
		}
		//is visible
		boolean netVisible = buf.readBoolean();
		//description lines
		int netDescriptionLines = buf.readInt();
		//color
		TextColor netColor = TextColor.fromRgb(buf.readInt());
		return new ItemUpgrade(netId, netBase, netValidSlots, netActions, netVisible, netDescriptionLines, netColor);
	}
	
}