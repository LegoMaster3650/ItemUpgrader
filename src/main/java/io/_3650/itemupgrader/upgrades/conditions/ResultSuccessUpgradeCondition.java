package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ResultSuccessUpgradeCondition extends UpgradeCondition {
	
	public ResultSuccessUpgradeCondition(IUpgradeInternals internals, boolean inverted) {
		super(internals, inverted, UpgradeEntrySet.EMPTY);
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		return data.getLastResultSuccess();
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.empty();
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<ResultSuccessUpgradeCondition> {
		
		@Override
		public ResultSuccessUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			return new ResultSuccessUpgradeCondition(internals, inverted);
		}
		
		@Override
		public void toNetwork(ResultSuccessUpgradeCondition condition, FriendlyByteBuf buf) {
			// nothing to write
		}
		
		@Override
		public ResultSuccessUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			return new ResultSuccessUpgradeCondition(internals, inverted);
		}
		
	}
	
}