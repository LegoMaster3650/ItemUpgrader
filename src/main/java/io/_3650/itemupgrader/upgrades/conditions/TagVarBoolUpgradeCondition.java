package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class TagVarBoolUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final String tagName;
	private final boolean value;
	
	public TagVarBoolUpgradeCondition(
			IUpgradeInternals internals,
			boolean inverted,
			UpgradeEntry<ItemStack> itemEntry,
			String tagName,
			boolean value) {
		super(internals, inverted, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.itemEntry = itemEntry;
		this.tagName = tagName;
		this.value = value;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		ItemStack stack = data.getEntry(this.itemEntry);
		if (!stack.hasTag()) return false;
		else {
			CompoundTag tag = stack.getTag();
			if (!tag.contains(this.tagName, CompoundTag.TAG_BYTE)) return !this.value; //false == value
			else {
				boolean tagVal = tag.getBoolean(this.tagName);
				return tagVal == this.value;
			}
		}
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
	
	public static class Serializer extends UpgradeConditionSerializer<TagVarBoolUpgradeCondition> {
		
		@Override
		public TagVarBoolUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			String tagName = GsonHelper.getAsString(json, "tag");
			boolean value = GsonHelper.getAsBoolean(json, "value", true);
			return new TagVarBoolUpgradeCondition(internals, inverted, itemEntry, tagName, value);
		}
		
		@Override
		public void toNetwork(TagVarBoolUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.itemEntry.toNetwork(buf);
			buf.writeUtf(condition.tagName);
			buf.writeBoolean(condition.value);
		}
		
		@Override
		public TagVarBoolUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			String tagName = buf.readUtf();
			boolean value = buf.readBoolean();
			return new TagVarBoolUpgradeCondition(internals, inverted, itemEntry, tagName, value);
		}
		
	}
	
}
