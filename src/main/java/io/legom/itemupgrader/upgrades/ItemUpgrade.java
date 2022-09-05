package io.legom.itemupgrader.upgrades;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import io.legom.itemupgrader.ItemUpgrader;
import io.legom.itemupgrader.api.serializer.UpgradeActionSerializer;
import io.legom.itemupgrader.api.type.UpgradeAction;
import io.legom.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemUpgrade {
	
	private final ResourceLocation id;
	private final Ingredient base;
	private final Set<EquipmentSlot> validSlots;
	private final Set<ResourceLocation> validActions;
	private final ListMultimap<ResourceLocation, UpgradeAction> actions;
	private final boolean visible;
	private final int descriptionLines;
	private final TextColor color;
	
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
	
	public ResourceLocation getId() {
		return this.id;
	}
	
	public boolean isValidItem(ItemStack stack) {
		return this.base.test(stack);
	}
	
	private List<ItemStack> validItemsCache;
	public List<ItemStack> getValidItems() {
		if (this.validItemsCache == null) {
			this.validItemsCache = ForgeRegistries.ITEMS.getValues().stream().map(ItemStack::new).filter(this::isValidItem).toList(); //I hate this but it's the only way
		}
		return this.validItemsCache;
	}
	
	public Set<EquipmentSlot> getValidSlots() {
		return this.validSlots;
	}
	
	public Set<ResourceLocation> getValidActions() {
		return this.validActions;
	}
	
	public boolean isValidSlot(EquipmentSlot slot) {
		return this.validSlots.contains(slot);
	}
	
	public boolean hasAction(ResourceLocation actionId) {
		return this.validActions.contains(actionId);
	}
	
	public List<UpgradeAction> getActions(ResourceLocation actionId) {
		return this.actions.get(actionId);
	}
	
	public void forEachAction(BiConsumer<ResourceLocation, UpgradeAction> consumer) {
		for (ResourceLocation actionId : this.getValidActions()) {
			for (UpgradeAction action : this.getActions(actionId)) {
				consumer.accept(actionId, action);
			}
		}
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public boolean hasDescription() {
		return this.descriptionLines > 0;
	}
	
	public int getDescriptionLines() {
		return this.descriptionLines;
	}
	
	public TextColor getColor() {
		return this.color;
	}
	
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
				action.getInternals().to(buf);
				action.hackyToNetworkReadJavadoc(buf);
			}
		}
		//is visible
		buf.writeBoolean(this.visible);
		//description lines
		buf.writeInt(this.descriptionLines);
		//color
		buf.writeInt(this.color.getValue());
	}
	
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
			ResourceLocation netActionId = buf.readResourceLocation();
			UpgradeActionSerializer<?> serializer = ItemUpgrader.ACTION_REGISTRY.get().getValue(netActionId);
			//actions of type
			int netActionsCount = buf.readInt();
			for (var j = 0; j < netActionsCount; j++) {
				IUpgradeInternals internals = IUpgradeInternals.of(netActionId, buf);
				UpgradeAction netAction = serializer.fromNetwork(internals, buf);
				netActions.put(netActionId, netAction);
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