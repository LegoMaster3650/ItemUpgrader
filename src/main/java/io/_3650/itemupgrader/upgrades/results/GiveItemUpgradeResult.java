package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

public class GiveItemUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Player> playerEntry;
	private final ItemStack stack;
	
	public GiveItemUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Player> playerEntry, ItemStack stack) {
		super(internals, UpgradeEntrySet.PLAYER.fillCategories(mapper -> {
			mapper.set(EntryCategory.PLAYER, playerEntry);
		}));
		this.playerEntry = playerEntry;
		this.stack = stack;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		if (!(data.getEntry(this.playerEntry) instanceof ServerPlayer player)) return false;
		ItemStack stack1 = this.stack.copy();
		if (player.addItem(stack1)) return true;
		else {
			player.drop(stack1, false);
			return false;
		}
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return new MutableComponent[] {new TextComponent("" + this.stack.getCount()), new TranslatableComponent(this.stack.getDescriptionId())};
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeResultSerializer<GiveItemUpgradeResult> {
		
		@Override
		public GiveItemUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			ItemStack stack = CraftingHelper.getItemStack(json, true, true);
			return new GiveItemUpgradeResult(internals, playerEntry, stack);
		}
		
		@Override
		public void toNetwork(GiveItemUpgradeResult result, FriendlyByteBuf buf) {
			result.playerEntry.toNetwork(buf);
			buf.writeItemStack(result.stack, false);
		}
		
		@Override
		public GiveItemUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			ItemStack stack = buf.readItem();
			return new GiveItemUpgradeResult(internals, playerEntry, stack);
		}
		
	}
	
}