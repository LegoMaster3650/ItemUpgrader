package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.api.util.UpgradeSerializer;
import io._3650.itemupgrader.api.util.UpgradeTooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class ConditionalUpgradeResult extends UpgradeResult {
	
	private final UpgradeCondition condition;
	private final UpgradeResult result;
	private final UpgradeResult elseResult;
	
	public ConditionalUpgradeResult(IUpgradeInternals internals, UpgradeCondition condition, UpgradeResult result, UpgradeResult elseResult) {
		super(internals, condition.getRequiredData().with(builder -> {
			builder.combine(result.getRequiredData());
			if (elseResult != null) builder.combine(elseResult.getRequiredData());
		}));
		this.condition = condition;
		this.result = result;
		this.elseResult = elseResult;
	}
	
	// funny how simple this is
	// you'd think the super meta-result would be more than 3 lines of code
	@Override
	public boolean execute(UpgradeEventData data) {
		if (this.condition.test(data)) return this.result.execute(data);
		else if (this.elseResult != null) return this.elseResult.execute(data);
		else return false;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		MutableComponent tooltip = new TranslatableComponent("tooltip.itemupgrader.if", UpgradeTooltipHelper.condition(this.condition, stack)).withStyle(ChatFormatting.BLUE)
				.append(UpgradeTooltipHelper.result(this.result, stack));
		if (this.elseResult != null) tooltip.append(new TranslatableComponent("tooltip.itemupgrader.else")).append(UpgradeTooltipHelper.result(this.elseResult, stack));
		return ComponentHelper.arrayify(tooltip);
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<ConditionalUpgradeResult> {
		
		@Override
		public ConditionalUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeCondition condition = UpgradeSerializer.condition(GsonHelper.getAsJsonObject(json, "condition"));
			UpgradeResult result = UpgradeSerializer.result(GsonHelper.getAsJsonObject(json, "result"));
			UpgradeResult elseResult = GsonHelper.isObjectNode(json, "else") ? UpgradeSerializer.result(GsonHelper.getAsJsonObject(json, "else")) : null;
			return new ConditionalUpgradeResult(internals, condition, result, elseResult);
		}
		
		@Override
		public void toNetwork(ConditionalUpgradeResult result, FriendlyByteBuf buf) {
			UpgradeSerializer.conditionToNetwork(result.condition, buf);
			UpgradeSerializer.resultToNetwork(result.result, buf);
			boolean hasElseResult = result.elseResult != null;
			buf.writeBoolean(hasElseResult);
			if (hasElseResult) UpgradeSerializer.resultToNetwork(result.elseResult, buf);
		}
		
		@Override
		public ConditionalUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeCondition condition = UpgradeSerializer.conditionFromNetwork(buf);
			UpgradeResult result = UpgradeSerializer.resultFromNetwork(buf);
			UpgradeResult elseResult = buf.readBoolean() ? UpgradeSerializer.resultFromNetwork(buf) : null;
			return new ConditionalUpgradeResult(internals, condition, result, elseResult);
		}
		
	}
	
}