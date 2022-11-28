package io._3650.itemupgrader.upgrades.results;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.api.util.UpgradeSerializer;
import io._3650.itemupgrader.api.util.UpgradeTooltipHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class CompoundUpgradeResult extends UpgradeResult {
	
	private final ArrayList<UpgradeResult> results;
	
	public CompoundUpgradeResult(IUpgradeInternals internals, ArrayList<UpgradeResult> results) {
		super(internals, UpgradeEntrySet.create(builder -> {
			for (var result : results) builder.combine(result.getRequiredData());
		}));
		this.results = results;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		for (var result : this.results) UpgradeEventData.InternalStuffIgnorePlease.setSuccess(data, result.execute(data)); 
		return data.getLastResultSuccess();
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		ArrayList<MutableComponent> resultComponents = new ArrayList<>(this.results.size());
		for (var result : this.results) {
			if (result.isVisible()) {
				resultComponents.add(UpgradeTooltipHelper.result(result, stack));
			}
		}
		return ComponentHelper.arrayify(ComponentHelper.andList(resultComponents));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<CompoundUpgradeResult> {
		
		@Override
		public CompoundUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			JsonArray jsonResults = GsonHelper.getAsJsonArray(json, "results");
			ArrayList<UpgradeResult> results = new ArrayList<>(jsonResults.size());
			for (var jsonResult : jsonResults) results.add(UpgradeSerializer.resultFromObject(jsonResult.getAsJsonObject()));
			return new CompoundUpgradeResult(internals, results);
		}
		
		@Override
		public void toNetwork(CompoundUpgradeResult result, FriendlyByteBuf buf) {
			buf.writeInt(result.results.size());
			for (var res : result.results) UpgradeSerializer.resultToNetwork(res, buf);
		}
		
		@Override
		public CompoundUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			int resultsSize = buf.readInt();
			ArrayList<UpgradeResult> results = new ArrayList<>(resultsSize);
			for (int i = 0; i < resultsSize; i++) results.add(UpgradeSerializer.resultFromNetwork(buf));
			return new CompoundUpgradeResult(internals, results);
		}
		
	}
	
}