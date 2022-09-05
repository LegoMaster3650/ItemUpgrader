package io.legom.itemupgrader.upgrades;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.legom.itemupgrader.ItemUpgrader;
import io.legom.itemupgrader.api.serializer.UpgradeActionSerializer;
import io.legom.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io.legom.itemupgrader.api.serializer.UpgradeResultSerializer;
import io.legom.itemupgrader.api.type.IUpgradeType.IUpgradeInternals;
import io.legom.itemupgrader.api.type.UpgradeAction;
import io.legom.itemupgrader.api.type.UpgradeCondition;
import io.legom.itemupgrader.api.type.UpgradeResult;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemUpgradeManager extends SimpleJsonResourceReloadListener {
	
	public static final ItemUpgradeManager INSTANCE = new ItemUpgradeManager();
	
	private Map<ResourceLocation, ItemUpgrade> upgrades = ImmutableMap.of();
	
	public ItemUpgradeManager() {
		super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "item_upgrades");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
		ImmutableMap.Builder<ResourceLocation, ItemUpgrade> builder = ImmutableMap.builder();
		
		jsonMap.forEach((upgradeId, jsonElem) -> {
			try {
				JsonObject json = jsonElem.getAsJsonObject();
				
				//base
				Ingredient base = Ingredient.fromJson(json.get("base"));
				//slots
				Set<EquipmentSlot> validSlots = new LinkedHashSet<EquipmentSlot>();
				GsonHelper.getAsJsonArray(json, "slots").forEach((element) -> {
					if (GsonHelper.isStringValue(element)) validSlots.add(EquipmentSlot.byName(element.getAsString()));
				});
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
				ItemUpgrader.LOGGER.error("Couldn't parse upgrade " + upgradeId.toString(), err);
			}
		});
		
		this.upgrades = builder.build();
		
	}
	
	public static UpgradeAction actionFromJson(JsonObject json) {
		ResourceLocation actionId = new ResourceLocation(GsonHelper.getAsString(json, "action"));
		IUpgradeInternals internals = IUpgradeInternals.of(actionId, json);
		UpgradeActionSerializer<?> actionType = ItemUpgrader.ACTION_REGISTRY.get().getValue(actionId);
		return actionType.fromJson(internals, json);
	}
	
	public static UpgradeCondition conditionFromJson(JsonObject json) {
		ResourceLocation conditionId = new ResourceLocation(GsonHelper.getAsString(json, "type"));
		IUpgradeInternals internals = IUpgradeInternals.of(conditionId, json);
		boolean inverted = GsonHelper.getAsBoolean(json, "inverted", false);
		UpgradeConditionSerializer<?> conditionType = ItemUpgrader.CONDITION_REGISTRY.get().getValue(conditionId);
		return conditionType.fromJson(internals, inverted, json);
	}
	
	public static UpgradeResult resultFromJson(JsonObject json) {
		ResourceLocation resultId = new ResourceLocation(GsonHelper.getAsString(json, "type"));
		IUpgradeInternals internals = IUpgradeInternals.of(resultId, json);
		UpgradeResultSerializer<?> resultType = ItemUpgrader.RESULT_REGISTRY.get().getValue(resultId);
		return resultType.fromJson(internals, json);
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