package io._3650.itemupgrader.upgrades.results.special;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.network.NetworkHandler;
import io._3650.itemupgrader.network.TellPlayerTimePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TellTimeUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Player> playerEntry;
	
	public TellTimeUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Player> playerEntry) {
		super(internals, UpgradeEntrySet.PLAYER.fillCategories(mapper -> {
			mapper.set(EntryCategory.PLAYER, playerEntry);
		}));
		this.playerEntry = playerEntry;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		if (!(data.getEntry(this.playerEntry) instanceof ServerPlayer player)) return false;
		if (player.level.isClientSide) return false;
		long worldTime = player.level.getDayTime() + 6000L; //aligned with midnight instead of 6 AM
		
		//SEND TO CLIENT
		NetworkHandler.sendToPlayer(player, new TellPlayerTimePacket(worldTime));
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
	
	public static class Serializer extends UpgradeResultSerializer<TellTimeUpgradeResult> {
		
		@Override
		public TellTimeUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			return new TellTimeUpgradeResult(internals, playerEntry);
		}
		
		@Override
		public void toNetwork(TellTimeUpgradeResult result, FriendlyByteBuf buf) {
			result.playerEntry.toNetwork(buf);
		}
		
		@Override
		public TellTimeUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			return new TellTimeUpgradeResult(internals, playerEntry);
		}
		
	}
	
}