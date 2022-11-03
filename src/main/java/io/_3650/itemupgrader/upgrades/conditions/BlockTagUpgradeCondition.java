package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockTagUpgradeCondition extends UpgradeCondition {
	
	private final ResourceLocation tagId;
	private final TagKey<Block> tag;
	
	public BlockTagUpgradeCondition(IUpgradeInternals internals, boolean inverted, ResourceLocation tagId) {
		super(internals, inverted, UpgradeEntrySet.BLOCK_STATE);
		this.tagId = tagId;
		this.tag = ForgeRegistries.BLOCKS.tags().createTagKey(tagId);
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		BlockState state = data.getEntry(UpgradeEntry.BLOCK_STATE);
		return state.is(this.tag);
	}
	
	private final Serializer instance = new Serializer();
	
	@Override
	public Serializer getSerializer() {
		return instance;
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TextComponent(this.tagId.toString()));
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<BlockTagUpgradeCondition> {
		
		@Override
		public BlockTagUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			ResourceLocation tagId = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
			return new BlockTagUpgradeCondition(internals, inverted, tagId);
		}
		
		@Override
		public void toNetwork(BlockTagUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeResourceLocation(condition.tagId);
		}
		
		@Override
		public BlockTagUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			ResourceLocation tagId = buf.readResourceLocation();
			return new BlockTagUpgradeCondition(internals, inverted, tagId);
		}
		
	}
	
}