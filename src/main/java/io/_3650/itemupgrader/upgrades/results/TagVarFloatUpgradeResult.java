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

public class TagVarFloatUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final String tagName;
	private final ValueModifier modifier;
	private final float value;
	
	public TagVarFloatUpgradeResult(
			IUpgradeInternals internals,
			UpgradeEntry<ItemStack> itemEntry,
			String tagName,
			ValueModifier modifier,
			float value) {
		super(internals, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.itemEntry = itemEntry;
		this.tagName = tagName;
		this.modifier = modifier;
		this.value = value;
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		ItemStack stack = data.getEntry(this.itemEntry);
		CompoundTag tag = stack.getOrCreateTag();
		switch (this.modifier) {
		default:
		case SET:
			tag.putFloat(this.tagName, this.value);
			break;
		case ADD:
			if (tag.contains(this.tagName, CompoundTag.TAG_INT)) tag.putFloat(this.tagName, tag.getInt(this.tagName) + this.value);
			else tag.putFloat(this.tagName, this.value);
			break;
		case SUB:
			if (tag.contains(this.tagName, CompoundTag.TAG_INT)) tag.putFloat(this.tagName, tag.getInt(this.tagName) - this.value);
			else tag.putFloat(this.tagName, -this.value);
			break;
		}
		stack.setTag(tag);
	}
	
	private final Serializer instance = new Serializer();
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[]{new TextComponent(this.tagName), new TextComponent(this.modifier.getName()), new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.value))};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<TagVarFloatUpgradeResult> {
		
		@Override
		public TagVarFloatUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			String tagName = GsonHelper.getAsString(json, "tag");
			ValueModifier modifier = ValueModifier.byName(GsonHelper.getAsString(json, "operation", ValueModifier.SET.getName()));
			float value = GsonHelper.getAsFloat(json, "value");
			return new TagVarFloatUpgradeResult(internals, itemEntry, tagName, modifier, value);
		}
		
		@Override
		public void toNetwork(TagVarFloatUpgradeResult result, FriendlyByteBuf buf) {
			result.itemEntry.toNetwork(buf);
			buf.writeUtf(result.tagName);
			buf.writeEnum(result.modifier);
			buf.writeFloat(result.value);
		}
		
		@Override
		public TagVarFloatUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			String tagName = buf.readUtf();
			ValueModifier modifier = buf.readEnum(ValueModifier.class);
			float value = buf.readFloat();
			return new TagVarFloatUpgradeResult(internals, itemEntry, tagName, modifier, value);
		}
		
	}
	
	private static enum ValueModifier {
		SET("="),
		ADD("+"),
		SUB("-");
		
		private final String name;
		
		private ValueModifier(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public static ValueModifier byName(String name) {
			for (var value : values()) {
				if (value.name.equals(name)) return value;
			}
			return SET;
		}
		
	}
	
}
