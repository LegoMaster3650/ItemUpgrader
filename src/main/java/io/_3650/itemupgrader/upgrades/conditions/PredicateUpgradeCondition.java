package io._3650.itemupgrader.upgrades.conditions;

import com.google.gson.JsonObject;

import io._3650.itemupgrader.api.data.EntryCategory;
import io._3650.itemupgrader.api.data.UpgradeEntry;
import io._3650.itemupgrader.api.data.UpgradeEntrySet;
import io._3650.itemupgrader.api.data.UpgradeEventData;
import io._3650.itemupgrader.api.serializer.UpgradeConditionSerializer;
import io._3650.itemupgrader.api.type.UpgradeCondition;
import io._3650.itemupgrader.api.util.ComponentHelper;
import io._3650.itemupgrader.api.util.UpgradeJsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

public class PredicateUpgradeCondition extends UpgradeCondition {
	
	private final ResourceLocation predicateId;
	private final UpgradeEntry<Entity> entityEntry;
	private final UpgradeEntry<Vec3> posEntry;
	private final Vec3 posOffset;
	private final UpgradeEntry<BlockPos> blockPosEntry;
	private final Vec3i blockPosOffset;
	private final UpgradeEntry<ItemStack> itemEntry;
	
	public PredicateUpgradeCondition(IUpgradeInternals internals,
			ResourceLocation predicateId,
			UpgradeEntry<Entity> entityEntry,
			UpgradeEntry<Vec3> posEntry,
			Vec3 posOffset,
			UpgradeEntry<BlockPos> blockPosEntry,
			Vec3i blockPosOffset,
			UpgradeEntry<ItemStack> itemEntry) {
		super(internals, false, UpgradeEntrySet.ENTITY.fillCategories(mapper -> {
			mapper
				.set(EntryCategory.ENTITY, entityEntry)
				.set(EntryCategory.POSITION, posEntry)
				.setOptional(EntryCategory.BLOCK_POS, blockPosEntry)
				.setOptional(EntryCategory.ITEM, itemEntry);
			
		}));
		this.predicateId = predicateId;
		this.entityEntry = entityEntry;
		this.posEntry = posEntry;
		this.posOffset = posOffset;
		this.blockPosEntry = blockPosEntry;
		this.blockPosOffset = blockPosOffset;
		this.itemEntry = itemEntry;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		if (data.getEntry(UpgradeEntry.SIDE).isClient()) return false;
		if (!(data.getEntry(UpgradeEntry.LEVEL) instanceof ServerLevel level)) return false;
		LootItemCondition predicate = level.getServer().getPredicateManager().get(this.predicateId);
		if (predicate == null) return false;
		
		BlockPos blockPos = data.getEntryOrNull(this.blockPosEntry);
		if (blockPos != null) blockPos = blockPos.offset(this.blockPosOffset);
		BlockState blockState = this.blockPosOffset == Vec3i.ZERO || blockPos == null ? data.getEntryOrNull(UpgradeEntry.BLOCK_STATE) : level.getBlockState(blockPos);
		
		LootContext context = new LootContext.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, data.getEntry(this.entityEntry))
				.withParameter(LootContextParams.ORIGIN, data.getEntry(this.posEntry))
				.withOptionalParameter(LootContextParams.BLOCK_ENTITY, data.getEntryOrNull(UpgradeEntry.BLOCK_ENTITY))
				.withOptionalParameter(LootContextParams.BLOCK_STATE, blockState)
				.withOptionalParameter(LootContextParams.DAMAGE_SOURCE, data.getEntryOrNull(UpgradeEntry.DAMAGE_SOURCE))
				.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, data.getEntryOrNull(UpgradeEntry.DIRECT_DAMAGER))
				.withOptionalParameter(LootContextParams.KILLER_ENTITY, data.getEntryOrNull(UpgradeEntry.DAMAGER_ENTITY))
				.withOptionalParameter(LootContextParams.TOOL, data.getEntryOrNull(UpgradeEntry.ITEM))
				.create(LootContextParamSets.COMMAND);
		
		return predicate.test(context);
	}
	
	@Override
	public MutableComponent[] getTooltip(ItemStack stack) {
		return ComponentHelper.arrayify(new TranslatableComponent("predicate." + ComponentHelper.keyFormat(this.predicateId)));
	}
	
	@Override
	public Serializer getSerializer() {
		return new Serializer();
	}
	
	@Override
	public void hackyToNetworkReadJavadoc(FriendlyByteBuf buf) {
		this.getSerializer().toNetwork(this, buf);
	}
	
	public static class Serializer extends UpgradeConditionSerializer<PredicateUpgradeCondition> {
		
		@Override
		public PredicateUpgradeCondition fromJson(IUpgradeInternals internals, boolean inverted, JsonObject json) {
			ResourceLocation predicateId = new ResourceLocation(GsonHelper.getAsString(json, "predicate"));
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromJson(json);
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromJson(json);
			Vec3 posOffset = UpgradeJsonHelper.getPosition(json, "offset");
			UpgradeEntry<BlockPos> blockPosEntry = EntryCategory.BLOCK_POS.fromJson(json);
			Vec3i blockPosOffset = UpgradeJsonHelper.getIntPosition(json, "block_offset");
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromJson(json);
			return new PredicateUpgradeCondition(internals, predicateId, entityEntry, posEntry, posOffset, blockPosEntry, blockPosOffset, itemEntry);
		}
		
		@Override
		public void toNetwork(PredicateUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeResourceLocation(condition.predicateId);
			condition.entityEntry.toNetwork(buf);
			condition.posEntry.toNetwork(buf);
			buf.writeDouble(condition.posOffset.x).writeDouble(condition.posOffset.y).writeDouble(condition.posOffset.z);
			condition.blockPosEntry.toNetwork(buf);
			buf.writeInt(condition.blockPosOffset.getX()).writeInt(condition.blockPosOffset.getY()).writeInt(condition.blockPosOffset.getZ());
			condition.itemEntry.toNetwork(buf);
		}
		
		@Override
		public PredicateUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			ResourceLocation predicateId = buf.readResourceLocation();
			UpgradeEntry<Entity> entityEntry = EntryCategory.ENTITY.fromNetwork(buf);
			UpgradeEntry<Vec3> posEntry = EntryCategory.POSITION.fromNetwork(buf);
			Vec3 posOffset = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			UpgradeEntry<BlockPos> blockPosEntry = EntryCategory.BLOCK_POS.fromNetwork(buf);
			Vec3i blockPosOffset = new Vec3i(buf.readInt(), buf.readInt(), buf.readInt());
			UpgradeEntry<ItemStack> itemEntry = EntryCategory.ITEM.fromNetwork(buf);
			return new PredicateUpgradeCondition(internals, predicateId, entityEntry, posEntry, posOffset, blockPosEntry, blockPosOffset, itemEntry);
		}
		
	}
	
}
