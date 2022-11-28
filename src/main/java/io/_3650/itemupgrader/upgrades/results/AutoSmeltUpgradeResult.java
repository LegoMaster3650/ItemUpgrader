package io._3650.itemupgrader.upgrades.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeResultSerializer;
import io._3650.itemupgrader.api.type.UpgradeResult;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.recipes.SmeltingRecipeGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class AutoSmeltUpgradeResult extends UpgradeResult {
	
	public AutoSmeltUpgradeResult(IUpgradeInternals internals) {
		super(internals, UpgradeEntrySet.BLOCK_DROPS);
	}
	
	@Override
	public boolean execute(UpgradeEventData data) {
		List<ItemStack> originalDrops = data.getEntry(UpgradeEntry.BLOCK_DROPS);
		
		if (originalDrops.size() == 0) return false;
		
		if (!(data.getEntry(UpgradeEntry.LEVEL) instanceof ServerLevel level)) return false;
		BlockPos pos = data.getEntry(UpgradeEntry.BLOCK_POS);
		
		List<ItemStack> newDrops = new ArrayList<>(originalDrops.size());
		
		for (ItemStack original : originalDrops) {
			Optional<SmeltingRecipe> optionalRecipe = SmeltingRecipeGetter.getRecipe(original, level);
			if (optionalRecipe.isPresent()) {
				ItemStack newItem = optionalRecipe.get().getResultItem().copy();
				newItem.setCount(original.getCount());
				newDrops.add(newItem);
				level.sendParticles(ParticleTypes.FLAME, pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D, original.getCount(), .14, .14, .14, .02);
			} else {
				newDrops.add(original);
			}
		}
		
		data.setModifiableEntry(UpgradeEntry.BLOCK_DROPS, newDrops);
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
	
	public static class Serializer extends UpgradeResultSerializer<AutoSmeltUpgradeResult> {
		
		@Override
		public AutoSmeltUpgradeResult fromJson(IUpgradeInternals internals, JsonObject json) {
			return new AutoSmeltUpgradeResult(internals);
		}
		
		@Override
		public void toNetwork(AutoSmeltUpgradeResult result, FriendlyByteBuf buf) {
			
		}
		
		@Override
		public AutoSmeltUpgradeResult fromNetwork(IUpgradeInternals internals, FriendlyByteBuf buf) {
			return new AutoSmeltUpgradeResult(internals);
		}
		
	}
	
}