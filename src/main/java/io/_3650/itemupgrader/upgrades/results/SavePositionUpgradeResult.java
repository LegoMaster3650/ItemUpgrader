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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SavePositionUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final String tagName;
	private final UpgradeEntry<Vec3> posEntry;
	private final boolean dimension;
	
	public SavePositionUpgradeResult(IUpgradeInternals internals, UpgradeEntry<ItemStack> itemEntry, String tagName, UpgradeEntry<Vec3> posEntry, boolean dimension) {
		super(internals, UpgradeEntrySet.POSITION.fillCategories(mapper -> {
			mapper.set(EntryCategory.ITEM, itemEntry).set(EntryCategory.POSITION, posEntry);
		}));
		this.itemEntry = itemEntry;
		this.tagName = tagName;
		this.posEntry = posEntry;
		this.dimension = dimension;
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		ItemStack stack = data.getEntry(this.itemEntry);
		CompoundTag tag = stack.getOrCreateTag();
		Vec3 pos = data.getEntry(this.posEntry);
		CompoundTag posTag = new CompoundTag();
		posTag.putDouble("x", pos.x());
		posTag.putDouble("y", pos.y());
		posTag.putDouble("z", pos.z());
		if (this.dimension) data.getOptional(UpgradeEntry.LEVEL).ifPresent(level -> posTag.putString("dim", level.dimension().location().toString()));
		tag.put(this.tagName, posTag);
		stack.setTag(tag);
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {new TranslatableComponent(ComponentHelper.entryFormat(this.posEntry)), new TextComponent(this.tagName)};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<SavePositionUpgradeResult> {
		
		@Override
		public SavePositionUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			String tagName = GsonHelper.getAsString(json, "tag");
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromJson(json, "value");
			boolean dimension = GsonHelper.getAsBoolean(json, "dimension", true);
			return new SavePositionUpgradeResult(internals, itemEntry, tagName, posEntry, dimension);
		}
		
		@Override
		public void toNetwork(SavePositionUpgradeResult result, FriendlyByteBuf buf) {
			result.itemEntry.toNetwork(buf);
			buf.writeUtf(result.tagName);
			result.posEntry.toNetwork(buf);
			buf.writeBoolean(result.dimension);
		}
		
		@Override
		public SavePositionUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			String tagName = buf.readUtf();
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromNetwork(buf);
			boolean dimension = buf.readBoolean();
			return new SavePositionUpgradeResult(internals, itemEntry, tagName, posEntry, dimension);
		}
		
	}
	
}