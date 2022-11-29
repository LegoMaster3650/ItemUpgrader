package io._3650.itemupgrader.upgrades.results.modify;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.registry.types.NumberType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SetNumberEntryUpgradeResult extends UpgradeResult {
	
	private final NumberType target;
	private final UpgradeEntry<Integer> intEntry;
	private final UpgradeEntry<Float> floatEntry;
	private final double amount;
	
	public SetNumberEntryUpgradeResult(
			IUpgradeInternals internals, NumberType target,
			@Nullable UpgradeEntry<Integer> intEntry,
			@Nullable UpgradeEntry<Float> floatEntry,
			double amount) {
		super(internals, UpgradeEntrySet.create(builder -> {
			if (target == NumberType.INTEGER) builder.modifiable(intEntry);
			if (target == NumberType.FLOAT) builder.modifiable(floatEntry);
		}));
		this.target = target;
		this.intEntry = intEntry;
		this.floatEntry = floatEntry;
		this.amount = target == NumberType.INTEGER ? Math.round(amount) : amount;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		switch (this.target) {
		default:
			return false; //default should never happen
		case INTEGER:
			data.setModifiableEntry(this.intEntry, Double.valueOf(this.amount).intValue());
			return true;
		case FLOAT:
			data.setModifiableEntry(this.floatEntry, Double.valueOf(this.amount).floatValue());
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
		MutableComponent entryTooltip = new TranslatableComponent(this.target == NumberType.FLOAT ? this.floatEntry.getDescriptionId() : this.intEntry.getDescriptionId());
		MutableComponent amountTooltip = new TextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.amount));
		return new MutableComponent[] {entryTooltip, amountTooltip};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<SetNumberEntryUpgradeResult> {
		
		@Override
		public SetNumberEntryUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
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
			return new SetNumberEntryUpgradeResult(internals, target, intEntry, floatEntry, amount);
		}
		
		@Override
		public void toNetwork(SetNumberEntryUpgradeResult result, FriendlyByteBuf buf) {
			buf.writeEnum(result.target);
			result.intEntry.toNetwork(buf);
			result.floatEntry.toNetwork(buf);
			buf.writeDouble(result.amount);
		}
		
		@Override
		public SetNumberEntryUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			NumberType target = buf.readEnum(NumberType.class);
			UpgradeEntry<Integer> intEntry = EntryCategory.INT_VALUE.fromNetwork(buf);
			UpgradeEntry<Float> floatEntry = EntryCategory.FLOAT_VALUE.fromNetwork(buf);
			double amount = buf.readDouble();
			return new SetNumberEntryUpgradeResult(internals, target, intEntry, floatEntry, amount);
		}
		
	}
	
}