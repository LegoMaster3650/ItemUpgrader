package io._3650.itemupgrader.api.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.util.UpgradeSerializer;
import io._3650.itemupgrader.api.util.UpgradeTooltipHelper;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.api.util.UpgradeJsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
	public MutableComponent applyTooltip(MutableComponent tooltip, ItemStack stack) {
		ArrayList<MutableComponent> conditionComponents = new ArrayList<>(this.conditions.size());
		for (var condition : this.conditions) {
			if (condition.isVisible()) {
				conditionComponents.add(UpgradeTooltipHelper.condition(condition, stack));
			}
		}
		if (!conditionComponents.isEmpty()) tooltip.append(new TranslatableComponent("tooltip.itemupgrader.if", ComponentHelper.andList(conditionComponents)));
		return this.applyResultTooltip(tooltip, stack);
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
		else this.onFail(data);
	}
	
	/**
	 * Allows post-processing logic to be applied to the given tooltip
	 * @param tooltip The current existing tooltip for the action
	 * @param stack The {@linkplain ItemStack} to get tooltip context from
	 * @return The tooltip after modification (you can overwrite it)
	 */
	public abstract MutableComponent applyResultTooltip(MutableComponent tooltip, ItemStack stack);
	
	/**
	 * Defines the behavior for a ConditionalUpgradeAction after running
	 * @param data The {@linkplain UpgradeEventData} parameters passed in
	 */
	public abstract void execute(UpgradeEventData data);
	
	/**
	 * Defines the optional behavior for a ConditionalUpgradeAction if the conditions don't passs
	 * @param data The {@linkplain UpgradeEventData} parameters passed in
	 */
	public void onFail(UpgradeEventData data) {};
	
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
			ArrayList<UpgradeCondition> conditions;
			if (json.has("condition")) {
				conditions = UpgradeJsonHelper.collectObjects(json.get("condition"), conditionJson -> {
					UpgradeCondition condition = UpgradeSerializer.condition(conditionJson);
					if (this.verifyCondition(condition)) return condition;
					else return null;
				});
			} else conditions = new ArrayList<>(0);
			return conditions;
		}
		
		private boolean verifyCondition(UpgradeCondition condition) {
			Set<UpgradeEntry<?>> test = condition.getRequiredData().verifyDifference(this.getProvidedData());
			if (test.isEmpty()) return true;
			else throw new IllegalArgumentException("Missing required entries for condition:" + condition.getId() + " - " + test);
		}
		
		/**
		 * Writes the given conditional action's conditions to a network buffer
		 * @param action The {@linkplain ConditionalUpgradeAction} to write to the buffer
		 * @param buf The {@linkplain FriendlyByteBuf} to write the conditions to
		 */
		public final void conditionsToNetwork(ConditionalUpgradeAction action, FriendlyByteBuf buf) {
			buf.writeInt(action.conditions.size());
			for (var condition : action.conditions) {
				UpgradeSerializer.conditionToNetwork(condition, buf);
			}
		}
		
		/**
		 * Reads a list of upgrade conditions from a network buffer
		 * @param buf The {@linkplain FriendlyByteBuf} to read the conditions from
		 * @return A {@linkplain List} of {@linkplain UpgradeCondition}s read from the buffer
		 */
		public final List<UpgradeCondition> conditionsFromNetwork(FriendlyByteBuf buf) {
			int conditionsSize = buf.readInt();
			ArrayList<UpgradeCondition> conditions = new ArrayList<>(conditionsSize);
			for (var i = 0; i < conditionsSize; i++) {
				conditions.add(UpgradeSerializer.conditionFromNetwork(buf));
			}
			return conditions;
		}
	}
	
}