package io._3650.itemupgrader.api.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public abstract class ConditionalUpgradeAction extends UpgradeAction {
	
	private final ImmutableList<UpgradeCondition> conditions;
	
	public ConditionalUpgradeAction(IUpgradeInternals internals, List<UpgradeCondition> conditions) {
		super(internals);
		this.conditions = ImmutableList.copyOf(conditions);
	}
	
	@Override
	public boolean customTooltipBase() {
		return true;
	}
	
	@Override
	public final void run(UpgradeEventData event) {
		boolean pass = true;
		for (UpgradeCondition condition : this.conditions) {
			pass = pass && (condition.test(event) ^ condition.isInverted()); //I love XOR so much its like a funky conditional NOT
		}
		if (pass) this.execute(event);
	}
	
	@Override
	public MutableComponent getActionTooltip(ItemStack stack) {
		List<MutableComponent> conditionComponents = new ArrayList<>(this.conditions.size());
		for (UpgradeCondition condition : this.conditions) {
			if(condition.isVisible()) conditionComponents.add(new TranslatableComponent("upgradeCondition." + ComponentHelper.keyFormat(condition.getId()) + (condition.isInverted() ? ".inverse" : ""), (Object[]) condition.getTooltipWithOverride(stack)));
		}
		MutableComponent conditionTooltip = ComponentHelper.andList(conditionComponents);
		
		MutableComponent resultTooltip = this.getResultTooltip(stack);
		
		String tooltipKey = "upgradeAction." + this.getId().getNamespace() + "." + this.getId().getPath();
		if (conditionComponents.isEmpty()) {
			return new TranslatableComponent(tooltipKey, resultTooltip).withStyle(ChatFormatting.BLUE);
		} else {
			return new TranslatableComponent(tooltipKey + ".condition", conditionTooltip, resultTooltip).withStyle(ChatFormatting.BLUE);
		}
	}
	
	public abstract void execute(UpgradeEventData event);
	
	public abstract MutableComponent getResultTooltip(ItemStack stack);
	
	public static abstract class ConditionalUpgradeActionSerializer<T extends ConditionalUpgradeAction> extends UpgradeActionSerializer<T> {
		
		public final List<UpgradeCondition> conditionsFromJson(JsonObject json) {
			ArrayList<UpgradeCondition> conditions = new ArrayList<>();
			if (json.has("condition")) {
				if (GsonHelper.isArrayNode(json, "condition")) {
					GsonHelper.getAsJsonArray(json, "condition").forEach(conditionJson -> {
						if (conditionJson.isJsonObject()) {
							UpgradeCondition condition = ItemUpgradeManager.conditionFromJson(conditionJson.getAsJsonObject());
							this.safeAddCondition(conditions, condition);
						}
					});
				} else {
					UpgradeCondition condition = ItemUpgradeManager.conditionFromJson(GsonHelper.getAsJsonObject(json, "condition"));
					this.safeAddCondition(conditions, condition);
				}
			}
			return conditions;
		}
		
		private void safeAddCondition(List<UpgradeCondition> conditions, UpgradeCondition condition) {
			Set<UpgradeEntry<?>> test = Sets.difference(condition.requiredData().getRequired(), this.providedData().getRequired());
			if (test.isEmpty()) {
				conditions.add(condition);
			} else {
				Iterator<UpgradeEntry<?>> iter = test.iterator();
				String errStr = iter.next().toString();
				while (iter.hasNext()) errStr = errStr + ", " + iter.next();
				throw new IllegalArgumentException("Missing required entries for condition " + condition.getId() + " - [" + errStr + "]");
			}
		}
		
		public final void conditionsToNetwork(ConditionalUpgradeAction action, FriendlyByteBuf buf) {
			buf.writeInt(action.conditions.size());
			for (var condition : action.conditions) {
				buf.writeResourceLocation(condition.getId());
				condition.getInternals().to(buf);
				buf.writeBoolean(condition.isInverted());
				condition.hackyToNetworkReadJavadoc(buf);
			}
		}
		
		public final List<UpgradeCondition> conditionsFromNetwork(FriendlyByteBuf buf) {
			int netConditionsSize = buf.readInt();
			ArrayList<UpgradeCondition> netConditions = new ArrayList<>(netConditionsSize);
			for (var i = 0; i < netConditionsSize; i++) {
				ResourceLocation conditionId = buf.readResourceLocation();
				IUpgradeInternals internals = IUpgradeInternals.of(conditionId, buf);
				boolean inverted = buf.readBoolean();
				UpgradeConditionSerializer<?> serializer = ItemUpgrader.CONDITION_REGISTRY.get().getValue(conditionId);
				netConditions.add(serializer.fromNetwork(internals, inverted, buf));
			}
			return netConditions;
		}
	}
	
}