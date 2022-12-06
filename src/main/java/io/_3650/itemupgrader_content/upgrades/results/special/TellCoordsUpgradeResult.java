package io._3650.itemupgrader_content.upgrades.results.special;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader_content.network.NetworkHandler;
import io._3650.itemupgrader_content.network.TellPlayerCoordsPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class TellCoordsUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Player> playerEntry;
	
	public TellCoordsUpgradeResult(IUpgradeInternals internals, UpgradeEntry<Player> playerEntry) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(playerEntry);
		}));
		this.playerEntry = playerEntry;
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		if (!(data.getEntry(this.playerEntry) instanceof ServerPlayer player)) return false;
		if (player.level.isClientSide) return false;
		Vec3 pos = player.position();
		
		//SEND TO CLIENT
		NetworkHandler.sendToPlayer(player, new TellPlayerCoordsPacket(pos));
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
	
	public static class Serializer extends UpgradeResultSerializer<TellCoordsUpgradeResult> {
		
		@Override
		public TellCoordsUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			return new TellCoordsUpgradeResult(internals, playerEntry);
		}
		
		@Override
		public void toNetwork(TellCoordsUpgradeResult result, FriendlyByteBuf buf) {
			result.playerEntry.toNetwork(buf);
		}
		
		@Override
		public TellCoordsUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			return new TellCoordsUpgradeResult(internals, playerEntry);
		}
		
	}
	
}