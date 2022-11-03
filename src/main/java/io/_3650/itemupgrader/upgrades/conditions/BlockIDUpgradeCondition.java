package io._3650.itemupgrader.upgrades.conditions;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BlockIDUpgradeCondition extends UpgradeCondition {
	
	private final ResourceLocation block;
	
	public BlockIDUpgradeCondition(IUpgradeInternals internals, boolean inverted, ResourceLocation block) {
		super(internals, inverted, UpgradeEntrySet.BLOCK_STATE);
		this.block = block;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		BlockState state = data.getEntry(UpgradeEntry.BLOCK_STATE);
		return state.getBlock().getRegistryName().equals(this.block);
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Nullable
	private String descriptionId;
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		if (this.descriptionId == null) this.descriptionId = Util.makeDescriptionId("block", this.block);
		return ComponentHelper.arrayify(new TranslatableComponent(this.descriptionId));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<BlockIDUpgradeCondition> {
		
		@Override
		public BlockIDUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			ResourceLocation block = new ResourceLocation(GsonHelper.getAsString(json, "block"));
			return new BlockIDUpgradeCondition(internals, inverted, block);
		}
		
		@Override
		public void toNetwork(BlockIDUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeResourceLocation(condition.block);
		}
		
		@Override
		public BlockIDUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			ResourceLocation block = buf.readResourceLocation();
			return new BlockIDUpgradeCondition(internals, inverted, block);
		}
		
	}
	
}