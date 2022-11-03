package io._3650.itemupgrader.api.util;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;

public class UpgradeSerializer {
	
	/**
	 * Deserializes an upgrade action from a json object
	 * @param json The {@linkplain JsonObject} to deserialize
	 * @return The resulting {@linkplain UpgradeAction}
	 * @throws NoSuchElementException if there is no valid action type for the detected identifier
	 */
	@Nonnull
	public static UpgradeAction action(JsonObject json) throws NoSuchElementException {
		//get action id
		ResourceLocation actionId = new ResourceLocation(GsonHelper.getAsString(json, "action"));
		//minecraft never registers anything anyways, default to item upgrader instead
		if (actionId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) actionId = ItemUpgraderRegistry.modRes(actionId.getPath());
		//verify it exists and throw a temper tantrum if not
		if (!ItemUpgrader.ACTION_REGISTRY.get().containsKey(actionId)) throw new NoSuchElementException("Action " + actionId.toString() + " does not exist");
		//get internals
		IUpgradeInternals internals = IUpgradeInternals.of(actionId, json);
		//get valid slots
		Set<EquipmentSlot> validSlots = new LinkedHashSet<>();
		if (GsonHelper.isStringValue(json, "slot")) {
			validSlots = new LinkedHashSet<>(1);
			validSlots.add(EquipmentSlot.byName(GsonHelper.getAsString(json, "slot")));
		} else if (GsonHelper.isArrayNode(json, "slots")) {
			JsonArray validSlotsJson = GsonHelper.getAsJsonArray(json, "slots");
			validSlots = new LinkedHashSet<>(validSlotsJson.size());
			for (var element : validSlotsJson) {
				if (GsonHelper.isStringValue(element)) validSlots.add(EquipmentSlot.byName(element.getAsString()));
			}
		}
		//get action serializer
		UpgradeActionSerializer<?> actionType = ItemUpgrader.ACTION_REGISTRY.get().getValue(actionId);
		//deserialize
		return actionType.fromJson(internals, validSlots, json);
	}
	
	/**
	 * Deserializes an upgrade condition from a json object
	 * @param json The {@linkplain JsonObject} to deserialize
	 * @return The resulting {@linkplain UpgradeCondition}
	 * @throws NoSuchElementException if there is no valid condition type for the detected identifier
	 */
	public static UpgradeCondition condition(JsonObject json) throws NoSuchElementException {
		//get condition id
		ResourceLocation conditionId = new ResourceLocation(GsonHelper.getAsString(json, "type"));
		//minecraft never registers anything anyways, default to item upgrader instead
		if (conditionId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) conditionId = ItemUpgraderRegistry.modRes(conditionId.getPath());
		//verify it exists and throw a temper tantrum if not
		if (!ItemUpgrader.CONDITION_REGISTRY.get().containsKey(conditionId)) throw new NoSuchElementException("Condition " + conditionId.toString() + " does not exist");
		//get internals
		IUpgradeInternals internals = IUpgradeInternals.of(conditionId, json);
		//get inverted
		boolean inverted = GsonHelper.getAsBoolean(json, "inverted", false);
		//get condition serializer
		UpgradeConditionSerializer<?> conditionType = ItemUpgrader.CONDITION_REGISTRY.get().getValue(conditionId);
		//deserialize
		return conditionType.fromJson(internals, inverted, json);
	}
	
	/**
	 * Deserializes an upgrade result from a json object
	 * @param json The {@linkplain JsonObject} to deserialize
	 * @return The resulting {@linkplain UpgradeResult}
	 * @throws NoSuchElementException if there is no valid result type for the detected identifier
	 */
	public static UpgradeResult result(JsonObject json) throws NoSuchElementException {
		// get result id
		ResourceLocation resultId = new ResourceLocation(GsonHelper.getAsString(json, "type"));
		// minecraft never registers anything anyways, default to item upgrader instead
		if (resultId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) resultId = ItemUpgraderRegistry.modRes(resultId.getPath());
		//verify it exists and throw a temper tantrum if not
		if (!ItemUpgrader.RESULT_REGISTRY.get().containsKey(resultId)) throw new NoSuchElementException("Result " + resultId.toString() + " does not exist");
		// get internals
		IUpgradeInternals internals = IUpgradeInternals.of(resultId, json);
		// get result serializer
		UpgradeResultSerializer<?> resultType = ItemUpgrader.RESULT_REGISTRY.get().getValue(resultId);
		// deserialize
		return resultType.fromJson(internals, json);
	}
	
	/**
	 * Serializes an upgrade action to a network buffer
	 * @param action The {@linkplain UpgradeAction} to serialize
	 * @param buf The {@linkplain FriendlyByteBuf} to serialize to
	 */
	public static void actionToNetwork(UpgradeAction action, FriendlyByteBuf buf) {
		action.getInternals().to(buf);
		Set<EquipmentSlot> actionValidSlots = action.getValidSlots();
		boolean hasActionValidSlots = actionValidSlots != null;
		buf.writeBoolean(hasActionValidSlots);
		if (hasActionValidSlots) {
			buf.writeInt(actionValidSlots.size());
			for (var slot : actionValidSlots) {
				buf.writeEnum(slot);
			}
		}
		action.hackyToNetworkReadJavadoc(buf);
	}

	/**
	 * Serializes an condition action to a network buffer
	 * @param condition The {@linkplain UpgradeCondition} to serialize
	 * @param buf The {@linkplain FriendlyByteBuf} to serialize to
	 */
	public static void conditionToNetwork(UpgradeCondition condition, FriendlyByteBuf buf) {
		buf.writeResourceLocation(condition.getId());
		condition.getInternals().to(buf);
		buf.writeBoolean(condition.isInverted());
		condition.hackyToNetworkReadJavadoc(buf);
	}

	/**
	 * Serializes an upgrade result to a network buffer
	 * @param result The {@linkplain UpgradeResult} to serialize
	 * @param buf The {@linkplain FriendlyByteBuf} to serialize to
	 */
	public static void resultToNetwork(UpgradeResult result, FriendlyByteBuf buf) {
		buf.writeResourceLocation(result.getId());
		result.getInternals().to(buf);
		result.hackyToNetworkReadJavadoc(buf);
	}
	
	/**
	 * Deserializes an upgrade action from a network buffer
	 * @param actionId The {@linkplain ResourceLocation} id for this action
	 * @param serializer The {@linkplain UpgradeActionSerializer} that serializes this action
	 * @param buf The {@linkplain FriendlyByteBuf} to deserialize from
	 * @return The resulting {@linkplain UpgradeAction}
	 */
	public static UpgradeAction actionFromNetwork(ResourceLocation actionId, UpgradeActionSerializer<?> serializer, FriendlyByteBuf buf) {
		IUpgradeInternals internals = IUpgradeInternals.of(actionId, buf);
		Set<EquipmentSlot> actionValidSlots = ImmutableSet.of();
		if (buf.readBoolean()) {
			int actionValidSlotsSize = buf.readInt();
			actionValidSlots = new LinkedHashSet<>(actionValidSlotsSize);
			for (int k = 0; k < actionValidSlotsSize; k++) {
				actionValidSlots.add(buf.readEnum(EquipmentSlot.class));
			}
		}
		return serializer.fromNetwork(internals, actionValidSlots, buf);
	}
	
	/**
	 * Deserializes an upgrade condition from a network buffer
	 * @param buf The {@linkplain FriendlyByteBuf} to deserialize from
	 * @return The resulting {@linkplain UpgradeResult}
	 * @throws NoSuchElementException if there is no valid result type for the detected identifier
	 */
	public static UpgradeCondition conditionFromNetwork(FriendlyByteBuf buf) throws NoSuchElementException {
		ResourceLocation conditionId = buf.readResourceLocation();
		if (!ItemUpgrader.CONDITION_REGISTRY.get().containsKey(conditionId)) throw new NoSuchElementException("Condition " + conditionId.toString() + " does not exist");
		IUpgradeInternals internals = IUpgradeInternals.of(conditionId, buf);
		boolean inverted = buf.readBoolean();
		UpgradeConditionSerializer<?> serializer = ItemUpgrader.CONDITION_REGISTRY.get().getValue(conditionId);
		return serializer.fromNetwork(internals, inverted, buf);
	}
	
	/**
	 * Deserializes an upgrade result from a network buffer
	 * @param buf The {@linkplain FriendlyByteBuf} to deserialize from
	 * @return The resulting {@linkplain UpgradeResult}
	 * @throws NoSuchElementException if there is no valid result type for the detected identifier
	 */
	public static UpgradeResult resultFromNetwork(FriendlyByteBuf buf) throws NoSuchElementException {
		ResourceLocation resultId = buf.readResourceLocation();
		if (!ItemUpgrader.RESULT_REGISTRY.get().containsKey(resultId)) throw new NoSuchElementException("Result " + resultId.toString() + " does not exist");
		IUpgradeInternals resultInternals = IUpgradeInternals.of(resultId, buf);
		UpgradeResultSerializer<?> serializer = ItemUpgrader.RESULT_REGISTRY.get().getValue(resultId);
		return serializer.fromNetwork(resultInternals, buf);
	}
	
}