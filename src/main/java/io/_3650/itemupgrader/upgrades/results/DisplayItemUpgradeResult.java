package io._3650.itemupgrader.upgrades.results;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.network.DisplayItemPacket;
import io._3650.itemupgrader.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

public class DisplayItemUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Player> playerEntry;
	@Nullable
	private final UpgradeEntry<ItemStack> itemEntry;
	private final ItemStack overrideItem;
	
	public DisplayItemUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Player> playerEntry, UpgradeEntry<ItemStack> itemEntry, ItemStack overrideItem) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(playerEntry);
			if (overrideItem.isEmpty()) builder.require(itemEntry);
		}));
		this.playerEntry = playerEntry;
		this.itemEntry = itemEntry;
		this.overrideItem = overrideItem;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		if (!(data.getEntry(this.playerEntry) instanceof ServerPlayer player)) return false;
		ItemStack stack = this.overrideItem.isEmpty() ? data.getEntry(this.itemEntry) : this.overrideItem;
		NetworkHandler.sendToPlayer(player, new DisplayItemPacket(stack));
		return true;
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
	
	public static class Serializer extends UpgradeResultSerializer<DisplayItemUpgradeResult> {
		
		@Override
		public DisplayItemUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.getDefaultValue();
			ItemStack overrideItem = ItemStack.EMPTY;
			if (GsonHelper.isObjectNode(json, "item")) {
				overrideItem = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "item"), true);
			} else if (GsonHelper.isStringValue(json, "item")) {
				itemEntry = EntryCategory.ITEM.fromJson(json, "item");
			} else {
				itemEntry = EntryCategory.ITEM.getDefaultValue();
			}
			return new DisplayItemUpgradeResult(internals, playerEntry, itemEntry, overrideItem);
		}
		
		@Override
		public void toNetwork(DisplayItemUpgradeResult result, FriendlyByteBuf buf) {
			result.playerEntry.toNetwork(buf);
			result.itemEntry.toNetwork(buf);
			buf.writeItemStack(result.overrideItem, false);
		}
		
		@Override
		public DisplayItemUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			ItemStack overrideItem = buf.readItem();
			return new DisplayItemUpgradeResult(internals, playerEntry, itemEntry, overrideItem);
		}
		
	}
	
}