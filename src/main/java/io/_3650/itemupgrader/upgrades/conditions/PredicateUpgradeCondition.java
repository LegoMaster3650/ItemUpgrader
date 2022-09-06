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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class PredicateUpgradeCondition extends UpgradeCondition {
	
	private final ResourceLocation predicateId;
	
	public PredicateUpgradeCondition(IUpgradeInternals internals, ResourceLocation predicateId) {
		super(internals, false);
		this.predicateId = predicateId;
	}
	
	@Override
	public UpgradeEntrySet requiredData() {
		return UpgradeEntrySet.ENTITY;
	}
	
	@Override
	public boolean test(UpgradeEventData data) {
		if (data.getEntry(UpgradeEntry.SIDE).isClient()) return false;
		if (!(data.getEntry(UpgradeEntry.LEVEL) instanceof ServerLevel level)) return false;
		LootItemCondition predicate = level.getServer().getPredicateManager().get(this.predicateId);
		if (predicate == null) return false;
		
		LootContext context = new LootContext.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, data.getEntry(UpgradeEntry.ENTITY))
				.withParameter(LootContextParams.ORIGIN, data.getEntry(UpgradeEntry.ORIGIN))
				.withOptionalParameter(LootContextParams.BLOCK_ENTITY, data.getEntryOrNull(UpgradeEntry.BLOCK_ENTITY))
				.withOptionalParameter(LootContextParams.BLOCK_STATE, data.getEntryOrNull(UpgradeEntry.BLOCK_STATE))
				.withOptionalParameter(LootContextParams.DAMAGE_SOURCE, data.getEntryOrNull(UpgradeEntry.DAMAGE_SOURCE))
				.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, data.getEntryOrNull(UpgradeEntry.DIRECT_DAMAGER))
				.withOptionalParameter(LootContextParams.EXPLOSION_RADIUS, data.getEntryOrNull(UpgradeEntry.EXPLOSION_RADIUS))
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
			return new PredicateUpgradeCondition(internals, predicateId);
		}
		
		@Override
		public void toNetwork(PredicateUpgradeCondition condition, FriendlyByteBuf buf) {
			buf.writeResourceLocation(condition.predicateId);
		}
		
		@Override
		public PredicateUpgradeCondition fromNetwork(IUpgradeInternals internals, boolean inverted, FriendlyByteBuf buf) {
			ResourceLocation netPredicateId = buf.readResourceLocation();
			return new PredicateUpgradeCondition(internals, netPredicateId);
		}
		
	}
	
}
