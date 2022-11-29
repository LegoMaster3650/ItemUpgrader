package io._3650.itemupgrader.upgrades.results.modify;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.registry.types.NumberType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class MultiplyEntryUpgradeResult extends UpgradeResult {
	
	private final NumberType target;
	private final UpgradeEntry<Integer> intEntry;
	private final UpgradeEntry<Float> floatEntry;
	private final double amount;
	private final boolean doPercentFormat;
	private final boolean isNegative;
	
	public MultiplyEntryUpgradeResult(
			IUpgradeInternals internals, NumberType target,
			@Nullable UpgradeEntry<Integer> intEntry,
			@Nullable UpgradeEntry<Float> floatEntry,
			double amount, boolean doPercentFormat,
			boolean isNegative) {
		super(internals, UpgradeEntrySet.create(builder -> {
			if (target == NumberType.INTEGER) builder.modifiable(intEntry);
			if (target == NumberType.FLOAT) builder.modifiable(floatEntry);
		}));
		this.target = target;
		this.intEntry = intEntry;
		this.floatEntry = floatEntry;
		this.amount = amount;
		this.doPercentFormat = doPercentFormat;
		this.isNegative = isNegative;
	}
	
	@Override
	public ChatFormatting getColor() {
		return this.isNegative ? ChatFormatting.RED : ChatFormatting.BLUE;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		switch (this.target) {
		default:
			return false; //default should never happen
		case INTEGER:
			int targetInt = data.getEntry(this.intEntry);
			data.setModifiableEntry(this.intEntry, Double.valueOf((this.amount + 1.0D) * targetInt).intValue());
			return true;
		case FLOAT:
			float targetFloat = data.getEntry(this.floatEntry);
			data.setModifiableEntry(this.floatEntry, Double.valueOf((this.amount + 1.0D) * targetFloat).floatValue());
			return true;
		}
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		MutableComponent amountTooltip = new TextComponent(this.doPercentFormat ? ComponentHelper.SIGNED_PERCENT.format(this.amount) : "x" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.amount));
		MutableComponent entryTooltip = new TranslatableComponent(this.target == NumberType.FLOAT ? this.floatEntry.getDescriptionId() : this.intEntry.getDescriptionId());
		return new MutableComponent[] {amountTooltip, entryTooltip};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<MultiplyEntryUpgradeResult> {
		
		@Override
		public MultiplyEntryUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			NumberType target = NumberType.INTEGER;
			UpgradeEntry<Integer> intEntry;
			UpgradeEntry<Float> floatEntry;
			if (EntryCategory.FLOAT_VALUE.jsonHasValue(json, "entry")) {
				target = NumberType.FLOAT;
				intEntry = EntryCategory.INT_VALUE.getDefaultValue();
				floatEntry = EntryCategory.FLOAT_VALUE.fromJson(json, "entry");
			} else {
				intEntry = EntryCategory.INT_VALUE.fromJson(json, "entry");
				floatEntry = EntryCategory.FLOAT_VALUE.getDefaultValue();
			}
			double amount = GsonHelper.getAsDouble(json, "amount");
			boolean doPercentFormat = GsonHelper.getAsBoolean(json, "percent_tooltip", true);
			boolean isNegative = GsonHelper.getAsBoolean(json, "positive", true) ? amount < 0 : amount > 0;
			return new MultiplyEntryUpgradeResult(internals, target, intEntry, floatEntry, amount, doPercentFormat, isNegative);
		}
		
		@Override
		public void toNetwork(MultiplyEntryUpgradeResult result, FriendlyByteBuf buf) {
			buf.writeEnum(result.target);
			result.intEntry.toNetwork(buf);
			result.floatEntry.toNetwork(buf);
			buf.writeDouble(result.amount);
			buf.writeBoolean(result.doPercentFormat);
			buf.writeBoolean(result.isNegative);
		}
		
		@Override
		public MultiplyEntryUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			NumberType target = buf.readEnum(NumberType.class);
			UpgradeEntry<Integer> intEntry = EntryCategory.INT_VALUE.fromNetwork(buf);
			UpgradeEntry<Float> floatEntry = EntryCategory.FLOAT_VALUE.fromNetwork(buf);
			double amount = buf.readDouble();
			boolean doPercentFormat = buf.readBoolean();
			boolean isNegative = buf.readBoolean();
			return new MultiplyEntryUpgradeResult(internals, target, intEntry, floatEntry, amount, doPercentFormat, isNegative);
		}
		
	}
	
}