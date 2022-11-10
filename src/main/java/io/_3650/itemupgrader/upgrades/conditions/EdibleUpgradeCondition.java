package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class EdibleUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	
	public EdibleUpgradeCondition(IUpgradeInternals internals, boolean inverted, UpgradeEntry<ItemStack> itemEntry) {
		super(internals, inverted, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.itemEntry = itemEntry;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		return data.getEntry(this.itemEntry).isEdible();
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.empty();
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<EdibleUpgradeCondition> {
		
		@Override
		public EdibleUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			return new EdibleUpgradeCondition(internals, inverted, itemEntry);
		}
		
		@Override
		public void toNetwork(EdibleUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.itemEntry.toNetwork(buf);
		}
		
		@Override
		public EdibleUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			return new EdibleUpgradeCondition(internals, inverted, itemEntry);
		}
		
	}
	
}