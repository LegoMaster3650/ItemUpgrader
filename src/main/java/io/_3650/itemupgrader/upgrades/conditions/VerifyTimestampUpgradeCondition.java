package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VerifyTimestampUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final String tagName;
	private final int ticks;
	
	public VerifyTimestampUpgradeCondition(IUpgradeInternals internals, boolean inverted, UpgradeEntry<ItemStack> itemEntry, String tagName, int ticks) {
		super(internals, inverted, UpgradeEntrySet.LEVEL.fillCategories(mapper -> {
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.itemEntry = itemEntry;
		this.tagName = tagName;
		this.ticks = ticks;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		Level level = data.getEntry(UpgradeEntry.LEVEL);
		ItemStack stack = data.getEntry(this.itemEntry);
		if (stack.isEmpty() || !stack.hasTag()) return false;
		CompoundTag tag = stack.getTag();
		if (tag.contains(this.tagName, CompoundTag.TAG_LONG)) return false;
		long oldTime = tag.getLong(this.tagName);
		return level.getDayTime() == oldTime + this.ticks;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent("" + this.ticks));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<VerifyTimestampUpgradeCondition> {
		
		@Override
		public VerifyTimestampUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			String tagName = GsonHelper.getAsString(json, "tag", "UpgradeTimestamp");
			int ticks = GsonHelper.getAsInt(json, "ticks");
			return new VerifyTimestampUpgradeCondition(internals, inverted, itemEntry, tagName, ticks);
		}
		
		@Override
		public void toNetwork(VerifyTimestampUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.itemEntry.toNetwork(buf);
			buf.writeUtf(condition.tagName);
			buf.writeInt(condition.ticks);
		}
		
		@Override
		public VerifyTimestampUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			String tagName = buf.readUtf();
			int ticks = buf.readInt();
			return new VerifyTimestampUpgradeCondition(internals, inverted, itemEntry, tagName, ticks);
		}
		
	}
	
}