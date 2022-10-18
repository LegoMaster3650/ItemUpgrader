package io._3650.itemupgrader.api.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.ItemUpgrader;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * A simple implementation of a conditional upgrade action with results.<br>
 * Unless you need special event logic, you should use this for most custom upgrade actions.<br>
 * <br>
 * To use: Initialize a {@linkplain Serializer} with your desired {@linkplain UpgradeEntrySet}
 * @author LegoMaster3650
 */
public class SimpleUpgradeAction extends ConditionalUpgradeAction {
	
	private final ImmutableList<UpgradeResult> results;
	private final Serializer serializer;
	
	public SimpleUpgradeAction(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, List<UpgradeCondition> conditions, List<UpgradeResult> results, Serializer serializer) {
		super(internals, validSlots, conditions);
		this.results = ImmutableList.copyOf(results);
		this.serializer = serializer;
	}
	
	public static Supplier<Serializer> of(UpgradeEntrySet provided) {
		return () -> new Serializer(provided);
	}
	
	@Override
	public MutableComponent getResultTooltip(ItemStack stack) {
		List<MutableComponent> resultComponents = new ArrayList<>(this.results.size());
		for (UpgradeResult result : this.results) {
			if (result.isVisible()) {
				if (result.hasTooltipOverride()) resultComponents.add(new TranslatableComponent(result.getTooltipOverride()));
				else resultComponents.add(new TranslatableComponent("upgradeResult." + ComponentHelper.keyFormat(result.getId()), (Object[]) result.getTooltip(stack)));
			}
		}
		return ComponentHelper.andList(resultComponents);
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		for (UpgradeResult result : this.results) {
			result.execute(data);
		}
	}
	
	@Override
	public Serializer getSerializer() {
		return serializer;
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	/**
	 * Serializer for {@linkplain SimpleUpgradeAction}
	 * @author LegoMaster3650
	 *
	 */
	public static class Serializer extends ConditionalUpgradeActionSerializer<SimpleUpgradeAction> {
		
		private final UpgradeEntrySet provided;
		
		/**
		 * Constructs a new serializer for a {@linkplain SimpleUpgradeAction}
		 * @param provided
		 */
		public Serializer(UpgradeEntrySet provided) {
			this.provided = provided;
		}
		
		@Override
		public UpgradeEntrySet getProvidedData() {
			return provided;
		}
		
		@Override
		public SimpleUpgradeAction fromJson(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, JsonObject json) {
			List<UpgradeCondition> conditions = this.conditionsFromJson(json);
			List<UpgradeResult> results = new ArrayList<>();
			if (json.has("result")) {
				if (GsonHelper.isArrayNode(json, "result")) {
					GsonHelper.getAsJsonArray(json, "result").forEach(resultJson -> {
						if (resultJson.isJsonObject()) {
							UpgradeResult result = ItemUpgradeManager.resultFromJson(resultJson.getAsJsonObject());
							this.safeAddResult(results, result);
						}
					});
				} else {
					UpgradeResult result = ItemUpgradeManager.resultFromJson(GsonHelper.getAsJsonObject(json, "result"));
					this.safeAddResult(results, result);
				}
			}
			return new SimpleUpgradeAction(internals, validSlots, conditions, results, this);
		}
		
		private void safeAddResult(List<UpgradeResult> results, UpgradeResult result) {
			Set<UpgradeEntry<?>> test = result.getRequiredData().verifyDifference(this.getProvidedData());
			if (test.isEmpty()) {
				results.add(result);
			} else {
				throw new IllegalArgumentException("Missing required entries for result:" + result.getId() + " - " + test);
			}
		}
		
		@Override
		public void toNetwork(SimpleUpgradeAction action, FriendlyByteBuf buf) {
			//conditions
			this.conditionsToNetwork(action, buf);
			//results
			buf.writeInt(action.results.size());
			for (var result : action.results) {
				buf.writeResourceLocation(result.getId());
				result.getInternals().to(buf);
				result.hackyToNetworkReadJavadoc(buf);
			}
		}
		
		public SimpleUpgradeAction fromNetwork(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, FriendlyByteBuf buf) {
			//conditions
			List<UpgradeCondition> netConditions = this.conditionsFromNetwork(buf);
			//results
			int netResultsSize = buf.readInt();
			List<UpgradeResult> netResults = new ArrayList<>(netResultsSize);
			for (int i = 0; i < netResultsSize; i++) {
				ResourceLocation resultId = buf.readResourceLocation();
				IUpgradeInternals resultInternals = IUpgradeInternals.of(resultId, buf);
				UpgradeResultSerializer<?> serializer = ItemUpgrader.RESULT_REGISTRY.get().getValue(resultId);
				netResults.add(serializer.fromNetwork(resultInternals, buf));
			}
			return new SimpleUpgradeAction(internals, validSlots, netConditions, netResults, this);
		}
		
	}
	
}