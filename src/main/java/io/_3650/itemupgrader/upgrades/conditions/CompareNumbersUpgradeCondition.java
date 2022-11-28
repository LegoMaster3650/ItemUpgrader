package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.upgrades.data.NumberType;
import io._3650.itemupgrader.upgrades.data.OperationValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class CompareNumbersUpgradeCondition extends UpgradeCondition {
	
	private final NumberType target;
	private final OperationValue op;
	private final UpgradeEntry<Integer> intEntry;
	private final UpgradeEntry<Float> floatEntry;
	private final int intValue;
	private final float floatValue;
	
	public CompareNumbersUpgradeCondition(
			IUpgradeInternals internals,
			boolean inverted,
			NumberType target,
			OperationValue op,
			UpgradeEntry<Integer> intEntry,
			UpgradeEntry<Float> floatEntry,
			int intValue,
			float floatValue) {
		super(internals, inverted, UpgradeEntrySet.create(builder -> {
			if (target == NumberType.INTEGER) builder.require(intEntry);
			if (target == NumberType.FLOAT) builder.require(floatEntry);
		}));
		this.target = target;
		this.op = op;
		this.intEntry = intEntry;
		this.floatEntry = floatEntry;
		this.intValue = intValue;
		this.floatValue = floatValue;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		switch (this.target) {
		default:
			return false;
		case INTEGER:
			int targetInt = data.getEntry(this.intEntry);
			switch (this.op) {
			default:
				return false;
			case EQ:
				return targetInt == this.intValue;
			case NE:
				return targetInt != this.intValue;
			case GT:
				return targetInt > this.intValue;
			case LT:
				return targetInt < this.intValue;
			case GE:
				return targetInt >= this.intValue;
			case LE:
				return targetInt <= this.intValue;
			}
		case FLOAT:
			float targetFloat = data.getEntry(this.floatEntry);
			switch (this.op) {
			default:
				return false;
			case EQ:
				return targetFloat == this.floatValue;
			case NE:
				return targetFloat != this.floatValue;
			case GT:
				return targetFloat > this.floatValue;
			case LT:
				return targetFloat < this.floatValue;
			case GE:
				return targetFloat >= this.floatValue;
			case LE:
				return targetFloat <= this.floatValue;
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
		switch (this.target) {
		default:
			return ComponentHelper.arrayify(new TranslatableComponent("tooltip.itemupgrader.error"));
		case INTEGER:
			return new MutableComponent[] {new TranslatableComponent(this.intEntry.getDescriptionId()), new TextComponent(this.op.getName()), new TextComponent("" + this.intValue)};
		case FLOAT:
			return new MutableComponent[] {new TranslatableComponent(this.floatEntry.getDescriptionId()), new TextComponent(this.op.getName()), new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.floatValue))};
		}
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<CompareNumbersUpgradeCondition> {
		
		@Override
		public CompareNumbersUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			NumberType target = NumberType.INTEGER;
			OperationValue op = OperationValue.byName(GsonHelper.getAsString(json, "operation", OperationValue.EQ.getName()));
			UpgradeEntry<Integer> intEntry;
			UpgradeEntry<Float> floatEntry;
			int intValue;
			float floatValue;
			if (EntryCategory.FLOAT_VALUE.jsonHasValue(json, "target")) {
				//use if float value is present
				target = NumberType.FLOAT;
				intEntry = EntryCategory.INT_VALUE.getDefaultValue();
				floatEntry = EntryCategory.FLOAT_VALUE.fromJson(json, "target");
				intValue = 0;
				floatValue = GsonHelper.getAsFloat(json, "value");
			} else {
				//otherwise default to int
				intEntry = EntryCategory.INT_VALUE.fromJson(json, "target");
				floatEntry = EntryCategory.FLOAT_VALUE.getDefaultValue();
				intValue = GsonHelper.getAsInt(json, "value");
				floatValue = 0.0F;
			}
			return new CompareNumbersUpgradeCondition(internals, false, target, op, intEntry, floatEntry, intValue, floatValue);
		}
		
		@Override
		public void toNetwork(CompareNumbersUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeEnum(condition.target);
			buf.writeEnum(condition.op);
			condition.intEntry.toNetwork(buf);
			condition.floatEntry.toNetwork(buf);
			buf.writeInt(condition.intValue);
			buf.writeFloat(condition.floatValue);
		}
		
		@Override
		public CompareNumbersUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			NumberType target = buf.readEnum(NumberType.class);
			OperationValue op = buf.readEnum(OperationValue.class);
			UpgradeEntry<Integer> intEntry = EntryCategory.INT_VALUE.fromNetwork(buf);
			UpgradeEntry<Float> floatEntry = EntryCategory.FLOAT_VALUE.fromNetwork(buf);
			int intValue = buf.readInt();
			float floatValue = buf.readFloat();
			return new CompareNumbersUpgradeCondition(internals, inverted, target, op, intEntry, floatEntry, intValue, floatValue);
		}
		
	}
	
}