package io._3650.itemupgrader.api.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.api.util.UpgradeSerializer;
import io._3650.itemupgrader.api.util.UpgradeTooltipHelper;
import io._3650.itemupgrader.api.util.UpgradeJsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
	private final ImmutableList<UpgradeResult> elseResults;
	private final Serializer serializer;
	
	public SimpleUpgradeAction(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, List<UpgradeCondition> conditions, List<UpgradeResult> results, List<UpgradeResult> elseResults, Serializer serializer) {
		super(internals, validSlots, conditions);
		this.results = ImmutableList.copyOf(results);
		this.elseResults = ImmutableList.copyOf(elseResults);
		this.serializer = serializer;
	}
	
	public static Supplier<Serializer> of(UpgradeEntrySet provided) {
		return () -> new Serializer(provided);
	}
	
	@Override
	public MutableComponent applyResultTooltip(MutableComponent tooltip, ItemStack stack) {
		//regular results
		ArrayList<MutableComponent> resultComponents = new ArrayList<>(this.results.size());
		for (var result : this.results) {
			if (result.isVisible()) {
				resultComponents.add(UpgradeTooltipHelper.result(result, stack));
			}
		}
		tooltip.append(ComponentHelper.andList(resultComponents));
		//else results
		if (this.elseResults.size() > 0) {
			ArrayList<MutableComponent> elseResultComponents = new ArrayList<>(this.elseResults.size());
			for (var result : this.elseResults) {
				if (result.isVisible()) {
					elseResultComponents.add(UpgradeTooltipHelper.result(result, stack));
				}
			}
			if (elseResultComponents.size() > 0) return tooltip.append(new TranslatableComponent("tooltip.itemupgrader.else").append(ComponentHelper.andList(elseResultComponents)));
		}
		//return
		return tooltip;
	}
	
	@Override
	public void execute(UpgradeEventData data) {
		for (UpgradeResult result : this.results) {
			UpgradeEventData.InternalStuffIgnorePlease.setSuccess(data, result.execute(data));
		}
	}
	
	@Override
	public void onFail(UpgradeEventData data) {
		for (UpgradeResult result : this.elseResults) {
			UpgradeEventData.InternalStuffIgnorePlease.setSuccess(data, result.execute(data));
		}
	}
	
	@Override
	public Serializer getSerializer() {
		return serializer;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.empty();
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	/**
	 * Serializer for {@linkplain SimpleUpgradeAction}
	 * @author LegoMaster3650
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
			List<UpgradeResult> results;
			if (json.has("result")) {
				results = UpgradeJsonHelper.collectObjects(json.get("result"), resultJson -> {
					UpgradeResult result = UpgradeSerializer.result(resultJson);
					if (this.verifyResult(result)) return result;
					else return null;
				});
			} else results = new ArrayList<>(0);
			List<UpgradeResult> elseResults;
			if (json.has("else")) {
				elseResults = UpgradeJsonHelper.collectObjects(json.get("else"), resultJson -> {
					UpgradeResult result = UpgradeSerializer.result(resultJson);
					if (this.verifyResult(result)) return result;
					else return null;
				});
			} else elseResults = new ArrayList<>(0);
			return new SimpleUpgradeAction(internals, validSlots, conditions, results, elseResults, this);
		}
		
		private boolean verifyResult(UpgradeResult result) {
			Set<UpgradeEntry<?>> test = result.getRequiredData().verifyDifference(this.getProvidedData());
			if (test.isEmpty()) return true;
			else throw new IllegalArgumentException("Missing required entries for result:" + result.getId() + " - " + test);
		}
		
		@Override
		public void toNetwork(SimpleUpgradeAction action, FriendlyByteBuf buf) {
			//conditions
			this.conditionsToNetwork(action, buf);
			//results
			buf.writeInt(action.results.size());
			for (var result : action.results) {
				UpgradeSerializer.resultToNetwork(result, buf);
			}
			buf.writeInt(action.elseResults.size());
			for (var result : action.elseResults) {
				UpgradeSerializer.resultToNetwork(result, buf);
			}
		}
		
		public SimpleUpgradeAction fromNetwork(IUpgradeInternals internals, Set<EquipmentSlot> validSlots, FriendlyByteBuf buf) {
			//conditions
			List<UpgradeCondition> conditions = this.conditionsFromNetwork(buf);
			//results
			int resultsSize = buf.readInt();
			List<UpgradeResult> results = new ArrayList<>(resultsSize);
			for (int i = 0; i < resultsSize; i++) {
				results.add(UpgradeSerializer.resultFromNetwork(buf));
			}
			int elseResultsSize = buf.readInt();
			List<UpgradeResult> elseResults = new ArrayList<>(elseResultsSize);
			for (int i = 0; i < elseResultsSize; i++) {
				elseResults.add(UpgradeSerializer.resultFromNetwork(buf));
			}
			return new SimpleUpgradeAction(internals, validSlots, conditions, results, elseResults, this);
		}
		
	}
	
}