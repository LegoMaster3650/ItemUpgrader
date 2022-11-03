package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SaveTimestampUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final String tagName;
	
	public SaveTimestampUpgradeResult(IUpgradeInternals internals, UpgradeEntry<ItemStack> itemEntry, String tagName) {
		super(internals, UpgradeEntrySet.LEVEL.fillCategories(mapper -> {
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.itemEntry = itemEntry;
		this.tagName = tagName;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Level level = data.getEntry(UpgradeEntry.LEVEL);
		if (level.isClientSide) return false;
		ItemStack stack = data.getEntry(this.itemEntry);
		if (stack.isEmpty()) return false;
		CompoundTag tag = stack.getOrCreateTag();
		tag.putLong(this.tagName, level.getDayTime());
		return true;
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
	
	public static class Serializer extends UpgradeResultSerializer<SaveTimestampUpgradeResult> {
		
		@Override
		public SaveTimestampUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			String tagName = GsonHelper.getAsString(json, "tag", "UpgradeTimestamp");
			return new SaveTimestampUpgradeResult(internals, itemEntry, tagName);
		}
		
		@Override
		public void toNetwork(SaveTimestampUpgradeResult result, FriendlyByteBuf buf) {
			
		}
		
		@Override
		public SaveTimestampUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			String tagName = buf.readUtf();
			return new SaveTimestampUpgradeResult(internals, itemEntry, tagName);
		}
		
	}
	
}