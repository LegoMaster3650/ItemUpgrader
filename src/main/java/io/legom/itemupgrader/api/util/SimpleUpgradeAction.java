package io.legom.itemupgrader.api.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import io.legom.itemupgrader.ItemUpgrader;
import io.legom.itemupgrader.api.data.UpgradeEntry;
import io.legom.itemupgrader.api.data.UpgradeEntrySet;
import io.legom.itemupgrader.api.data.UpgradeEventData;
import io.legom.itemupgrader.api.serializer.UpgradeResultSerializer;
import io.legom.itemupgrader.api.type.UpgradeCondition;
import io.legom.itemupgrader.api.type.UpgradeResult;
import io.legom.itemupgrader.upgrades.ItemUpgradeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SimpleUpgradeAction extends ConditionalUpgradeAction {
	
	private final ImmutableList<UpgradeResult> results;
	private final Serializer serializer;
	
	public SimpleUpgradeAction(IUpgradeInternals internals, List<UpgradeCondition> conditions, List<UpgradeResult> results, Serializer serializer) {
		super(internals, conditions);
		this.results = ImmutableList.copyOf(results);
		this.serializer = serializer;
	}
	
	@Override
	public void execute(UpgradeEventData event) {
		for (UpgradeResult result : this.results) {
			result.execute(event);
		}
	}
	
	@Override
	public MutableComponent getResultTooltip(ItemStack stack) {
		List<MutableComponent> resultComponents = new ArrayList<>(this.results.size());
		for (UpgradeResult result : this.results) {
			if (result.isVisible()) resultComponents.add(new TranslatableComponent("upgradeResult." + ComponentHelper.keyFormat(result.getId()), (Object[]) result.getTooltipWithOverride(stack)));
		}
		return ComponentHelper.andList(resultComponents);
	}
	
	@Override
	public Serializer getSerializer() {
		return serializer;
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends ConditionalUpgradeActionSerializer<SimpleUpgradeAction> {
		
		private final UpgradeEntrySet provided;
		
		public Serializer(UpgradeEntrySet provided) {
			this.provided = provided;
		}
		
		@Override
		public UpgradeEntrySet providedData() {
			return provided;
		}
		
		@Override
		public SimpleUpgradeAction fromJson(IUpgradeInternals internals, JsonObject json) {
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
			return new SimpleUpgradeAction(internals, conditions, results, this);
		}
		
		private void safeAddResult(List<UpgradeResult> results, UpgradeResult result) {
			Set<UpgradeEntry<?>> test = Sets.difference(result.requiredData().getRequired(), this.providedData().getRequired());
			if (test.isEmpty()) {
				results.add(result);
			} else {
				Iterator<UpgradeEntry<?>> iter = test.iterator();
				String errStr = iter.next().toString();
				while (iter.hasNext()) errStr = errStr + ", " + iter.next();
				throw new IllegalArgumentException("Missing required entries for result " + result.getId() + " - [" + errStr + "]");
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
		
		public SimpleUpgradeAction fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
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
			return new SimpleUpgradeAction(internals, netConditions, netResults, this);
		}
		
	}
	
}