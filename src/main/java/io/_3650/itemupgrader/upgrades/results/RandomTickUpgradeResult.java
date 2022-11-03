package io._3650.itemupgrader.upgrades.results;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RandomTickUpgradeResult extends UpgradeResult {
	
	public RandomTickUpgradeResult(IUpgradeInternals internals) {
		super(internals, UpgradeEntrySet.LEVEL);
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		if (!(data.getEntry(UpgradeEntry.LEVEL) instanceof ServerLevel level)) return false;
		BlockPos pos = data.getEntry(UpgradeEntry.BLOCK_POS);
		BlockState state = level.getBlockState(pos);
		state.randomTick(level, pos, level.getRandom());
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
	
	public static class Serializer extends UpgradeResultSerializer<RandomTickUpgradeResult> {
		
		@Override
		public RandomTickUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			return new RandomTickUpgradeResult(internals);
		}
		
		@Override
		public void toNetwork(RandomTickUpgradeResult result, FriendlyByteBuf buf) {
			// nothing to write
		}
		
		@Override
		public RandomTickUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			return new RandomTickUpgradeResult(internals);
		}
		
	}
	
}