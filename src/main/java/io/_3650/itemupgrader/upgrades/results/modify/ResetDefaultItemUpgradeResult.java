package io._3650.itemupgrader.upgrades.results.modify;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

public class ResetDefaultItemUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	
	public ResetDefaultItemUpgradeResult(IUpgradeInternals internals, UpgradeEntry<ItemStack> itemEntry) {
		super(internals, UpgradeEntrySet.SLOT.fillCategories(mapper -> {
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.itemEntry = itemEntry;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		data.forceModifyEntry(this.itemEntry, data.getOwnerItem());
		return true;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {new TranslatableComponent(this.itemEntry.getDescriptionId())};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<ResetDefaultItemUpgradeResult> {
		
		@Override
		public ResetDefaultItemUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			return new ResetDefaultItemUpgradeResult(internals, itemEntry);
		}
		
		@Override
		public void toNetwork(ResetDefaultItemUpgradeResult result, FriendlyByteBuf buf) {
			result.itemEntry.toNetwork(buf);
		}
		
		@Override
		public ResetDefaultItemUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			return new ResetDefaultItemUpgradeResult(internals, itemEntry);
		}
		
	}
	
}