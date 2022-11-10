package io._3650.itemupgrader.upgrades.conditions.compound;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.UpgradeSerializer;
import io._3650.itemupgrader.api.util.UpgradeTooltipHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class XorUpgradeCondition extends UpgradeCondition {
	
	private final UpgradeCondition cond1;
	private final UpgradeCondition cond2;
	
	public XorUpgradeCondition(IUpgradeInternals internals, UpgradeCondition cond1, UpgradeCondition cond2) {
		super(internals, false, UpgradeEntrySet.create(builder -> {
			builder.combine(cond1.getRequiredData()).combine(cond2.getRequiredData());
		}));
		this.cond1 = cond1;
		this.cond2 = cond2;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		return this.cond1.test(data) ^ this.cond2.test(data);
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {UpgradeTooltipHelper.condition(this.cond1, stack), UpgradeTooltipHelper.condition(this.cond2, stack)};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<XorUpgradeCondition> {
		
		@Override
		public XorUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			JsonArray jsonConditions = GsonHelper.getAsJsonArray(json, "conditions");
			if (jsonConditions.size() != 2) throw new IllegalArgumentException("A Xor condition must have exactly 2 values. Got " + jsonConditions.size() + " instead.");
			UpgradeCondition cond1 = UpgradeSerializer.condition(jsonConditions.get(0).getAsJsonObject());
			UpgradeCondition cond2 = UpgradeSerializer.condition(jsonConditions.get(1).getAsJsonObject());
			return new XorUpgradeCondition(internals, cond1, cond2);
		}
		
		@Override
		public void toNetwork(XorUpgradeCondition condition, FriendlyByteBuf buf) {
			UpgradeSerializer.conditionToNetwork(condition.cond1, buf);
			UpgradeSerializer.conditionToNetwork(condition.cond2, buf);
		}
		
		@Override
		public XorUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			UpgradeCondition cond1 = UpgradeSerializer.conditionFromNetwork(buf);
			UpgradeCondition cond2 = UpgradeSerializer.conditionFromNetwork(buf);
			return new XorUpgradeCondition(internals, cond1, cond2);
		}
		
	}
	
}