package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.registry.types.OperationValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class TagVarIntUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final OperationValue op;
	private final String tagName;
	private final int value;
	
	public TagVarIntUpgradeCondition(
			IUpgradeInternals internals,
			boolean inverted,
			UpgradeEntry<ItemStack> itemEntry,
			OperationValue op,
			String tagName,
			int value) {
		super(internals, inverted, UpgradeEntrySet.create(builder -> {
			builder.require(itemEntry);
		}));
		this.itemEntry = itemEntry;
		this.op = op;
		this.tagName = tagName;
		this.value = value;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		ItemStack stack = data.getEntry(this.itemEntry);
		if (!stack.hasTag()) return false;
		else {
			CompoundTag tag = stack.getTag();
			if (!tag.contains(this.tagName, CompoundTag.TAG_INT)) return 0 == this.value;
			else {
				int tagVal = tag.getInt(this.tagName);
				switch (this.op) {
				case EQ:
					return tagVal == this.value;
				case NE:
					return tagVal != this.value;
				case GT:
					return tagVal > this.value;
				case LT:
					return tagVal < this.value;
				case GE:
					return tagVal >= this.value;
				case LE:
					return tagVal <= this.value;
				default:
					return false;
				}
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
		return new MutableComponent[]{new TextComponent(this.tagName), new TextComponent(this.op.getName()), new TextComponent(Integer.toString(this.value))};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<TagVarIntUpgradeCondition> {
		
		@Override
		public TagVarIntUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			OperationValue op = OperationValue.byName(GsonHelper.getAsString(json, "operation", OperationValue.EQ.getName()));
			String tagName = GsonHelper.getAsString(json, "tag");
			int value = GsonHelper.getAsInt(json, "value");
			return new TagVarIntUpgradeCondition(internals, false, itemEntry, op, tagName, value);
		}
		
		@Override
		public void toNetwork(TagVarIntUpgradeCondition condition, FriendlyByteBuf buf) {
			condition.itemEntry.toNetwork(buf);
			buf.writeEnum(condition.op);
			buf.writeUtf(condition.tagName);
			buf.writeInt(condition.value);
		}
		
		@Override
		public TagVarIntUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			OperationValue op = buf.readEnum(OperationValue.class);
			String tagName = buf.readUtf();
			int value = buf.readInt();
			return new TagVarIntUpgradeCondition(internals, inverted, itemEntry, op, tagName, value);
		}
		
	}
	
}
