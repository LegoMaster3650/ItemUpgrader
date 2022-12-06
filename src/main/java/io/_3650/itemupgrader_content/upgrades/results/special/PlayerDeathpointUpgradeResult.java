package io._3650.itemupgrader_content.upgrades.results.special;

import java.util.Optional;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

public class PlayerDeathpointUpgradeResult extends UpgradeResult {
	
	public PlayerDeathpointUpgradeResult(IUpgradeInternals internals) {
		super(internals, UpgradeEntrySet.create(builder -> builder.require(UpgradeEntry.PLAYER)));
	}
	
	//SPECIAL RETURN: Did cross-dimensional teleport?
	@Override
	public boolean execute(UpgradeEventData data) { //TO BE ADDED IN 1.19, death pos tracker missing for now
		if (!(data.getEntry(UpgradeEntry.PLAYER) instanceof ServerPlayer player)) return false;
		if (player.level.isClientSide) return false;
		Optional<GlobalPos> optional = player.getLastDeathLocation();
		if (optional.isPresent()) {
			GlobalPos globalPos = optional.get();
			ServerLevel level = globalPos.dimension() == null ? player.getLevel() : player.server.getLevel(globalPos.dimension());
			BlockPos pos = globalPos.pos();
			
			player.unRide();
			player.setDeltaMovement(0, 0, 0);
			player.fallDistance = 0;
			level.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(pos), 1, player.getId());
			
			if (player.level == level) {
				player.teleportTo(pos.getX(), pos.getY(), pos.getZ());
				return true;
			} else {
				player.teleportTo(level, pos.getX(), pos.getY(), pos.getZ(), player.getRespawnAngle(), 0.5F);
				return true;
			}
		}
		return false;
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
	
	public static class Serializer extends UpgradeResultSerializer<PlayerDeathpointUpgradeResult> {
		
		@Override
		public PlayerDeathpointUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			return new PlayerDeathpointUpgradeResult(internals);
		}
		
		@Override
		public void toNetwork(PlayerDeathpointUpgradeResult result, FriendlyByteBuf buf) {
			// nothing to write
		}
		
		@Override
		public PlayerDeathpointUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			return new PlayerDeathpointUpgradeResult(internals);
		}
		
	}
	
}