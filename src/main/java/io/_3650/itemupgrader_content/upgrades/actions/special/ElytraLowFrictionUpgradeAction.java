package io._3650.itemupgrader_content.upgrades.actions.special;

import java.util.Set;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeActionSerializer;
import io._3650.itemupgrader.api.slot.InventorySlot;
import io._3650.itemupgrader.api.type.UpgradeAction;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ElytraLowFrictionUpgradeAction extends UpgradeAction {
	
	public ElytraLowFrictionUpgradeAction(IUpgradeInternals internals, Set<InventorySlot> validSlots) {
		super(internals, validSlots);
	}
	
	@Override
	public MutableComponent applyTooltip(MutableComponent tooltip, ItemStack stack) {
		return tooltip;
	}
	
	@Override
	public void run(UpgradeEventData data) {
		data.cancel();
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.empty();
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeActionSerializer<ElytraLowFrictionUpgradeAction> {
		
		@Override
		public UpgradeEntrySet getProvidedData() {
			return UpgradeEntrySet.LIVING_SLOT_ITEM.with(builder -> builder.cancellable());
		}
		
		@Override
		public ElytraLowFrictionUpgradeAction fromJson(IUpgradeInternals internals, Set<InventorySlot> validSlots, JsonObject json) {
			return new ElytraLowFrictionUpgradeAction(internals, validSlots);
		}
		
		@Override
		public void toNetwork(ElytraLowFrictionUpgradeAction action, FriendlyByteBuf buf) {
			//no
		}
		
		@Override
		public ElytraLowFrictionUpgradeAction fromNetwork(IUpgradeInternals internals, Set<InventorySlot> validSlots, FriendlyByteBuf buf) {
			return new ElytraLowFrictionUpgradeAction(internals, validSlots);
		}
		
	}
	
}