package io._3650.itemupgrader.upgrades.results.special;

import java.util.Optional;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.mixin.ServerPlayerInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PlayerSpawnpointUpgradeResult extends UpgradeResult {
	
	public PlayerSpawnpointUpgradeResult(IUpgradeInternals internals) {
		super(internals, UpgradeEntrySet.create(builder -> builder.require(UpgradeEntry.LIVING)));
	}
	
	//SPECIAL RETURN: Did cross-dimensional teleport?
	@Override
	public boolean execute(UpgradeEventData data) {
		if (!(data.getEntry(UpgradeEntry.LIVING) instanceof ServerPlayer player)) return false;
		if (player.level.isClientSide) return false;
		
		BlockPos blockPos = player.getRespawnPosition();
		float f = player.getRespawnAngle();
		boolean flag = player.isRespawnForced();
		ServerLevel level = player.server.getLevel(player.getRespawnDimension());
		
		level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		
		Optional<Vec3> optional;
		if (level != null && blockPos != null) {
			optional = Player.findRespawnPositionAndUseSpawnBlock(level, blockPos, f, flag, true);
		} else {
			optional = Optional.empty();
		}
		ServerLevel level1 = level != null && optional.isPresent() ? level : player.server.overworld();
		BlockPos spawnPos = level1.getSharedSpawnPos();
		player.moveTo((double) spawnPos.getX() + 0.5D, (double) (spawnPos.getY() + 1), (double) spawnPos.getZ() + 0.5D, level1.getSharedSpawnAngle(), 0.0F);
		((ServerPlayerInvoker) player).callFudgeSpawnLocation(level1);
		
		if (optional.isPresent()) {
			BlockState blockState = level1.getBlockState(blockPos);
			Vec3 vec3 = optional.get();
			float f1;
			if (!blockState.is(BlockTags.BEDS) && !blockState.is(Blocks.RESPAWN_ANCHOR)) {
				f1 = f;
			} else {
				Vec3 vec31 = Vec3.atBottomCenterOf(blockPos).subtract(vec3).normalize();
				f1 = (float) Mth.wrapDegrees(Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI) - 90.0D);
			}
			player.moveTo(vec3.x, vec3.y, vec3.z, f1, 0.0F);
			player.setRespawnPosition(level1.dimension(), blockPos, f, flag, false);
		} else if (blockPos != null) {
			player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
		}
		
		while (!level1.noCollision(player) && player.getY() < (double) level1.getMaxBuildHeight()) {
			player.setPos(player.getX(), player.getY() + 1.0D, player.getZ());
		}
		
		Vec3 teleportPosition = player.position();
		ServerLevel teleportLevel = level1;
		
		player.unRide();
		player.setDeltaMovement(0, 0, 0);
		player.fallDistance = 0;
		teleportLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(new BlockPos(teleportPosition.x, teleportPosition.y, teleportPosition.z)), 1, player.getId());
		
		if (player.level == level1) {
			player.teleportTo(player.getX(), player.getY(), player.getZ());
			return false;
		} else {
			player.teleportTo(teleportLevel, player.getX(), player.getY(), player.getZ(), player.getViewXRot(1), 0);
			teleportLevel.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.3F, 1.0F);
			return true;
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
	
	public static class Serializer extends UpgradeResultSerializer<PlayerSpawnpointUpgradeResult> {
		
		@Override
		public PlayerSpawnpointUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			return new PlayerSpawnpointUpgradeResult(internals);
		}
		
		@Override
		public void toNetwork(PlayerSpawnpointUpgradeResult result, FriendlyByteBuf buf) {
			// nothing to write
		}
		
		@Override
		public PlayerSpawnpointUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			return new PlayerSpawnpointUpgradeResult(internals);
		}
		
	}
	
}