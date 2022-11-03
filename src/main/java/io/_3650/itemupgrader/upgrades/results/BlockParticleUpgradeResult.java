package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;
import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.api.util.UpgradeJsonHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockParticleUpgradeResult extends UpgradeResult {
	
	private final UpgradeEntry<Vec3> posEntry;
	private final Vec3 offset;
	private int count;
	private final Vec3 delta;
	private double speed;
	private final UpgradeEntry<Player> playerEntry;
	private final boolean clientOnly;
	
	public BlockParticleUpgradeResult(IUpgradeInternals internals,
			UpgradeEntry<Vec3> posEntry,
			Vec3 offset,
			int count,
			Vec3 delta,
			double speed,
			UpgradeEntry<Player> playerEntry,
			boolean clientOnly) {
		super(internals, UpgradeEntrySet.create(builder -> {
			builder.require(UpgradeEntry.SIDE).require(UpgradeEntry.LEVEL).require(UpgradeEntry.BLOCK_STATE);
		}).fillCategories(mapper -> {
			mapper.set(EntryCategory.POSITION, posEntry).set(EntryCategory.PLAYER, playerEntry);
		}));
		this.posEntry = posEntry;
		this.offset = offset;
		this.count = count;
		this.delta = delta;
		this.speed = speed;
		this.playerEntry = playerEntry;
		this.clientOnly = clientOnly;
	}
	
	private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
	
	@Override
	public boolean execute(UpgradeEventData data) {
		if (!(data.getEntry(UpgradeEntry.LEVEL) instanceof ServerLevel level)) return false;
		Vec3 spawnPos = data.getEntry(this.posEntry).add(this.offset);
		BlockState state = data.getEntry(UpgradeEntry.BLOCK_STATE);
		@SuppressWarnings("deprecation") //L + ratio cope and seethe (why do I think this will backfire in 1.19)
		BlockParticleOption options = BlockParticleOption.DESERIALIZER.fromNetwork(ParticleTypes.BLOCK, new FriendlyByteBuf(Unpooled.buffer()).writeVarInt(Block.getId(state)));
		if (this.clientOnly && data.getEntry(this.playerEntry) instanceof ServerPlayer player) {
			LOGGER.debug("client");
			level.sendParticles(player, options, false, spawnPos.x, spawnPos.y, spawnPos.z, this.count, this.delta.x, this.delta.y, this.delta.z, this.speed);
			return true;
		} else if (!this.clientOnly) {
			LOGGER.debug("server");
			level.sendParticles(options, spawnPos.x, spawnPos.y, spawnPos.z, this.count, this.delta.x, this.delta.y, this.delta.z, this.speed);
			return true;
		}
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
	
	public static class Serializer extends UpgradeResultSerializer<BlockParticleUpgradeResult> {
		
		@Override
		public BlockParticleUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromJson(json);
			Vec3 offset = UpgradeJsonHelper.getPosition(json, "offset");
			int count = GsonHelper.getAsInt(json, "count", 1);
			Vec3 delta = UpgradeJsonHelper.getPosition(json, "delta");
			double speed = GsonHelper.getAsDouble(json, "speed", 0.0F);
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromJson(json);
			boolean clientOnly = GsonHelper.getAsBoolean(json, "client_only", false);
			return new BlockParticleUpgradeResult(internals, posEntry, offset, count, delta, speed, playerEntry, clientOnly);
		}
		
		@Override
		public void toNetwork(BlockParticleUpgradeResult result, FriendlyByteBuf buf) {
			result.posEntry.toNetwork(buf);
			buf.writeDouble(result.offset.x).writeDouble(result.offset.y).writeDouble(result.offset.z);
			buf.writeInt(result.count);
			buf.writeDouble(result.delta.x).writeDouble(result.delta.y).writeDouble(result.delta.z);
			buf.writeDouble(result.speed);
			result.playerEntry.toNetwork(buf);
			buf.writeBoolean(result.clientOnly);
		}
		
		@Override
		public BlockParticleUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.getEntry(buf.readResourceLocation());
			Vec3 offset = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			int count = buf.readInt();
			Vec3 delta = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			double speed = buf.readDouble();
			UpgradeEntry<Player> playerEntry = EntryCategory.PLAYER.fromNetwork(buf);
			boolean clientOnly = buf.readBoolean();
			return new BlockParticleUpgradeResult(internals, posEntry, offset, count, delta, speed, playerEntry, clientOnly);
		}
		
	}
	
}