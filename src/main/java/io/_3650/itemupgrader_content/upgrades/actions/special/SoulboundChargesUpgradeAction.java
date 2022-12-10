package io._3650.itemupgrader_content.upgrades.actions.special;

import java.util.Set;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.slot.InventorySlot;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader_content.registry.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SoulboundChargesUpgradeAction extends UpgradeAction {
	
	private final String tagName;
	private final int amount;
	
	public SoulboundChargesUpgradeAction(IUpgradeInternals internals, Set<InventorySlot> validSlots, String tagName, int amount) {
		super(internals, validSlots);
		this.tagName = tagName;
		this.amount = amount;
	}
	
	@Override
	public MutableComponent applyTooltip(MutableComponent tooltip, ItemStack stack) {
		return tooltip;
	}
	
	@Override
	public void run(UpgradeEventData data) {
		//do nothing!
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		int stackCharges = stack.getOrCreateTag().getInt(this.tagName);
		if (stackCharges == -1)return ComponentHelper.arrayify(Component.translatable("tooltip.itemupgrader.unlimited_soulbound"));
		if (stackCharges == 0) return ComponentHelper.arrayify(Component.literal("0").withStyle(ChatFormatting.RED));
		return ComponentHelper.arrayify(Component.literal(Integer.valueOf(stackCharges).toString()));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeActionSerializer<SoulboundChargesUpgradeAction> {
		
		@Override
		public UpgradeEntrySet getProvidedData() {
			return UpgradeEntrySet.create(builder -> builder.require(UpgradeEntry.ITEM));
		}
		
		@Override
		public SoulboundChargesUpgradeAction fromJson(IUpgradeInternals internals, Set<InventorySlot> validSlots, JsonObject json) {
			String tagName = GsonHelper.getAsString(json, "tag");
			int amount = Config.COMMON.echoCharges.get();
			return new SoulboundChargesUpgradeAction(internals, validSlots, tagName, amount);
		}
		
		@Override
		public void toNetwork(SoulboundChargesUpgradeAction action, FriendlyByteBuf buf) {
			buf.writeUtf(action.tagName);
			buf.writeInt(action.amount);
		}
		
		@Override
		public SoulboundChargesUpgradeAction fromNetwork(IUpgradeInternals internals, Set<InventorySlot> validSlots, FriendlyByteBuf buf) {
			String tagName = buf.readUtf();
			int amount = buf.readInt();
			return new SoulboundChargesUpgradeAction(internals, validSlots, tagName, amount);
		}
		
	}
	
}