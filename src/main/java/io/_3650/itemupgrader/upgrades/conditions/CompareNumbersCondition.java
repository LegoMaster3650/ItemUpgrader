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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class CompareNumbersCondition extends UpgradeCondition {
	
	private final CompareTarget target;
	private final CompareOperation op;
	private final UpgradeEntry<Integer> intEntry;
	private final UpgradeEntry<Float> floatEntry;
	private final int intValue;
	private final float floatValue;
	
	public CompareNumbersCondition(
			IUpgradeInternals internals,
			boolean inverted,
			CompareTarget target,
			CompareOperation op,
			UpgradeEntry<Integer> intEntry,
			UpgradeEntry<Float> floatEntry,
			int intValue,
			float floatValue) {
		super(internals, inverted, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			switch (target) {
			default:
			case INTEGER:
				mapper.set(EntryCategory.INT_VALUE, intEntry);
			case FLOAT:
				mapper.set(EntryCategory.FLOAT_VALUE, floatEntry);
			}
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
			return ComponentHelper.arrayify(new TextComponent(this.intEntry.getId() + " " + this.op.getName() + " " + this.intValue));
		case FLOAT:
			return ComponentHelper.arrayify(new TextComponent(this.floatEntry.getId() + " " + this.op.getName() + " " + this.floatValue));
		}
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<CompareNumbersCondition> {
		
		@Override
		public CompareNumbersCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			CompareTarget target = CompareTarget.INTEGER;
			CompareOperation op = CompareOperation.byName(GsonHelper.getAsString(json, "operation", CompareOperation.EQ.getName()));
			UpgradeEntry<Integer> intEntry;
			UpgradeEntry<Float> floatEntry;
			int intValue;
			float floatValue;
			if (EntryCategory.INT_VALUE.jsonHasValue(json, "target")) {
				intEntry = EntryCategory.INT_VALUE.fromJson(json, "target");
				floatEntry = EntryCategory.FLOAT_VALUE.getDefaultValue();
				intValue = GsonHelper.getAsInt(json, "value");
				floatValue = 0.0F;
			} else {
				target = CompareTarget.FLOAT;
				intEntry = EntryCategory.INT_VALUE.getDefaultValue();
				floatEntry = EntryCategory.FLOAT_VALUE.fromJson(json, "target");
				intValue = 0;
				floatValue = GsonHelper.getAsFloat(json, "value");
			}
			return new CompareNumbersCondition(internals, inverted, target, op, intEntry, floatEntry, intValue, floatValue);
		}
		
		@Override
		public void toNetwork(CompareNumbersCondition condition, FriendlyByteBuf buf) {
			buf.writeEnum(condition.target);
			buf.writeEnum(condition.op);
			condition.intEntry.toNetwork(buf);
			condition.floatEntry.toNetwork(buf);
			buf.writeInt(condition.intValue);
			buf.writeFloat(condition.floatValue);
		}
		
		@Override
		public CompareNumbersCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			CompareTarget target = buf.readEnum(CompareTarget.class);
			CompareOperation op = buf.readEnum(CompareOperation.class);
			UpgradeEntry<Integer> intEntry = EntryCategory.INT_VALUE.fromNetwork(buf);
			UpgradeEntry<Float> floatEntry = EntryCategory.FLOAT_VALUE.fromNetwork(buf);
			int intValue = buf.readInt();
			float floatValue = buf.readFloat();
			return new CompareNumbersCondition(internals, inverted, target, op, intEntry, floatEntry, intValue, floatValue);
		}
		
	}
	
	private static enum CompareTarget {
		INTEGER,
		FLOAT;
	}
	
	private static enum CompareOperation {
		EQ("="),
		NE("!="),
		GT(">"),
		LT("<"),
		GE(">="),
		LE("<=");
		
		private final String name;
		
		private CompareOperation(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public static CompareOperation byName(String nameIn) {
			for (var value : values()) {
				if (value.name.equals(nameIn)) return value;
			}
			return EQ;
		}
	}
	
}