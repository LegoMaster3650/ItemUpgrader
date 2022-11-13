package io._3650.itemupgrader.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io._3650.itemupgrader.api.ItemUpgraderApi;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.registry.ModUpgradeActions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Block.class)
public abstract class BlockMixin {
	
	@Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
	private static void itemupgrader_getDrops(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
		List<ItemStack> originalDrops = cir.getReturnValue();
		
		UpgradeEventData data = ItemUpgraderApi.runActions(ModUpgradeActions.BLOCK_DROPS, new UpgradeEventData.Builder(entity)
				.entry(UpgradeEntry.LEVEL, level)
				.entry(UpgradeEntry.ITEM, stack)
				.entry(UpgradeEntry.BLOCK_STATE, state)
				.entry(UpgradeEntry.BLOCK_POS, pos)
				.modifiableEntry(UpgradeEntry.BLOCK_DROPS, originalDrops));
		
		List<ItemStack> newDrops = data.getEntry(UpgradeEntry.BLOCK_DROPS);
		if (newDrops != originalDrops) cir.setReturnValue(newDrops);
	}
	
}