package io._3650.itemupgrader.upgrades.conditions.compound;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.api.util.UpgradeSerializer;
import io._3650.itemupgrader.api.util.UpgradeTooltipHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class OrUpgradeCondition extends UpgradeCondition {
	
	private final ArrayList<UpgradeCondition> conditions;
	
	public OrUpgradeCondition(IUpgradeInternals internals, ArrayList<UpgradeCondition> conditions) {
		super(internals, false, UpgradeEntrySet.create(builder -> {
			for (var condition : conditions) builder.combine(condition.getRequiredData());
		}));
		this.conditions = conditions;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		boolean result = true;
		for (var condition : this.conditions) result |= condition.test(data);
		return result;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		ArrayList<MutableComponent> conditionComponents = new ArrayList<>(this.conditions.size());
		for (var condition : this.conditions) {
			if (condition.isVisible()) {
				conditionComponents.add(UpgradeTooltipHelper.condition(condition, stack));
			}
		}
		return ComponentHelper.arrayify(ComponentHelper.orList(conditionComponents));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<OrUpgradeCondition> {
		
		@Override
		public OrUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			JsonArray jsonConditions = GsonHelper.getAsJsonArray(json, "conditions");
			ArrayList<UpgradeCondition> conditions = new ArrayList<>(jsonConditions.size());
			for (var jsonCondition : jsonConditions) conditions.add(UpgradeSerializer.condition(jsonCondition.getAsJsonObject()));
			return new OrUpgradeCondition(internals, conditions);
		}
		
		@Override
		public void toNetwork(OrUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeInt(condition.conditions.size());
			for (var con : condition.conditions) UpgradeSerializer.conditionToNetwork(con, buf);
		}
		
		@Override
		public OrUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			int conditionsSize = buf.readInt();
			ArrayList<UpgradeCondition> conditions = new ArrayList<>(conditionsSize);
			for (var i = 0; i < conditionsSize; i++) conditions.add(UpgradeSerializer.conditionFromNetwork(buf));
			return new OrUpgradeCondition(internals, conditions);
		}
		
	}
	
}