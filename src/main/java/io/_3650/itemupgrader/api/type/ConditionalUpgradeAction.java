package io._3650.itemupgrader.api.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Class for upgrade actions that rely on a condition in order to run
 * @author LegoMaster3650
 */
public abstract class ConditionalUpgradeAction extends UpgradeAction {
	
	private final ImmutableList<UpgradeCondition> conditions;
	
	public ConditionalUpgradeAction(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, List<UpgradeCondition> conditions) {
		super(internals, validSlots);
		this.conditions = ImmutableList.copyOf(conditions);
	}
	
	@Override
	public boolean customTooltipBase() {
		return true;
	}
	
	/**
	 * @see UpgradeAction#getActionTooltip(ItemStack) getActionTooltip(ItemStack)
	 * @see #getResultTooltip(ItemStack)
	 */
	@Override
	public MutableComponent getActionTooltip(ItemStack stack) {
		List<MutableComponent> conditionComponents = new ArrayList<>(this.conditions.size());
		for (UpgradeCondition condition : this.conditions) {
			if (condition.isVisible()) {
				if (condition.hasTooltipOverride()) conditionComponents.add(new TranslatableComponent(condition.getTooltipOverride()));
				else conditionComponents.add(new TranslatableComponent("upgradeCondition." + ComponentHelper.keyFormat(condition.getId()) + (condition.isInverted() ? ".inverse" : ""), (Object[]) condition.getTooltip(stack)));
			}
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
	
	/**
	 * @see UpgradeAction#run(UpgradeEventData) run(UpgradeEventData)
	 * @see #execute(UpgradeEventData)
	 */
	@Override
	public final void run(UpgradeEventData data) {
		boolean pass = true;
		for (UpgradeCondition condition : this.conditions) {
			pass = pass && (condition.test(data) ^ condition.isInverted()); //I love XOR so much its like a funky conditional NOT
		}
		if (pass) this.execute(data);
	}
	
	/**
	 * Gets the tooltip component for the object applied to the tooltip defined in the language file
	 * @param stack The ItemStack to get tooltip context from
	 * @return A MutableComponent to apply to the tooltip specified in the language file
	 */
	public abstract MutableComponent getResultTooltip(ItemStack stack);
	
	/**
	 * Defines the behavior for a ConditionalUpgradeAction after running
	 * @param data The {@linkplain UpgradeEventData} parameters passed in
	 */
	public abstract void execute(UpgradeEventData data);
	
	/**
	 * Serializer class for conditional upgrade actions
	 * @author LegoMaster3650
	 * 
	 * @param <T> The {@linkplain ConditionalUpgradeAction} subclass serialized by this serializer
	 */
	public static abstract class ConditionalUpgradeActionSerializer<T extends ConditionalUpgradeAction> extends UpgradeActionSerializer<T> {
		
		/**
		 * Gets action conditions from json
		 * @param json The {@linkplain JsonObject} to get the conditions from
		 * @return A {@linkplain List} of {@linkplain UpgradeCondition}s read from the json
		 */
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
			Set<UpgradeEntry<?>> test = condition.getRequiredData().verifyDifference(this.getProvidedData());
			if (test.isEmpty()) {
				conditions.add(condition);
			} else {
				throw new IllegalArgumentException("Missing required entries for condition:" + condition.getId() + " - " + test);
			}
		}
		
		/**
		 * Writes the given conditional action's conditions to a network buffer
		 * @param action The {@linkplain ConditionalUpgradeAction} to write to the buffer
		 * @param buf The {@linkplain FriendlyByteBuf} to write the conditions to
		 */
		public final void conditionsToNetwork(ConditionalUpgradeAction action, FriendlyByteBuf buf) {
			buf.writeInt(action.conditions.size());
			for (var condition : action.conditions) {
				buf.writeResourceLocation(condition.getId());
				condition.getInternals().to(buf);
				buf.writeBoolean(condition.isInverted());
				condition.hackyToNetworkReadJavadoc(buf);
			}
		}
		
		/**
		 * Reads a list of upgrade conditions from a network buffer
		 * @param buf The {@linkplain FriendlyByteBuf} to read the conditions from
		 * @return A {@linkplain List} of {@linkplain UpgradeCondition}s read from the buffer
		 */
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