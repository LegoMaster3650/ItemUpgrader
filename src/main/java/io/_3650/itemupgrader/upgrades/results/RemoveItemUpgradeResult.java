package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class RemoveItemUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<ItemStack> itemEntry;
	private final int amount;
	
	public RemoveItemUpgradeResult(IUpgradeInternals internals, UpgradeEntry<ItemStack> itemEntry, int amount) {
		super(internals, UpgradeEntrySet.EMPTY.fillCategories(mapper -> {
			mapper.set(EntryCategory.ITEM, itemEntry);
		}));
		this.itemEntry = itemEntry;
		this.amount = amount;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		ItemStack stack = data.getEntry(this.itemEntry);
		stack.shrink(this.amount);
		return !stack.isEmpty();
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent("" + this.amount));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<RemoveItemUpgradeResult> {
		
		@Override
		public RemoveItemUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			int amount = GsonHelper.getAsInt(json, "amount");
			return new RemoveItemUpgradeResult(internals, itemEntry, amount);
		}
		
		@Override
		public void toNetwork(RemoveItemUpgradeResult result, FriendlyByteBuf buf) {
			result.itemEntry.toNetwork(buf);
			buf.writeInt(result.amount);
		}
		
		@Override
		public RemoveItemUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			int amount = buf.readInt();
			return new RemoveItemUpgradeResult(internals, itemEntry, amount);
		}
		
	}
	
}