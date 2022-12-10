package io._3650.itemupgrader_content.upgrades.results.special;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader_content.registry.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SoulboundChargesUpgradeResult extends UpgradeResult {
	
	private final String tagName;
	private final int amount;
	private final String amountStr;
	
	public SoulboundChargesUpgradeResult(IUpgradeInternals internals, String tagName, int amount) {
		super(internals, UpgradeEntrySet.create(builder -> builder.require(UpgradeEntry.ITEM)));
		this.tagName = tagName;
		this.amount = amount;
		this.amountStr = Integer.valueOf(amount).toString();
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		ItemStack stack = data.getEntry(UpgradeEntry.ITEM);
		CompoundTag tag = stack.getOrCreateTag();
		if (this.amount == -1) tag.putInt(this.tagName, -1);
		else tag.putInt(this.tagName, tag.getInt(this.tagName) + this.amount);
		stack.setTag(tag);
		return false;
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(Component.literal(this.amountStr));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<SoulboundChargesUpgradeResult> {
		
		@Override
		public SoulboundChargesUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			String tagName = GsonHelper.getAsString(json, "tag");
			int amount = Config.COMMON.echoCharges.get();
			return new SoulboundChargesUpgradeResult(internals, tagName, amount);
		}
		
		@Override
		public void toNetwork(SoulboundChargesUpgradeResult result, FriendlyByteBuf buf) {
			buf.writeUtf(result.tagName);
			buf.writeInt(result.amount);
		}
		
		@Override
		public SoulboundChargesUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			String tagName = buf.readUtf();
			int amount = buf.readInt();
			return new SoulboundChargesUpgradeResult(internals, tagName, amount);
		}
		
	}
	
}