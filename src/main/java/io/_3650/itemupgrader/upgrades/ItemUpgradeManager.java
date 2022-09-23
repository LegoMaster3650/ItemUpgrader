package io._3650.itemupgrader.upgrades;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.ItemUpgrade;
import io._3650.itemupgrader.api.registry.ItemUpgraderRegistry;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

public class ItemUpgradeManager extends SimpleJsonResourceReloadListener {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public static final ItemUpgradeManager INSTANCE = new ItemUpgradeManager();
	
	private Map<ResourceLocation, ItemUpgrade> upgrades = ImmutableMap.of();
	
	public ItemUpgradeManager() {
		super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "item_upgrades");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
		ImmutableMap.Builder<ResourceLocation, ItemUpgrade> builder = ImmutableMap.builder();
		
		boolean contextValid = true;
		if (this.serverContext == null) {
			LOGGER.error("Skipping datapack condition checks for upgrades as the context was null, !!!Report this if you see it!!!");
			contextValid = false;
		}
		
		for (var upgradeId : jsonMap.keySet()) {
			try {
				JsonObject json = jsonMap.get(upgradeId).getAsJsonObject();
				
				//Hopefully will just ignore context at worst idk its 1 AM
				if (contextValid && !CraftingHelper.processConditions(json, "conditions", this.serverContext)) {
					LOGGER.debug("Skipping loading upgrade {} as it's conditions were not met", upgradeId);
					continue;
				}
				
				//base
				Ingredient base = Ingredient.fromJson(json.get("base"));
				//slots
				JsonArray validSlotsJson = GsonHelper.getAsJsonArray(json, "slots");
				Set<EquipmentSlot> validSlots = new LinkedHashSet<EquipmentSlot>(validSlotsJson.size());
				for (var element : validSlotsJson) {
					if (GsonHelper.isStringValue(element)) validSlots.add(EquipmentSlot.byName(element.getAsString()));
				}
				//is visible
				boolean visible = GsonHelper.getAsBoolean(json, "visible", true);
				//description lines
				int descriptionLines = GsonHelper.getAsInt(json, "description", 0);
				//color
				TextColor color = TextColor.parseColor(GsonHelper.getAsString(json, "color", "#FFFFFF"));
				
				//actions
				ListMultimap<ResourceLocation, UpgradeAction> actions = MultimapBuilder.treeKeys().arrayListValues().build();
				if (GsonHelper.isArrayNode(json, "upgrade")) {
					GsonHelper.getAsJsonArray(json, "upgrade").forEach(upgradeJson -> {
						if (upgradeJson.isJsonObject()) {
							UpgradeAction act = actionFromJson(upgradeJson.getAsJsonObject());
							actions.put(act.getId(), act);
						}
					});
				} else {
					UpgradeAction act = actionFromJson(GsonHelper.getAsJsonObject(json, "upgrade"));
					actions.put(act.getId(), act);
				}
				MultimapBuilder.treeKeys().arrayListValues();
				
				builder.put(upgradeId, new ItemUpgrade(upgradeId, base, validSlots, actions, visible, descriptionLines, color));
			} catch (Exception err) {
				LOGGER.error("Couldn't parse upgrade " + upgradeId.toString(), err);
			}
		}
		
		this.upgrades = builder.build();
		
	}
	
	public static UpgradeAction actionFromJson(JsonObject json) {
		//get action id
		ResourceLocation actionId = new ResourceLocation(GsonHelper.getAsString(json, "action"));
		//minecraft never registers anything anyways, default to item upgrader instead
		if (actionId.getNamespace() == ResourceLocation.DEFAULT_NAMESPACE) actionId = ItemUpgraderRegistry.modRes(actionId.getNamespace());
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
	
	public static UpgradeCondition conditionFromJson(JsonObject json) {
		//get condition id
		ResourceLocation conditionId = new ResourceLocation(GsonHelper.getAsString(json, "type"));
		//minecraft never registers anything anyways, default to item upgrader instead
		if (conditionId.getNamespace() == ResourceLocation.DEFAULT_NAMESPACE) conditionId = ItemUpgraderRegistry.modRes(conditionId.getNamespace());
		//get internals
		IUpgradeInternals internals = IUpgradeInternals.of(conditionId, json);
		//get inverted
		boolean inverted = GsonHelper.getAsBoolean(json, "inverted", false);
		//get condition serializer
		UpgradeConditionSerializer<?> conditionType = ItemUpgrader.CONDITION_REGISTRY.get().getValue(conditionId);
		//deserialize
		return conditionType.fromJson(internals, inverted, json);
	}
	
	public static UpgradeResult resultFromJson(JsonObject json) {
		//get result id
		ResourceLocation resultId = new ResourceLocation(GsonHelper.getAsString(json, "type"));
		//minecraft never registers anything anyways, default to item upgrader instead
		if (resultId.getNamespace() == ResourceLocation.DEFAULT_NAMESPACE) resultId = ItemUpgraderRegistry.modRes(resultId.getNamespace());
		//get internals
		IUpgradeInternals internals = IUpgradeInternals.of(resultId, json);
		//get result serializer
		UpgradeResultSerializer<?> resultType = ItemUpgrader.RESULT_REGISTRY.get().getValue(resultId);
		//deserialize
		return resultType.fromJson(internals, json);
	}
	
	private net.minecraftforge.common.crafting.conditions.ICondition.IContext serverContext = null;
	public void setContext(net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
		this.serverContext = context;
	}
	
	@Nullable
	public ItemUpgrade getUpgrade(ResourceLocation upgradeId) {
		return this.upgrades.get(upgradeId);
	}
	
	public Map<ResourceLocation, ItemUpgrade> getUpgrades() {
		return this.upgrades;
	}
	
	public void setUpgrades(Map<ResourceLocation, ItemUpgrade> upgradeMap) {
		this.upgrades = ImmutableMap.copyOf(upgradeMap);
	}
	
}