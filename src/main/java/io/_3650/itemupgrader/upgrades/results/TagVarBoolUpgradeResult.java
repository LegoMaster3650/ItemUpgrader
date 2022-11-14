package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class TagVarBoolUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final String tagName;
	private final boolean value;
	
	public TagVarBoolUpgradeResult(
			IUpgradeInternals internals,
			UpgradeEntry<ItemStack> itemEntry,
			String tagName,
			boolean value) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(itemEntry);
		}));
		this.itemEntry = itemEntry;
		this.tagName = tagName;
		this.value = value;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		ItemStack stack = data.getEntry(this.itemEntry);
		CompoundTag tag = stack.getOrCreateTag();
		tag.putBoolean(this.tagName, this.value);
		stack.setTag(tag);
		return true;
	}
	
	private final Serializer instance = new Serializer();
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[]{new TextComponent(this.tagName), new TextComponent("" + this.value)};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<TagVarBoolUpgradeResult> {
		
		@Override
		public TagVarBoolUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			String tagName = GsonHelper.getAsString(json, "tag");
			boolean value = GsonHelper.getAsBoolean(json, "value");
			return new TagVarBoolUpgradeResult(internals, itemEntry, tagName, value);
		}
		
		@Override
		public void toNetwork(TagVarBoolUpgradeResult result, FriendlyByteBuf buf) {
			result.itemEntry.toNetwork(buf);
			buf.writeUtf(result.tagName);
			buf.writeBoolean(result.value);
		}
		
		@Override
		public TagVarBoolUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			String tagName = buf.readUtf();
			boolean value = buf.readBoolean();
			return new TagVarBoolUpgradeResult(internals, itemEntry, tagName, value);
		}
		
	}
	
}
