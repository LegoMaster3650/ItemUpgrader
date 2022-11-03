package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class UpgradeRemoveUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Player> playerEntry;
	private final UpgradeEntry<ItemStack> itemEntry;
	private final boolean wholeStack;
	private final boolean ignoreCreative;
	
	public UpgradeRemoveUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Player> playerEntry, UpgradeEntry<ItemStack> itemEntry, boolean wholeStack, boolean ignoreCreative) {
		super(internals, UpgradeEntrySet.PLAYER_ITEM.fillCategories(mapper -> {
			mapper.set(EntryCategory.PLAYER, playerEntry).set(EntryCategory.ITEM, itemEntry);
		}));
		this.playerEntry = playerEntry;
		this.itemEntry = itemEntry;
		this.wholeStack = wholeStack;
		this.ignoreCreative = ignoreCreative;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		Player player = data.getEntry(this.playerEntry);
		if (player.level.isClientSide) return false;
		if (player.getAbilities().instabuild && !this.ignoreCreative) return false;
		ItemStack stack = data.getEntry(this.itemEntry);
		if (this.wholeStack) {
			ItemUpgraderApi.removeUpgrade(stack);
			return true;
		} else {
			return ItemUpgraderApi.removeUpgradeFromStack(player, stack);
		}
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
	
	public static class Serializer extends UpgradeResultSerializer<UpgradeRemoveUpgradeResult> {
		
		@Override
		public UpgradeRemoveUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			boolean wholeStack = GsonHelper.getAsBoolean(json, "whole_stack", false);
			boolean ignoreCreative = GsonHelper.getAsBoolean(json, "ignore_creative", false);
			return new UpgradeRemoveUpgradeResult(internals, playerEntry, itemEntry, wholeStack, ignoreCreative);
		}
		
		@Override
		public void toNetwork(UpgradeRemoveUpgradeResult result, FriendlyByteBuf buf) {
			result.playerEntry.toNetwork(buf);
			result.itemEntry.toNetwork(buf);
			buf.writeBoolean(result.wholeStack);
			buf.writeBoolean(result.ignoreCreative);
		}
		
		@Override
		public UpgradeRemoveUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			boolean wholeStack = buf.readBoolean();
			boolean ignoreCreative = buf.readBoolean();
			return new UpgradeRemoveUpgradeResult(internals, playerEntry, itemEntry, wholeStack, ignoreCreative);
		}
		
	}
	
}